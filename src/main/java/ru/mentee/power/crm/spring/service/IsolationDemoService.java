package ru.mentee.power.crm.spring.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.mentee.power.crm.entity.Lead;
import ru.mentee.power.crm.spring.repository.JpaLeadRepository;

/**
 * Сервис для демонстрации уровней изоляции транзакций.
 * Показывает разницу между READ_COMMITTED и REPEATABLE_READ.
 */
@Service
@RequiredArgsConstructor
public class IsolationDemoService {

    private final JpaLeadRepository leadRepository;

    /**
     * Читает лид с уровнем изоляции READ_COMMITTED.
     * Гарантирует, что будут прочитаны только зафиксированные данные.
     * Может возникнуть (non-repeatable read).
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Lead readWithReadCommitted(UUID leadId) {
        return leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));
    }

    /**
     * Читает лид с уровнем изоляции REPEATABLE_READ.
     * Гарантирует, что повторные чтения в рамках одной транзакции
     * вернут одинаковые данные, даже если другие транзакции их изменили.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Lead readWithRepeatableRead(UUID leadId) {
        return leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));
    }
}
