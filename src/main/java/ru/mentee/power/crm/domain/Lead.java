package ru.mentee.power.crm.domain;

import java.util.Set;
import java.util.UUID;

/**
 * Represents a potential customer (lead) in the CRM system.
 * <p>
 * A lead contains contact information, company name, and current status
 * in the sales pipeline. The status must be one of the predefined values.
 *
 * @param id       unique identifier; must not be {@code null}
 * @param contact  contact information; must not be {@code null}
 * @param company  company name (maybe {@code null} or empty)
 * @param status   current sales status; must be one of:
 *                 {@code "NEW"}, {@code "QUALIFIED"}, {@code "CONVERTED"} (case-sensitive)
 */
public record Lead(UUID id, Contact contact, String company, String status) {

    private static final Set<String> ALLOWED_STATUSES = Set.of("NEW", "QUALIFIED", "CONVERTED");

    /**
     * Compact constructor that validates the components of the {@code Lead} record.
     * <p>
     * Performs the following validations:
     * <ul>
     *   <li>{@code id} must not be {@code null}</li>
     *   <li>{@code contact} must not be {@code null}</li>
     *   <li>{@code status} must not be {@code null}</li>
     *   <li>{@code status} must be one of: {@code "NEW"}, {@code "QUALIFIED"}, {@code "CONVERTED"}</li>
     * </ul>
     * <p>
     * The {@code company} field is not validated and may be {@code null} or empty.
     *
     * @throws IllegalArgumentException if any validation rule is violated
     */
    public Lead {
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
    }
}