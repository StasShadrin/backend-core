package ru.mentee.power.crm.spring.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.mentee.power.crm.domain.DealStatus;
import ru.mentee.power.crm.entity.Deal;
import ru.mentee.power.crm.spring.repository.JpaDealRepository;

/** JPA-версия сервиса для управления сделками. */
@Service
@RequiredArgsConstructor
public class JpaDealService {

    private final JpaDealRepository dealRepository;

    /**
     * Изменяет статус существующей сделки.
     * Для JPA-версии бизнес-логика упрощена (без state machine).
     */
    @Transactional
    public Deal transitionDealStatus(UUID dealId, DealStatus newStatus) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new IllegalArgumentException("Deal not found: " + dealId));

        deal.setStatus(newStatus);
        return dealRepository.save(deal);
    }

    /** Возвращает все существующие сделки. */
    public List<Deal> getAllDeals() {
        return dealRepository.findAll();
    }

    /** Группирует все сделки по статусам для отображения Kanban-доски.*/
    public Map<DealStatus, List<Deal>> getDealsByStatusForKanban() {
        return dealRepository.findAll().stream()
                .collect(Collectors.groupingBy(Deal::getStatus));
    }
}