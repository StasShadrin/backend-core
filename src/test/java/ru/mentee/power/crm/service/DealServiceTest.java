package ru.mentee.power.crm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mentee.power.crm.domain.Deal;
import ru.mentee.power.crm.domain.DealStatus;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.spring.repository.DealRepository;
import ru.mentee.power.crm.spring.repository.LeadRepository;
import ru.mentee.power.crm.spring.service.DealService;
import ru.mentee.power.crm.spring.service.LeadService;

@ExtendWith(MockitoExtension.class)
class DealServiceTest {

  @Mock private DealRepository dealRepository;

  @Mock private LeadRepository leadRepository;

  @InjectMocks private DealService dealService;
  @InjectMocks private LeadService leadService;

  private final UUID leadId = UUID.randomUUID();
  private final UUID dealId = UUID.randomUUID();

  // 💡 Мокируем Lead и Deal для тестов
  private Lead mockLead() {
    return new Lead(leadId, "Иван", "ivan@example.com", "+7123", "Acme", LeadStatus.QUALIFIED);
  }

  private Deal mockDeal() {
    return new Deal(dealId, leadId, BigDecimal.valueOf(100000), DealStatus.NEW, null);
  }

  @Test
  void shouldCreateDealWhenLeadExists() {
    // Given
    when(leadRepository.findById(leadId)).thenReturn(Optional.of(mockLead()));
    new Deal(leadId, BigDecimal.valueOf(50000));

    doAnswer(
            invocation -> {
              invocation.getArgument(0);
              return null;
            })
        .when(dealRepository)
        .save(any());

    // When
    Deal result = leadService.convertLeadToDeal(leadId, BigDecimal.valueOf(50000));

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getLeadId()).isEqualTo(leadId);
    assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(50000));
    assertThat(result.getStatus()).isEqualTo(DealStatus.NEW);
    verify(dealRepository).save(any());
  }

  @Test
  void shouldThrowExceptionWhenLeadNotFound() {
    // Given
    when(leadRepository.findById(leadId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> leadService.convertLeadToDeal(leadId, BigDecimal.valueOf(100000)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Lead not found");
  }

  @Test
  void shouldTransitionStatusWhenDealExistsAndValid() {
    // Given
    Deal deal = mockDeal();
    when(dealRepository.findById(dealId)).thenReturn(Optional.of(deal));

    doAnswer(
            invocation -> {
              invocation.getArgument(0);
              return null;
            })
        .when(dealRepository)
        .save(any());

    // When
    Deal result = dealService.transitionDealStatus(dealId, DealStatus.QUALIFIED);

    // Then
    assertThat(result.getStatus()).isEqualTo(DealStatus.QUALIFIED);
    verify(dealRepository).save(any());
  }

  @Test
  void shouldThrowExceptionWhenDealNotFound() {
    // Given
    when(dealRepository.findById(dealId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> dealService.transitionDealStatus(dealId, DealStatus.QUALIFIED))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Deal not found");
  }

  @Test
  void shouldReturnAllDeals() {
    // Given
    List<Deal> deals = List.of(mockDeal(), mockDeal());
    when(dealRepository.findAll()).thenReturn(deals);

    // When
    List<Deal> result = dealService.getAllDeals();

    // Then
    assertThat(result).hasSize(2);
    verify(dealRepository).findAll();
  }

  @Test
  void shouldGroupDealsByStatus() {
    // Given
    Deal deal1 = new Deal(UUID.randomUUID(), leadId, BigDecimal.TEN, DealStatus.NEW, null);
    Deal deal2 = new Deal(UUID.randomUUID(), leadId, BigDecimal.TEN, DealStatus.QUALIFIED, null);
    Deal deal3 = new Deal(UUID.randomUUID(), leadId, BigDecimal.TEN, DealStatus.NEW, null);

    List<Deal> allDeals = List.of(deal1, deal2, deal3);
    when(dealRepository.findAll()).thenReturn(allDeals);

    // When
    Map<DealStatus, List<Deal>> result = dealService.getDealsByStatusForKanban();

    // Then
    assertThat(result).hasSize(2);
    assertThat(result.get(DealStatus.NEW)).hasSize(2);
    assertThat(result.get(DealStatus.QUALIFIED)).hasSize(1);
  }
}
