package ru.mentee.power.crm.domain;

import java.util.Objects;

/**
 * Represents a person's contact information.
 * <p>
 * A contact includes first name, last name, and email address.
 */
public class Contact {
    private String firstName;
    private String lastName;
    private String email;

    /***/
    public Contact(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Contact contact = (Contact) o;
        return Objects.equals(firstName, contact.firstName)
               && Objects.equals(lastName, contact.lastName)
               && Objects.equals(email, contact.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, email);
    }

    @Override
    public String toString() {
        return "Contact{" +
               "firstName='" + firstName + '\'' +
               ", lastName='" + lastName + '\'' +
               ", email='" + email + '\'' +
               '}';
    }
}
