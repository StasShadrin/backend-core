package ru.mentee.power.crm.domain;

/**
 * Represents a postal address with city, street, and ZIP code.
 * <p>
 * This record is immutable and validates that essential fields (city and ZIP code)
 * are not null or blank upon construction.
 * <p>
 * The street field is optional and may be {@code null} or empty.
 *
 * @param city  the city name; must not be {@code null} or blank
 * @param street the street name and number; may be {@code null} or empty
 * @param zip   the postal (ZIP) code; must not be {@code null} or blank
 */
public record Address(String city, String street, String zip) {

    /**
     * Compact constructor that validates the {@code city} and {@code zip} fields.
     * <p>
     * Validation rules:
     * <ul>
     *   <li>{@code city} must not be {@code null} and must contain at least one non-whitespace character</li>
     *   <li>{@code zip} must not be {@code null} and must contain at least one non-whitespace character</li>
     * </ul>
     * <p>
     * The {@code street} field is not validated and may be {@code null} or blank.
     *
     * @throws IllegalArgumentException if {@code city} or {@code zip} is {@code null} or blank
     */
    public Address {
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City must not be null or empty");
        }
        if (zip == null || zip.isBlank()) {
            throw new IllegalArgumentException("Zip must not be null or empty");
        }
    }
}