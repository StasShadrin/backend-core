package ru.mentee.power.crm.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.repository.LeadRepository;

/**
 * Service layer for lead-related business logic, including deduplication by email.
 */
@Service
@RequiredArgsConstructor
public class LeadService {
    private static final Logger log = LoggerFactory.getLogger(LeadService.class);
    private final LeadRepository repository;

    @PostConstruct
    void init() {
        log.info("LeadService @PostConstruct init() called - Bean lifecycle phase");
    }

    /**
     * Creates a new lead after ensuring the email is unique.
     *
     * @throws IllegalStateException if a lead with the given email already exists
     */
    public Lead addLead(String email, String phone, String company, LeadStatus status) {
        // Бизнес-правило: проверка уникальности email
        Optional<Lead> existing = repository.findByEmail(email);
        if (existing.isPresent()) {
            throw new IllegalStateException("Lead with email already exists: " + email);
        }

        Lead lead = new Lead(
                UUID.randomUUID(),
                email,
                phone,
                company,
                status
        );

        return repository.save(lead);
    }

    /** Returns all leads stored in the repository. */
    public List<Lead> findAll() {
        return repository.findAll();
    }

    /** Finds a lead by its unique ID. */
    public Optional<Lead> findById(UUID id) {
        return repository.findById(id);
    }

    /** Finds a lead by email address. */
    public Optional<Lead> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    /** Возвращает список лидов с указанным статусом. */
    public List<Lead> findByStatus(LeadStatus status) {
        return repository.findAll().stream()
                .filter(lead -> lead.status().equals(status))
                .toList();
    }

    /** Обновление существующего лида */
    public void update(UUID id, Lead updatedLead) {
        Lead existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + id));
        Lead updated = new Lead(
                existing.id(),
                updatedLead.email(),
                updatedLead.phone(),
                updatedLead.company(),
                updatedLead.status()
        );

        repository.save(updated);
    }
}