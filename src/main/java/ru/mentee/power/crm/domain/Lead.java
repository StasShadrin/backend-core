package ru.mentee.power.crm.domain;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a potential customer (lead) in the CRM system.
 */
public class Lead {

    private final UUID id;
    private final Contact contact;
    private final String company;
    private final String status;

    private static final Set<String> ALLOWED_STATUSES = Set.of("NEW", "QUALIFIED", "CONVERTED");

    /**
     * Creates a new Lead with the given properties.
     *
     * @param id       unique identifier (must not be null)
     * @param contact  contact information (must not be null)
     * @param company  company name (maybe null)
     * @param status   sales status (must be one of: NEW, QUALIFIED, CONVERTED)
     * @throws IllegalArgumentException if validation fails
     */
    public Lead(UUID id, Contact contact, String company, String status) {
        if (id == null) {
            throw new IllegalArgumentException("ID must not be null");
        }
        if (contact == null) {
            throw new IllegalArgumentException("Contact must not be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status must not be null");
        }
        if (!ALLOWED_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Status must be one of: NEW, QUALIFIED, CONVERTED");
        }

        this.id = id;
        this.contact = contact;
        this.company = company;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public Contact getContact() {
        return contact;
    }

    public String getCompany() {
        return company;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Lead lead = (Lead) o;
        return Objects.equals(id, lead.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}