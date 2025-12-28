package ru.mentee.power.crm.domain;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LeadTest {
    private static Address createAddress(String city) {
        return new Address(city, "123 Main St", "94105");
    }

    private static Contact createContact(String email, String phone, String city) {
        return new Contact(email, phone, createAddress(city));
    }

    private static Lead createLead(String email, String phone, String city, String company, String status) {
        return new Lead(UUID.randomUUID(), createContact(email, phone, city), company, status);
    }

    @Test
    void shouldCreateLeadWhenValidData() {
        Address address = createAddress("San Francisco");
        Contact contact = new Contact("test@example.com", "+71234567890", address);
        Lead lead = new Lead(UUID.randomUUID(), contact, "TechCorp", "NEW");

        assertThat(lead.contact()).isEqualTo(contact);
        assertThat(lead.company()).isEqualTo("TechCorp");
        assertThat(lead.status()).isEqualTo("NEW");
    }

    @Test
    void shouldAccessEmailThroughDelegationWhenLeadCreated() {
        Lead lead = createLead(
                "test@example.com",
                "+71234567890",
                "San Francisco",
                "TechCorp",
                "NEW");

        assertThat(lead.contact().email()).isEqualTo("test@example.com");
        assertThat(lead.contact().address().city()).isEqualTo("San Francisco");
    }

    @Test
    void shouldBeEqualWhenAllFieldsAreEqual() {
        UUID id = UUID.randomUUID();
        Address address = createAddress("San Francisco");
        Contact contact = new Contact("same@test.com", "+7000", address);

        Lead lead1 = new Lead(id, contact, "SameCorp", "NEW");
        Lead lead2 = new Lead(id, contact, "SameCorp", "NEW");

        assertThat(lead1).isEqualTo(lead2);
        assertThat(lead1).hasSameHashCodeAs(lead2);
    }

    @Test
    void shouldNotBeEqualWhenAnyFieldDiffers() {
        Lead lead1 = createLead("a@test.com", "+71", "SF", "Corp", "NEW");
        Lead lead2 = createLead("b@test.com", "+71", "SF", "Corp", "NEW");

        assertThat(lead1).isNotEqualTo(lead2);
    }

    @Test
    void shouldThrowExceptionWhenContactIsNull() {
        assertThatThrownBy(this::createLeadWithNullContact)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Contact must not be null");
    }

    private void createLeadWithNullContact() {
        new Lead(
                UUID.randomUUID(),
                null,
                "Company",
                "NEW"
        );
    }

    @Test
    void shouldThrowExceptionWhenInvalidStatus() {
        assertThatThrownBy(this::createLeadWithInvalidStatus)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Status must be one of");
    }

    private void createLeadWithInvalidStatus() {
        new Lead(
                UUID.randomUUID(),
                createContact("test@test.com", "+7123", "SF"),
                "Company",
                "INVALID"
        );
    }

    @Test
    void shouldDemonstrateThreeLevelCompositionWhenAccessingCity() {
        Lead lead = createLead(
                "test@example.com",
                "+71234567890",
                "San Francisco",
                "TechCorp",
                "NEW");

        Contact contact = lead.contact();
        Address address = contact.address();
        String cityStep = address.city();
        assertThat(cityStep).isEqualTo("San Francisco");
    }
}