package ru.mentee.power.crm.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.mentee.power.crm.model.Lead;

/** Lead repository using HashMap for O(1) ID-based lookup.*/
public class LeadRepository {
    private final Map<String, Lead> storage = new HashMap<>();

    /** Saves a lead (rejects null). */
    public void save(Lead lead) {
        if (lead == null) {
            throw new IllegalArgumentException("Lead must not be null");
        }
        storage.put(lead.id(), lead);
    }

    /** Returns lead by ID, or null if not found. */
    public Lead findById(String id) {
        return storage.get(id);
    }

    /** Returns a new list of all leads (defensive copy). */
    public List<Lead> findAll() {
        return new ArrayList<>(storage.values());
    }

    /** Removes lead by ID. */
    public void delete(String id) {
        storage.remove(id);
    }

    /** Returns number of stored leads. */
    public int size() {
        return storage.size();
    }
}