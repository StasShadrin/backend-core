package ru.mentee.power.crm.spring.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.mentee.power.crm.entity.Lead;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.spring.repository.JpaLeadRepository;

/**
 * Сервис для демонстрации различных уровней распространения транзакций (propagation).
 * Показывает поведение REQUIRED, REQUIRES_NEW и MANDATORY в реальных сценариях.
 */
@Service
@RequiredArgsConstructor
public class PropagationDemoService {

    private final JpaLeadRepository leadRepository;

    /**
     * Метод с распространением REQUIRED (по умолчанию).
     * Использует существующую транзакцию, если она есть, или создаёт новую.
     * Это наиболее часто используемый уровень распространения.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void methodWithRequired(UUID leadId) {
        updateLeadStatus(leadId, LeadStatus.CONTACTED);
    }

    /**
     * Метод с распространением REQUIRES_NEW.
     * Всегда создаёт новую транзакцию, приостанавливая текущую (если есть).
     * Полезен для операций, которые должны фиксироваться независимо от основной транзакции
     * (например, логирование, аудит, уведомления).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void methodWithRequiresNew(UUID leadId) {
        updateLeadStatus(leadId, LeadStatus.QUALIFIED);
    }

    /**
     * Метод с распространением MANDATORY.
     * Требует существующую транзакцию. Если транзакции нет, выбрасывается исключение.
     * Используется для методов, которые не должны вызываться вне транзакционного контекста.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void methodWithMandatory(UUID leadId) {
        updateLeadStatus(leadId, LeadStatus.LOST);
    }

    private void updateLeadStatus(UUID leadId, LeadStatus status) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));
        lead.setStatus(status);
        leadRepository.save(lead);
    }
}