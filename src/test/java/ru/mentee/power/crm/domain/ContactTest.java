package ru.mentee.power.crm.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContactTest {
    private final Address address1 = new Address("San Francisco", "123 Main St", "94105");
    private final Contact contact1 = new Contact(
            "test@example.com",
            "+71234567890",
            address1);
    private final Address address2 = new Address("San Francisco", "123 Main St", "91234");
    private final Contact contact2 = new Contact(
            "test2@example.com",
            "+71234567999",
            address2);

    @Test
    void shouldCreateContactWhenValidData() {
        assertThat(contact1.email()).isEqualTo("test@example.com");
        assertThat(contact1.phone()).isEqualTo("+71234567890");
        assertThat(contact1.address()).isEqualTo(address1);
        assertThat(contact1.address().city()).isEqualTo("San Francisco");
    }

    @Test
    void shouldBeEqualWhenSameData() {
        assertThat(contact1).isEqualTo(contact1);
        assertThat(contact1).hasSameHashCodeAs(contact1);
    }

    @Test
    void shouldNotBeEqualWhenDifferentData() {
        assertThat(contact1).isNotEqualTo(contact2);
    }

    @Test
    void shouldDelegateToAddressWhenAccessingCity() {
        assertThat(contact1.address().city()).isEqualTo("San Francisco");
        assertThat(contact1.address().street()).isEqualTo("123 Main St");
    }

    @Test
    void shouldThrowExceptionWhenAddressIsNull() {
        assertThatThrownBy(() -> new Contact(
                "test2@example.com",
                "+71234567890",
                null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
