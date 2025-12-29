package ru.mentee.power.crm.domain;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CustomerTest {

    @Test
    void shouldReuseContactWhenCreatingCustomer() {
        // Given: один и тот же контакт, но два разных адреса
        Address contactAddress = new Address("San Francisco", "123 Main St", "94105");
        Address billingAddress = new Address("Los Angeles", "456 Oak Ave", "90001");

        Contact contact = new Contact("customer@example.com", "+71234567890", contactAddress);

        // When: создаём Customer с этим контактом и другим billingAddress
        Customer customer = new Customer(
                UUID.randomUUID(),
                contact,
                billingAddress,
                "SILVER"
        );

        // Then: адрес из контакта и billingAddress — это разные объекты
        assertThat(customer.contact().address())
                .isNotSameAs(customer.billingAddress())
                .isNotEqualTo(customer.billingAddress());

        assertThat(customer.contact().email()).isEqualTo("customer@example.com");
        assertThat(customer.contact().address().city()).isEqualTo("San Francisco");
        assertThat(customer.billingAddress().city()).isEqualTo("Los Angeles");
    }

    @Test
    void shouldDemonstrateContactReuseAcrossLeadAndCustomer() {
        // Given: один и тот же Contact используется и в Lead, и в Customer
        Address sharedAddress = new Address("Seattle", "789 Pine St", "98101");
        Contact sharedContact = new Contact("shared@example.com", "+79876543210", sharedAddress);

        Lead lead = new Lead(
                UUID.randomUUID(),
                sharedContact,
                "Startup Inc",
                "QUALIFIED"
        );

        Customer customer = new Customer(
                UUID.randomUUID(),
                sharedContact,
                sharedAddress,
                "GOLD"
        );

        // Then: оба объекта используют один и тот же Contact (по ссылке или по содержимому)
        assertThat(lead.getContact()).isEqualTo(customer.contact());
        assertThat(lead.getContact().email()).isEqualTo("shared@example.com");

    }
}