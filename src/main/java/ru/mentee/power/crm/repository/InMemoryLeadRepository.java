package ru.mentee.power.crm.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import ru.mentee.power.crm.model.Lead;

/** Lead repository using HashMap for O(1) ID-based lookup.*/
@Repository
public class InMemoryLeadRepository implements LeadRepository {

    private final Map<UUID, Lead> storage = new LinkedHashMap<>();
    private final Map<String, UUID> emailIndex = new HashMap<>();

    /** Saves a lead (rejects null). */
    @Override
    public Lead save(Lead lead) {
        if (lead == null) {
            throw new IllegalArgumentException("Lead must not be null");
        }

        storage.put(lead.id(), lead);
        emailIndex.put(lead.email(), lead.id());
        return lead;
    }

    /** Returns lead by ID, or null if not found. */
    @Override
    public Optional<Lead> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    /** Finds a lead by email using an index for O(1) lookup. */
    @Override
    public Optional<Lead> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        UUID id = emailIndex.get(email);
        return (id != null) ? Optional.ofNullable(storage.get(id)) : Optional.empty();
    }

    /** Returns a new list of all leads (defensive copy). */
    @Override
    public List<Lead> findAll() {
        return new ArrayList<>(storage.values());
    }

    /** Removes lead by ID. */
    @Override
    public void delete(UUID id) {
        Lead lead = storage.remove(id);
        if (lead != null) {
            emailIndex.remove(lead.email());
        }
    }

    /** Returns number of stored leads. */
    @Override
    public int size() {
        return storage.size();
    }
}