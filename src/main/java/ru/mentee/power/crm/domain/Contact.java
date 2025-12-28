package ru.mentee.power.crm.domain;

/**
 * Represents a person's contact information.
 * <p>
 * A contact consists of an email address, a phone number, and a postal address.
 * All fields are mandatory and validated upon construction.
 *
 * @param email   the email address; must not be {@code null} or blank
 * @param phone   the phone number; must not be {@code null} or blank
 * @param address the postal address; must not be {@code null}
 */
public record Contact(String email, String phone, Address address) {

    /**
     * Compact constructor that validates all components of the contact.
     * <p>
     * Validation rules:
     * <ul>
     *   <li>{@code email} must not be {@code null} and must contain at least one non-whitespace character</li>
     *   <li>{@code phone} must not be {@code null} and must contain at least one non-whitespace character</li>
     *   <li>{@code address} must not be {@code null}</li>
     * </ul>
     * <p>
     * Note: The {@code Address} object itself is expected to be valid
     * (e.g., its {@code city} and {@code zip} are non-blank).
     *
     * @throws IllegalArgumentException if any field fails validation
     */
    public Contact {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be null or empty");
        }
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone must not be null or empty");
        }
        if (address == null) {
            throw new IllegalArgumentException("Address must not be null");
        }
    }
}