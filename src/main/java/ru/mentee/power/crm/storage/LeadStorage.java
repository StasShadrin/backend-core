package ru.mentee.power.crm.storage;

import java.util.Objects;

import ru.mentee.power.crm.domain.Lead;

/**
 * A storage for leads (potential customers) based on a fixed-size array.
 * <p>
 * Provides the following features:
 * <ul>
 *   <li>Adds a lead only if no existing lead has the same email address</li>
 *   <li>Prevents duplicates</li>
 *   <li>Returns a compact array of all stored leads (without nulls)</li>
 * </ul>
 * The maximum capacity is {@value #MAX_SIZE} leads.
 */
public class LeadStorage {

    /**
     * Maximum number of leads that can be stored.
     */
    private static final int MAX_SIZE = 100;

    /**
     * Internal array for storing leads.
     * Unused slots are {@code null}.
     */
    private final Lead[] leads = new Lead[MAX_SIZE];

    /**
     * Adds a new lead to the storage.
     * <p>
     * The lead will not be added if another lead with the same email
     * already exists (comparison handles {@code null} safely).
     * <p>
     * If the storage is full, an {@link IllegalStateException} is thrown.
     *
     * @param lead the lead to add; must not be {@code null}
     * @return {@code true} if the lead was successfully added,
     *         {@code false} if a lead with the same email already exists
     * @throws IllegalStateException if the storage is full and cannot accept more leads
     * @throws NullPointerException if the provided lead is {@code null}
     */
    public boolean add(Lead lead) {
        if (lead == null) {
            throw new NullPointerException("Lead must not be null");
        }

        int firstNullIndex = -1;

        for (int i = 0; i < leads.length; i++) {
            if (leads[i] == null) {
                if (firstNullIndex == -1) {
                    firstNullIndex = i;
                }
            } else {
                if (Objects.equals(leads[i].getEmail(), lead.getEmail())) {
                    return false;
                }
            }
        }

        if (firstNullIndex != -1) {
            leads[firstNullIndex] = lead;
            return true;
        } else {
            throw new IllegalStateException("Storage is full, cannot add more leads");
        }
    }

    /**
     * Returns a copy of all stored leads, excluding {@code null} entries.
     * <p>
     * The order of elements matches the insertion order.
     *
     * @return an array of non-null leads; empty if no leads are stored
     */
    public Lead[] findAll() {
        Lead[] result = new Lead[size()];
        int resultIndex = 0;
        for (Lead lead : leads) {
            if (lead != null) {
                result[resultIndex++] = lead;
            }
        }
        return result;
    }

    /**
     * Returns the current number of leads in storage.
     *
     * @return the count of non-null elements in the {@link #leads} array
     */
    public int size() {
        int count = 0;
        for (Lead lead : leads) {
            if (lead != null) {
                count++;
            }
        }
        return count;
    }
}