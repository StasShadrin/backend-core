package ru.mentee.power.crm.spring.service;

import java.util.UUID;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.mentee.power.crm.entity.Lead;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.spring.exception.LeadNotFoundException;
import ru.mentee.power.crm.spring.repository.JpaLeadRepository;

/**
 * Сервис для демонстрации pessimistic и optimistic locking.
 * Используется в тестах конкурентного доступа (BCORE-30).
 */
@Service
@RequiredArgsConstructor
public class LeadLockingService {

    private final JpaLeadRepository leadRepository;

    /**
     * Критическая операция с pessimistic locking.
     * Используется для демонстрации @Lock(PESSIMISTIC_WRITE).
     */
    @Transactional
    public Lead convertLeadToDealWithLock(UUID leadId, LeadStatus newStatus) {
        // Блокируем Lead эксклюзивно до конца транзакции
        Lead lead = leadRepository.findByIdForUpdate(leadId)
                .orElseThrow(() -> new LeadNotFoundException(leadId));

        // Здесь могла бы быть сложная бизнес-логика конверсии:
        // - создание Deal
        // - обновление статуса Lead
        // - отправка уведомлений
        // Другие транзакции ЖДУТ завершения этой операции

        lead.setStatus(newStatus);
        return leadRepository.save(lead);
    }

    /**
     * Обычное обновление с optimistic locking.
     * Использует @Version для защиты от конфликтов.
     */
    @Transactional
    public void updateLeadStatusOptimistic(UUID leadId, LeadStatus newStatus) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new LeadNotFoundException(leadId));

        // Блокировки НЕТ — другие транзакции могут читать и изменять
        // При сохранении JPA проверит version и выбросит OptimisticLockException если конфликт

        lead.setStatus(newStatus);
        leadRepository.save(lead);
        // UPDATE leads SET status=?, version=version+1 WHERE id=? AND version=?
    }

    /** Метод для демонстрации обработки OptimisticLockException */
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @Transactional
    public Lead updateWithRetry(UUID leadId, LeadStatus newStatus) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new LeadNotFoundException(leadId));
        lead.setStatus(newStatus);
        return leadRepository.save(lead);
    }

    /**
     * Блокирует два лида в указанном порядке.
     * Используется для демонстрации deadlock при разном порядке блокировок.
     */
    @Transactional
    public void processTwoLeadsInOrder(UUID leadId1, UUID leadId2) {
        // Блокируем первый лид
        leadRepository.findByIdForUpdate(leadId1)
                .orElseThrow(() -> new LeadNotFoundException(leadId1));

        // Небольшая задержка для гарантии пересечения с другим потоком
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during processing", e);
        }

        // Блокируем второй лид
        leadRepository.findByIdForUpdate(leadId2)
                .orElseThrow(() -> new LeadNotFoundException(leadId2));
    }
}