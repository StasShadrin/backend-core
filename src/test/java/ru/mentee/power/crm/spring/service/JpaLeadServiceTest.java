package ru.mentee.power.crm.spring.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.mentee.power.crm.spring.dto.generated.LeadResponse.StatusEnum;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.mentee.power.crm.entity.Company;
import ru.mentee.power.crm.entity.Lead;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.spring.dto.CreateDealRequest;
import ru.mentee.power.crm.spring.exception.DuplicateEmailException;
import ru.mentee.power.crm.spring.exception.EntityNotFoundException;
import ru.mentee.power.crm.spring.exception.IllegalLeadStateException;
import ru.mentee.power.crm.spring.repository.CompanyRepository;
import ru.mentee.power.crm.spring.repository.JpaLeadRepository;

@SpringBootTest
@Transactional
class JpaLeadServiceTest {

  @Autowired private JpaLeadService leadService;

  @Autowired private JpaLeadRepository leadRepository;

  @Autowired private CompanyRepository companyRepository;

  @BeforeEach
  void setUp() {
    leadRepository.deleteAll();
    companyRepository.deleteAll();

    for (int i = 1; i <= 3; i++) {
      Company company = Company.builder().name("Company " + i).build();
      company = companyRepository.save(company);

      Lead lead =
          Lead.builder()
              .name("Lead" + i)
              .email("lead" + i + "@example.com")
              .phone(i + "123")
              .company(company)
              .status(StatusEnum.NEW)
              .build();
      leadRepository.save(lead);
    }

    for (int i = 4; i <= 5; i++) {
      Company company = Company.builder().name("LostCompany " + i).build();
      company = companyRepository.save(company);

      Lead lead =
          Lead.builder()
              .name("LostLead" + i)
              .email("lost" + i + "@example.com")
              .phone(i + "456")
              .company(company)
              .status(StatusEnum.LOST)
              .build();
      leadRepository.save(lead);
    }
  }

  @Test
  void convertNewToContacted_shouldUpdateMultipleLeads() {
    int updated = leadService.convertNewToContacted();

    assertThat(updated).isEqualTo(3);

    long contactedCount = leadRepository.countByStatus(StatusEnum.CONTACTED);
    assertThat(contactedCount).isEqualTo(3);

    long newCount = leadRepository.countByStatus(StatusEnum.NEW);
    assertThat(newCount).isZero();
  }

  @Test
  void archiveOldLeads_shouldDeleteLeadsByStatus() {
    int deleted = leadService.archiveOldLeads(StatusEnum.LOST);

    assertThat(deleted).isEqualTo(2);

    long lostCount = leadRepository.countByStatus(StatusEnum.LOST);
    assertThat(lostCount).isZero();

    long newCount = leadRepository.countByStatus(StatusEnum.NEW);
    assertThat(newCount).isEqualTo(3);
  }

  @Test
  void createLead_shouldSaveNewLead() {
    Company company = Company.builder().name("New Company").build();
    companyRepository.save(company);

    Lead newLead =
        Lead.builder()
            .name("New Lead")
            .email("new@example.com")
            .phone("123")
            .company(company)
            .status(StatusEnum.NEW)
            .build();

    Lead saved = leadService.createLead(newLead);

    assertThat(saved).isNotNull();
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getEmail()).isEqualTo("new@example.com");
  }

  @Test
  void createLead_shouldThrowException_whenEmailExists() {
    Company company = Company.builder().name("Dup Company").build();
    companyRepository.save(company);

    Lead duplicate =
        Lead.builder()
            .name("Duplicate")
            .email("lead1@example.com")
            .phone("123")
            .company(company)
            .status(StatusEnum.NEW)
            .build();

    assertThatThrownBy(() -> leadService.createLead(duplicate))
        .isInstanceOf(DuplicateEmailException.class);
  }

  @Test
  void findAll_shouldReturnAllLeads() {
    List<Lead> leads = leadService.findAll();

    assertThat(leads).hasSize(5);
  }

  @Test
  void findById_shouldReturnLead_whenExists() {
    UUID id = leadRepository.findAll().getFirst().getId();

    Optional<Lead> found = leadService.findById(id);

    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(id);
  }

  @Test
  void findByStatus_shouldReturnFilteredLeads() {
    List<Lead> newLeads = leadService.findByStatus(LeadStatus.NEW);

    assertThat(newLeads).hasSize(3);
    assertThat(newLeads.getFirst().getStatus()).isEqualTo(StatusEnum.NEW);
  }

  @Test
  void update_shouldUpdateLeadFields() {
    UUID id = leadRepository.findAll().getFirst().getId();
    Company company = Company.builder().name("Updated Company").build();
    companyRepository.save(company);

    Lead updatedLead =
        Lead.builder()
            .name("Updated Name")
            .email("updated@example.com")
            .phone("999")
            .company(company)
            .status(StatusEnum.CONTACTED)
            .build();

    leadService.update(id, updatedLead);

    Optional<Lead> result = leadService.findById(id);
    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo("Updated Name");
    assertThat(result.get().getStatus()).isEqualTo(StatusEnum.CONTACTED);
  }

  @Test
  void delete_shouldRemoveLead() {
    UUID id = leadRepository.findAll().getFirst().getId();

    leadService.delete(id);

    assertThat(leadService.findById(id)).isEmpty();
  }

  @Test
  void findLeads_shouldFilterBySearchAndStatus() {
    List<Lead> results = leadService.findLeads("Lead1", StatusEnum.NEW);

    assertThat(results).hasSize(1);
    assertThat(results.getFirst().getEmail()).isEqualTo("lead1@example.com");
  }

  @Test
  void findByEmail_shouldReturnLead_whenExists() {
    Optional<Lead> found = leadService.findByEmail("lead1@example.com");

    assertThat(found).isPresent();
    assertThat(found.get().getEmail()).isEqualTo("lead1@example.com");
  }

  @Test
  void findByStatuses_shouldReturnLeadsWithMultipleStatuses() {
    List<Lead> leads = leadService.findByStatuses(StatusEnum.NEW, StatusEnum.LOST);

    assertThat(leads).hasSize(5);
  }

  @Test
  void getFirstPage_shouldReturnPagedResults() {
    var page = leadService.getFirstPage(2);

    assertThat(page.getContent()).hasSize(2);
    assertThat(page.getTotalElements()).isEqualTo(5);
  }

  @Test
  void searchByCompany_shouldReturnPagedResults() {
    Company company =
        companyRepository
            .findByName("Company 1")
            .orElseThrow(() -> new RuntimeException("Company 'Company 1' not found"));

    var page = leadService.searchByCompany(company, 0, 1);

    assertThat(page.getContent()).hasSize(1);
    assertThat(page.getContent().getFirst().getEmail()).isEqualTo("lead1@example.com");
  }

  @Test
  void changeStatus_shouldUpdateLeadsByCompany() {
    Company company =
        companyRepository
            .findByName("Company 1")
            .orElseThrow(() -> new RuntimeException("Company 'Company 1' not found"));

    long initialNewCount = leadRepository.countByStatus(StatusEnum.NEW);
    assertThat(initialNewCount).isGreaterThanOrEqualTo(1);

    leadService.changeStatus(company, StatusEnum.CONTACTED);

    long newCount = leadRepository.countByStatus(StatusEnum.NEW);
    long contactedCount = leadRepository.countByStatus(StatusEnum.CONTACTED);

    assertThat(newCount).isEqualTo(initialNewCount - 1);
    assertThat(contactedCount).isEqualTo(1);
  }

  @Test
  void updateLead_shouldUpdate_whenEmailNotChanged() {
    // Given
    Lead existingLead = leadRepository.findAll().getFirst();
    UUID id = existingLead.getId();

    Lead updatedLead =
        Lead.builder()
            .name("New Name")
            .email(existingLead.getEmail())
            .phone("999")
            .company(existingLead.getCompany())
            .status(StatusEnum.CONTACTED)
            .build();

    // When
    Optional<Lead> result = leadService.updateLead(id, updatedLead);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo("New Name");
    assertThat(result.get().getStatus()).isEqualTo(StatusEnum.CONTACTED);
    assertThat(result.get().getEmail()).isEqualTo(existingLead.getEmail());
  }

  @Test
  void updateLead_shouldUpdate_whenNewEmailIsUnique() {
    // Given
    Lead existingLead = leadRepository.findAll().getFirst();
    UUID id = existingLead.getId();

    Lead updatedLead =
        Lead.builder()
            .name("New Name")
            .email("unique-new@example.com")
            .phone("999")
            .company(existingLead.getCompany())
            .status(StatusEnum.CONTACTED)
            .build();

    // When
    Optional<Lead> result = leadService.updateLead(id, updatedLead);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getEmail()).isEqualTo("unique-new@example.com");
  }

  @Test
  void updateLead_shouldThrowException_whenEmailAlreadyExists() {
    // Given
    List<Lead> leads = leadRepository.findAll();
    Lead firstLead = leads.get(0);
    Lead secondLead = leads.get(1);

    Lead updatedLead =
        Lead.builder()
            .name("Updated Name")
            .email(secondLead.getEmail())
            .phone("999")
            .company(firstLead.getCompany())
            .status(StatusEnum.CONTACTED)
            .build();

    // When/Then
    assertThatThrownBy(() -> leadService.updateLead(firstLead.getId(), updatedLead))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Email already in use");
  }

  @Test
  void updateLead_shouldReturnEmpty_whenLeadNotFound() {
    // Given
    UUID nonExistentId = UUID.randomUUID();
    Lead updatedLead = Lead.builder().name("Name").email("test@example.com").build();

    // When
    Optional<Lead> result = leadService.updateLead(nonExistentId, updatedLead);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void deleteLead_shouldReturnTrue_whenLeadExists() {
    // Given
    UUID id = leadRepository.findAll().getFirst().getId();

    // When
    boolean result = leadService.deleteLead(id);

    // Then
    assertThat(result).isTrue();
    assertThat(leadService.findById(id)).isEmpty();
  }

  @Test
  void deleteLead_shouldReturnFalse_whenLeadNotExists() {
    // Given
    UUID nonExistentId = UUID.randomUUID();

    // When
    boolean result = leadService.deleteLead(nonExistentId);

    // Then
    assertThat(result).isFalse();
  }

  @Test
  void convertLeadToDeal_shouldCreateDeal_whenLeadIsQualified() {
    // Given
    Company company = Company.builder().name("Deal Company").build();
    companyRepository.save(company);

    Lead qualifiedLead =
        Lead.builder()
            .name("Qualified Lead")
            .email("qualified@example.com")
            .phone("123")
            .company(company)
            .status(StatusEnum.QUALIFIED)
            .build();
    leadRepository.save(qualifiedLead);

    CreateDealRequest request = new CreateDealRequest();
    request.setTitle("Test Deal");
    request.setAmount(BigDecimal.valueOf(1000.0));

    // When
    leadService.convertLeadToDeal(qualifiedLead.getId(), request);

    // Then
    Lead updatedLead = leadService.findById(qualifiedLead.getId()).orElseThrow();
    assertThat(updatedLead.getStatus()).isEqualTo(StatusEnum.CONVERTED);
  }

  @Test
  void convertLeadToDeal_shouldThrowException_whenLeadNotFound() {
    // Given
    UUID nonExistentId = UUID.randomUUID();
    CreateDealRequest request = new CreateDealRequest();
    request.setTitle("Test Deal");
    request.setAmount(BigDecimal.valueOf(1000.0));

    // When/Then
    assertThatThrownBy(() -> leadService.convertLeadToDeal(nonExistentId, request))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Lead not found");
  }

  @Test
  void convertLeadToDeal_shouldThrowException_whenLeadNotQualified() {
    // Given
    Lead newLead = leadRepository.findByStatus(StatusEnum.NEW).getFirst();
    CreateDealRequest request = new CreateDealRequest();
    request.setTitle("Test Deal");
    request.setAmount(BigDecimal.valueOf(1000.0));

    // When/Then
    assertThatThrownBy(() -> leadService.convertLeadToDeal(newLead.getId(), request))
        .isInstanceOf(IllegalLeadStateException.class);
  }

  @Test
  void changeStatus_shouldThrowException_whenParametersAreNull() {
    // When/Then
    assertThatThrownBy(() -> leadService.changeStatus(null, StatusEnum.NEW))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Company and LeadStatus must not be null");

    assertThatThrownBy(() -> leadService.changeStatus(Company.builder().build(), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Company and LeadStatus must not be null");
  }
}
