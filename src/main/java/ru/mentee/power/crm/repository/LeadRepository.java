package ru.mentee.power.crm.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ru.mentee.power.crm.model.Lead;

/** Contract for lead persistence operations with CRUD and email-based lookup. */
public interface LeadRepository {
    /** Saves a lead and returns the persisted instance. */
    Lead save(Lead lead);

    /** Finds a lead by its unique ID. */
    Optional<Lead> findById(UUID id);

    /** Finds a lead by email address. */
    Optional<Lead> findByEmail(String email);

    /** Returns all persisted leads. */
    List<Lead> findAll();

    /** Deletes a lead by its ID. */
    void delete(UUID id);

    /** Returns the total number of stored leads. */
    int size();
}
