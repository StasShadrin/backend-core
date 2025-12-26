package ru.mentee.power.crm.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ContactTest {

    @Test
    void shouldCreateContactWhenValidData() {
        Contact contact = new Contact("John", "Doe", "john@example.com");
        assertThat(contact.firstName()).isEqualTo("John");
        assertThat(contact.lastName()).isEqualTo("Doe");
        assertThat(contact.email()).isEqualTo("john@example.com");
    }

    @Test
    void shouldBeEqualWhenSameData() {
        Contact contact1 = new Contact("John", "Doe", "john@example.com");
        Contact contact2 = new Contact("John", "Doe", "john@example.com");
        assertThat(contact1).isEqualTo(contact2);
        assertThat(contact1).hasSameHashCodeAs(contact2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentData() {
        Contact contact1 = new Contact("John", "Doe", "john@example.com");
        Contact contact2 = new Contact("John", "Smith", "john@example.com");
        assertThat(contact1).isNotEqualTo(contact2);
    }
}