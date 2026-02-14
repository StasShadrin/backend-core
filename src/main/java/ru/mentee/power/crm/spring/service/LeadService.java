package ru.mentee.power.crm.spring.service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.mentee.power.crm.domain.Deal;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.spring.repository.DealRepository;
import ru.mentee.power.crm.spring.repository.LeadRepository;

/** Service layer for lead-related business logic, including deduplication by email. */
@Service
@RequiredArgsConstructor
public class LeadService {
  private static final Logger LOG = LoggerFactory.getLogger(LeadService.class);
  private final LeadRepository leadRepository;
  private final DealRepository dealRepository;

  @PostConstruct
  void init() {
    LOG.info("LeadService @PostConstruct init() called - Bean lifecycle phase");
  }

  /**
   * Creates a new lead after ensuring the email is unique.
   *
   * @throws IllegalStateException if a lead with the given email already exists
   */
  public Lead addLead(Lead lead) {
    // Бизнес-правило: проверка уникальности email
    Optional<Lead> existing = leadRepository.findByEmail(lead.email());
    if (existing.isPresent()) {
      throw new IllegalStateException("Lead with email already exists: " + lead.email());
    }

    Lead newLead =
        new Lead(
            UUID.randomUUID(),
            lead.name(),
            lead.email(),
            lead.phone(),
            lead.company(),
            lead.status());

    return leadRepository.save(newLead);
  }

  /** Returns all leads stored in the repository. */
  public List<Lead> findAll() {
    return leadRepository.findAll();
  }

  /** Finds a lead by its unique ID. */
  public Optional<Lead> findById(UUID id) {
    return leadRepository.findById(id);
  }

  /** Finds a lead by email address. */
  public Optional<Lead> findByEmail(String email) {
    return leadRepository.findByEmail(email);
  }

  /** Возвращает список лидов с указанным статусом. */
  public List<Lead> findByStatus(LeadStatus status) {
    return leadRepository.findAll().stream().filter(lead -> lead.status().equals(status)).toList();
  }

  /** Обновление существующего лида. */
  public void update(UUID id, Lead updatedLead) {
    Lead existing =
        leadRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + id));
    Lead updated =
        new Lead(
            existing.id(),
            updatedLead.name(),
            updatedLead.email(),
            updatedLead.phone(),
            updatedLead.company(),
            updatedLead.status());

    leadRepository.save(updated);
  }

  /** Удаление существующего лида. */
  public void delete(UUID id) {
    leadRepository
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    leadRepository.deleteById(id);
  }

  /** Выполняет поиск и фильтрацию лидов по текстовому запросу и статусу. */
  public List<Lead> findLeads(String search, LeadStatus status) {
    List<Lead> leads = leadRepository.findAll();
    Stream<Lead> stream = leads.stream();

    if (search != null && !search.trim().isEmpty()) {
      String lowerSearch = search.toLowerCase().trim();
      stream =
          stream.filter(
              lead ->
                  lead.name().toLowerCase().contains(lowerSearch)
                      || lead.email().toLowerCase().contains(lowerSearch)
                      || lead.company().toLowerCase().contains(lowerSearch));
    }

    if (status != null) {
      stream = stream.filter(lead -> lead.status().equals(status));
    }

    return stream.toList();
  }

  /**
   * Конвертирует существующий лид в новую сделку. Проверяет существование лида и создаёт сделку со
   * статусом NEW.
   */
  @Transactional
  public Deal convertLeadToDeal(UUID leadId, BigDecimal amount) {
    Lead lead =
        leadRepository
            .findById(leadId)
            .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + leadId));

    if (lead.status() == LeadStatus.CONVERTED) {
      throw new IllegalStateException("Lead already converted: " + leadId);
    }

    Deal deal = new Deal(leadId, amount);
    dealRepository.save(deal);

    Lead updatedLead =
        new Lead(
            lead.id(),
            lead.name(),
            lead.email(),
            lead.phone(),
            lead.company(),
            LeadStatus.CONVERTED);
    leadRepository.save(updatedLead);
    return deal;
  }
}
