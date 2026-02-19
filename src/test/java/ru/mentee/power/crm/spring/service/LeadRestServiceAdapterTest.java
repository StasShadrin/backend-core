package ru.mentee.power.crm.spring.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mentee.power.crm.entity.Company;
import ru.mentee.power.crm.entity.Lead;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.spring.dto.CreateLeadRequest;
import ru.mentee.power.crm.spring.dto.LeadResponse;
import ru.mentee.power.crm.spring.dto.UpdateLeadRequest;
import ru.mentee.power.crm.spring.exception.EntityNotFoundException;
import ru.mentee.power.crm.spring.mapper.LeadMapper;
import ru.mentee.power.crm.spring.repository.CompanyRepository;

@ExtendWith(MockitoExtension.class)
class LeadRestServiceAdapterTest {

  @Mock private JpaLeadService leadService;

  @Mock private CompanyRepository companyRepository;

  @Mock private LeadMapper leadMapper;

  @InjectMocks private LeadRestServiceAdapter adapter;

  @Captor private ArgumentCaptor<Lead> leadCaptor;

  private UUID leadId;
  private UUID companyId;
  private Lead lead;
  private Company company;
  private LeadResponse leadResponse;
  private OffsetDateTime now;

  @BeforeEach
  void setUp() {
    now = OffsetDateTime.now();
    leadId = UUID.randomUUID();
    companyId = UUID.randomUUID();

    company = Company.builder().id(companyId).name("Тестовая компания").industry("IT").build();

    lead =
        Lead.builder()
            .id(leadId)
            .name("Иван Иванов")
            .email("ivan@example.com")
            .phone("+79991234567")
            .company(company)
            .status(LeadStatus.NEW)
            .createdAt(now)
            .updatedAt(now)
            .version(1L)
            .build();

    leadResponse =
        new LeadResponse(
            leadId,
            "Иван Иванов",
            "ivan@example.com",
            "+79991234567",
            companyId,
            LeadStatus.NEW,
            now);
  }

  @Test
  @DisplayName("Должен вернуть список всех лидов")
  void shouldReturnAllLeads() {
    // Given
    List<Lead> leads = List.of(lead);
    when(leadService.findAll()).thenReturn(leads);
    when(leadMapper.toResponse(lead)).thenReturn(leadResponse);

    // When
    List<LeadResponse> result = adapter.findAllLeads();

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.getFirst()).isEqualTo(leadResponse);
    verify(leadService).findAll();
    verify(leadMapper).toResponse(lead);
  }

  @Test
  @DisplayName("Должен вернуть пустой список, если лидов нет")
  void shouldReturnEmptyListWhenNoLeads() {
    // Given
    when(leadService.findAll()).thenReturn(List.of());

    // When
    List<LeadResponse> result = adapter.findAllLeads();

    // Then
    assertThat(result).isEmpty();
    verify(leadService).findAll();
    verify(leadMapper, never()).toResponse(any());
  }

  @Test
  @DisplayName("Должен найти лида по ID")
  void shouldFindLeadById() {
    // Given
    when(leadService.findById(leadId)).thenReturn(Optional.of(lead));
    when(leadMapper.toResponse(lead)).thenReturn(leadResponse);

    // When
    LeadResponse result = adapter.findLeadById(leadId);

    // Then
    assertThat(result).isEqualTo(leadResponse);
    verify(leadService).findById(leadId);
    verify(leadMapper).toResponse(lead);
  }

  @Test
  @DisplayName("Должен выбросить исключение при поиске несуществующего лида")
  void shouldThrowExceptionWhenLeadNotFound() {
    // Given
    UUID nonExistentId = UUID.randomUUID();
    when(leadService.findById(nonExistentId)).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> adapter.findLeadById(nonExistentId))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Lead not found: " + nonExistentId);

    verify(leadService).findById(nonExistentId);
    verify(leadMapper, never()).toResponse(any());
  }

  @Test
  @DisplayName("Должен создать нового лида")
  void shouldCreateLead() {
    // Given
    CreateLeadRequest request =
        new CreateLeadRequest("Иван Иванов", "ivan@example.com", "+79991234567", companyId);

    when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
    when(leadMapper.toEntity(request)).thenReturn(lead);
    when(leadService.createLead(lead)).thenReturn(lead);
    when(leadMapper.toResponse(lead)).thenReturn(leadResponse);

    // When
    LeadResponse result = adapter.createLead(request);

    // Then
    assertThat(result).isEqualTo(leadResponse);
    verify(companyRepository).findById(companyId);
    verify(leadMapper).toEntity(request);
    verify(leadService).createLead(leadCaptor.capture()); // Используем ArgumentCaptor

    Lead capturedLead = leadCaptor.getValue();
    assertThat(capturedLead.getCompany()).isEqualTo(company);
    assertThat(capturedLead.getStatus()).isEqualTo(LeadStatus.NEW);

    verify(leadMapper).toResponse(lead);
  }

  @Test
  @DisplayName("Должен выбросить исключение при создании лида с несуществующей компанией")
  void shouldThrowExceptionWhenCompanyNotFoundOnCreate() {
    // Given
    CreateLeadRequest request =
        new CreateLeadRequest("Иван Иванов", "ivan@example.com", "+79991234567", companyId);

    when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> adapter.createLead(request))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Company not found: " + companyId);

    verify(companyRepository).findById(companyId);
    verify(leadMapper, never()).toEntity(any());
    verify(leadService, never()).createLead(any());
  }

  @Test
  @DisplayName("Должен обновить существующего лида")
  void shouldUpdateLead() {
    // Given
    UpdateLeadRequest request =
        new UpdateLeadRequest(
            Optional.of("Иван Петров"),
            Optional.of("ivan.petrov@example.com"),
            Optional.of("+79991112233"),
            Optional.of(companyId),
            Optional.of(LeadStatus.CONTACTED));

    Lead updatedLead =
        Lead.builder()
            .id(leadId)
            .name("Иван Петров")
            .email("ivan.petrov@example.com")
            .phone("+79991112233")
            .company(company)
            .status(LeadStatus.CONTACTED)
            .createdAt(now)
            .updatedAt(now)
            .version(2L)
            .build();

    LeadResponse updatedResponse =
        new LeadResponse(
            leadId,
            "Иван Петров",
            "ivan.petrov@example.com",
            "+79991112233",
            companyId,
            LeadStatus.CONTACTED,
            now);

    when(leadService.findById(leadId)).thenReturn(Optional.of(lead));
    when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
    when(leadService.updateLead(any(UUID.class), any(Lead.class)))
        .thenReturn(Optional.of(updatedLead));
    when(leadMapper.toResponse(updatedLead)).thenReturn(updatedResponse);

    // When
    LeadResponse result = adapter.updateLead(leadId, request);

    // Then
    assertThat(result).isEqualTo(updatedResponse);
    verify(leadService).findById(leadId);
    verify(leadMapper).updateEntity(request, lead);
    verify(companyRepository).findById(companyId);
    verify(leadService).updateLead(eq(leadId), leadCaptor.capture());

    Lead capturedLead = leadCaptor.getValue();
    assertThat(capturedLead.getCompany()).isEqualTo(company);

    verify(leadMapper).toResponse(updatedLead);
  }

  @Test
  @DisplayName("Должен обновить лида без изменения компании")
  void shouldUpdateLeadWithoutCompany() {
    // Given
    UpdateLeadRequest request =
        new UpdateLeadRequest(
            Optional.of("Иван Петров"),
            Optional.of("ivan.petrov@example.com"),
            Optional.of("+79991112233"),
            Optional.empty(),
            Optional.of(LeadStatus.CONTACTED));

    Lead updatedLead =
        Lead.builder()
            .id(leadId)
            .name("Иван Петров")
            .email("ivan.petrov@example.com")
            .phone("+79991112233")
            .company(company)
            .status(LeadStatus.CONTACTED)
            .createdAt(now)
            .updatedAt(now)
            .version(2L)
            .build();

    LeadResponse updatedResponse =
        new LeadResponse(
            leadId,
            "Иван Петров",
            "ivan.petrov@example.com",
            "+79991112233",
            companyId,
            LeadStatus.CONTACTED,
            now);

    when(leadService.findById(leadId)).thenReturn(Optional.of(lead));
    when(leadService.updateLead(leadId, lead)).thenReturn(Optional.of(updatedLead));
    when(leadMapper.toResponse(updatedLead)).thenReturn(updatedResponse);

    // When
    LeadResponse result = adapter.updateLead(leadId, request);

    // Then
    assertThat(result).isEqualTo(updatedResponse);
    verify(leadService).findById(leadId);
    verify(leadMapper).updateEntity(request, lead);
    verify(companyRepository, never()).findById(any());
    verify(leadService).updateLead(leadId, lead);
    verify(leadMapper).toResponse(updatedLead);
  }

  @Test
  @DisplayName("Должен выбросить исключение при обновлении несуществующего лида")
  void shouldThrowExceptionWhenUpdatingNonExistentLead() {
    // Given
    UUID nonExistentId = UUID.randomUUID();
    UpdateLeadRequest request =
        new UpdateLeadRequest(
            Optional.of("Иван Петров"),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty());

    when(leadService.findById(nonExistentId)).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> adapter.updateLead(nonExistentId, request))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Lead not found: " + nonExistentId);

    verify(leadService).findById(nonExistentId);
    verify(leadMapper, never()).updateEntity(any(), any());
    verify(leadService, never()).updateLead(any(), any());
  }

  @Test
  @DisplayName("Должен выбросить исключение при обновлении с несуществующей компанией")
  void shouldThrowExceptionWhenUpdatingWithNonExistentCompany() {
    // Given
    UpdateLeadRequest request =
        new UpdateLeadRequest(
            Optional.of("Иван Петров"),
            Optional.empty(),
            Optional.empty(),
            Optional.of(companyId),
            Optional.empty());

    when(leadService.findById(leadId)).thenReturn(Optional.of(lead));
    when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> adapter.updateLead(leadId, request))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Company not found: " + companyId);

    verify(leadService).findById(leadId);
    verify(leadMapper).updateEntity(request, lead);
    verify(companyRepository).findById(companyId);
    verify(leadService, never()).updateLead(any(), any());
  }

  @Test
  @DisplayName("Должен удалить существующего лида")
  void shouldDeleteExistingLead() {
    // Given
    when(leadService.deleteLead(leadId)).thenReturn(true);

    // When
    adapter.deleteLead(leadId);

    // Then
    verify(leadService).deleteLead(leadId);
  }

  @Test
  @DisplayName("Должен выбросить исключение при удалении несуществующего лида")
  void shouldThrowExceptionWhenDeletingNonExistentLead() {
    // Given
    UUID nonExistentId = UUID.randomUUID();
    when(leadService.deleteLead(nonExistentId)).thenReturn(false);

    // When/Then
    assertThatThrownBy(() -> adapter.deleteLead(nonExistentId))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Lead not found: " + nonExistentId);

    verify(leadService).deleteLead(nonExistentId);
  }
}
