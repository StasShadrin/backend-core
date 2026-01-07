package ru.mentee.power.crm.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

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

    private final LeadRepository repository;

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
}