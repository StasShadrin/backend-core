package ru.mentee.power.crm.config;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.mentee.power.crm.domain.DealStatus;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.spring.service.DealService;
import ru.mentee.power.crm.spring.service.LeadService;
import ru.mentee.power.crm.util.TestDataUtils;

/**
 * Инициализатор тестовых данных при запуске приложения. Создаёт демо-лиды и сделки для демонстрации
 * воронки продаж.
 */
@Component
@RequiredArgsConstructor
public class TestDataInitializer {

  private final LeadService leadService;
  private final DealService dealService;

  /**
   * Загружает демо-данные (лиды и сделки) в сервисы при старте контекста Spring. Выполняется только
   * если данные ещё не созданы.
   */
  @PostConstruct
  public void init() {
    if (leadService.findAll().isEmpty()) {
      TestDataUtils.initializeTestData(leadService);
    }

    if (dealService.getAllDeals().isEmpty()) {
      initializeDeals();
    }
  }

  /** Создаёт сделку для каждого квалифицированного лида и распределяет по статусам */
  private void initializeDeals() {
    var qualifiedLeads = leadService.findByStatus(LeadStatus.QUALIFIED);

    if (qualifiedLeads.isEmpty()) {
      return;
    }

    for (int i = 0; i < qualifiedLeads.size(); i++) {
      var lead = qualifiedLeads.get(i);
      BigDecimal amount = new BigDecimal(100000 + i * 1000);

      var deal = leadService.convertLeadToDeal(lead.id(), amount);

      try {
        switch (i % 4) {
          case 0:
            break;
          case 1:
            dealService.transitionDealStatus(deal.getId(), DealStatus.QUALIFIED);
            break;
          case 2:
            dealService.transitionDealStatus(deal.getId(), DealStatus.QUALIFIED);
            dealService.transitionDealStatus(deal.getId(), DealStatus.PROPOSAL_SENT);
            break;
          case 3:
            dealService.transitionDealStatus(deal.getId(), DealStatus.QUALIFIED);
            dealService.transitionDealStatus(deal.getId(), DealStatus.PROPOSAL_SENT);
            dealService.transitionDealStatus(deal.getId(), DealStatus.NEGOTIATION);
            dealService.transitionDealStatus(deal.getId(), DealStatus.WON);
            break;
          default:
        }
      } catch (IllegalStateException e) {
        System.out.println("Transition failed: " + e.getMessage());
      }
    }
  }
}
