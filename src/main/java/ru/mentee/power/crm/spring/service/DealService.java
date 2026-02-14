package ru.mentee.power.crm.spring.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mentee.power.crm.domain.Deal;
import ru.mentee.power.crm.domain.DealStatus;
import ru.mentee.power.crm.spring.repository.DealRepository;

/**
 * Сервис для управления сделками (Deal). Координирует взаимодействие между агрегатами Lead и Deal,
 * обеспечивает бизнес-логику.
 */
@Service
@RequiredArgsConstructor
public class DealService {
  private final DealRepository dealRepository;

  /**
   * Изменяет статус существующей сделки с проверкой валидности перехода. Делегирует валидацию
   * самому объекту Deal.
   */
  public Deal transitionDealStatus(UUID dealId, DealStatus newStatus) {
    Deal deal =
        dealRepository
            .findById(dealId)
            .orElseThrow(() -> new IllegalArgumentException("Deal not found: " + dealId));
    deal.transitionTo(newStatus);
    dealRepository.save(deal);
    return deal;
  }

  /** Возвращает все существующие сделки. */
  public List<Deal> getAllDeals() {
    return dealRepository.findAll();
  }

  /** Группирует все сделки по статусам для отображения Kanban-доски. */
  public Map<DealStatus, List<Deal>> getDealsByStatusForKanban() {
    return dealRepository.findAll().stream().collect(Collectors.groupingBy(Deal::getStatus));
  }
}
