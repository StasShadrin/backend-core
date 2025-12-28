package ru.mentee.power.crm.domain;

import java.util.Set;
import java.util.UUID;

/**
 * Represents a customer in the CRM system.
 * <p>
 * A customer is defined by a unique identifier, contact information,
 * billing address, and a loyalty tier.
 * <p>
 * The loyalty tier must be one of the following values:
 * {@code "BRONZE"}, {@code "SILVER"}, or {@code "GOLD"}.
 *
 * @param id             unique identifier of the customer; must not be {@code null}
 * @param contact        customer's contact information; must not be {@code null}
 * @param billingAddress postal address used for billing; must not be {@code null}
 * @param loyaltyTier    customer's loyalty level; must be one of:
 *                       {@code "BRONZE"}, {@code "SILVER"}, {@code "GOLD"} (case-sensitive)
 */
public record Customer(UUID id, Contact contact, Address billingAddress, String loyaltyTier) {

    private static final Set<String> LOYALTY_LEVELS = Set.of("BRONZE", "SILVER", "GOLD");

    /**
     * Compact constructor for validating the components of the {@code Customer} record.
     * <p>
     * Performs the following validations:
     * <ul>
     *   <li>{@code id} must not be {@code null}</li>
     *   <li>{@code contact} must not be {@code null}</li>
     *   <li>{@code billingAddress} must not be {@code null}</li>
     *   <li>{@code loyaltyTier} must not be {@code null}</li>
     *   <li>{@code loyaltyTier} must be one of: {@code "BRONZE"}, {@code "SILVER"}, {@code "GOLD"}</li>
     * </ul>
     *
     * @throws IllegalArgumentException if any component fails validation
     */
    public Customer {
        if (id == null) {
            throw new IllegalArgumentException("ID must not be null");
        }
        if (contact == null) {
            throw new IllegalArgumentException("Contact must not be null");
        }
        if (billingAddress == null) {
            throw new IllegalArgumentException("Billing Address must not be null");
        }
        if (loyaltyTier == null) {
            throw new IllegalArgumentException("Loyalty Tier must not be null");
        }
        if (!LOYALTY_LEVELS.contains(loyaltyTier)) {
            throw new IllegalArgumentException("Loyalty Tier must be one of: BRONZE, SILVER, GOLD");
        }
    }
}