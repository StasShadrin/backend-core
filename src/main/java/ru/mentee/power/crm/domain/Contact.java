package ru.mentee.power.crm.domain;

/**
 * Represents a person's contact information.
 * <p>
 * A contact includes first name, last name, and email address.
 */
public record Contact(String firstName, String lastName, String email) {
}
