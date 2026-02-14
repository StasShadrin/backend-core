package ru.mentee.power.crm.spring.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.mentee.power.crm.entity.Lead;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.spring.repository.JpaLeadRepository;

/**
 * Сервис для обработки отдельных лидов. Каждая операция выполняется в изолированной транзакции, что
 * позволяет независимо обрабатывать каждый лид без влияния на другие.
 */
@Service
@RequiredArgsConstructor
public class LeadProcessor {

  private final JpaLeadRepository leadRepository;

  /**
   * Обрабатывает один лид в отдельной транзакции. Используется REQUIRES_NEW для изоляции каждой
   * операции. Если обработка одного лида завершается ошибкой, это не влияет на обработку других
   * лидов в рамках batch-операции.
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void processSingleLead(UUID id) {
    Lead lead =
        leadRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + id));

    // Имитируем ошибку для лида с определённым email
    if ("fail@example.com".equals(lead.getEmail())) {
      throw new RuntimeException("Simulated failure for lead: " + id);
    }

    lead.setStatus(LeadStatus.CONTACTED);
    leadRepository.save(lead);
  }
}
