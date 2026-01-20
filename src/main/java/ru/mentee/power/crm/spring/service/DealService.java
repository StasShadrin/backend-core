package ru.mentee.power.crm.spring.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ru.mentee.power.crm.domain.Deal;
import ru.mentee.power.crm.domain.DealStatus;
import ru.mentee.power.crm.spring.repository.DealRepository;
import ru.mentee.power.crm.spring.repository.LeadRepository;

/**
 * Сервис для управления сделками (Deal).
 * Координирует взаимодействие между агрегатами Lead и Deal, обеспечивает бизнес-логику.
 */
@Service
@RequiredArgsConstructor
public class DealService {
    private final DealRepository dealRepository;
    private final LeadRepository leadRepository;

    /**
     * Конвертирует существующий лид в новую сделку.
     * Проверяет существование лида и создаёт сделку со статусом NEW.
     */
    public Deal convertLeadToDeal(UUID leadId, BigDecimal amount) {
        if (leadRepository.findById(leadId).isEmpty()) {
            throw new IllegalArgumentException("Lead not found: " + leadId);
        }
        Deal deal = new Deal(leadId, amount);
        dealRepository.save(deal);
        return deal;
    }

    /**
     * Изменяет статус существующей сделки с проверкой валидности перехода.
     * Делегирует валидацию самому объекту Deal.
     */
    public Deal transitionDealStatus(UUID dealId, DealStatus newStatus) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new IllegalArgumentException("Deal not found: " + dealId));
        deal.transitionTo(newStatus);
        dealRepository.save(deal);
        return deal;
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