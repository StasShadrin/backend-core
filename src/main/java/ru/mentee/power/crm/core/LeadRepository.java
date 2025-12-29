package ru.mentee.power.crm.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ru.mentee.power.crm.domain.Lead;

/**
 * Repository for {@link Lead} entities using {@link HashSet} for automatic deduplication.
 * <p>
 * Provides O(1) average-time complexity for add and contains operations,
 * leveraging the {@code equals()} and {@code hashCode()} contract of {@link Lead}.
 */
public class LeadRepository {
    private final Set<Lead> leads = new HashSet<>();

    /**
     * Adds a lead to the repository if not already present.
     *
     * @param lead the lead to add; must not be {@code null}
     * @return {@code true} if the lead was added, {@code false} if it was already present
     * @throws IllegalArgumentException if the lead is {@code null}
     */
    public boolean add(Lead lead) {
        if (lead == null) {
            throw new IllegalArgumentException("Lead must not be null");
        }
        return leads.add(lead);
    }

    /**
     * Checks whether the repository contains the specified lead.
     *
     * @param lead the lead to check; may be {@code null} (returns {@code false})
     * @return {@code true} if the lead is present, {@code false} otherwise
     */
    public boolean contains(Lead lead) {
        return leads.contains(lead);
    }

    /**
     * Returns an unmodifiable view of all leads in the repository.
     * <p>
     * Attempts to modify the returned set will result in an {@link UnsupportedOperationException}.
     *
     * @return an unmodifiable set of all leads
     */
    public Set<Lead> findAll() {
        return Collections.unmodifiableSet(leads);
    }

    /**
     * Returns the number of leads in the repository.
     *
     * @return the current size of the repository
     */
    public int size() {
        return leads.size();
    }
}