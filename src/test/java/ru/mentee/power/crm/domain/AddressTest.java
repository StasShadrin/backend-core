package ru.mentee.power.crm.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AddressTest {

    @Test
    void shouldCreateAddressWhenValidData() {
        Address address = new Address("San Francisco", "123 Main St", "94105");

        assertThat(address.city()).isEqualTo("San Francisco");
        assertThat(address.street()).isEqualTo("123 Main St");
        assertThat(address.zip()).isEqualTo("94105");
    }

    @Test
    void shouldBeEqualWhenSameData() {
        Address address1 = new Address("San Francisco", "123 Main St", "94105");
        Address address2 = new Address("San Francisco", "123 Main St", "94105");
        assertThat(address1).isEqualTo(address2);
        assertThat(address1).hasSameHashCodeAs(address2);
    }

    @Test
    void shouldThrowExceptionWhenCityIsNull() {
        assertThatThrownBy(() -> new Address(null, "123 Main St", "94105"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("City must not be null or empty");
    }

    @Test
    void shouldThrowExceptionWhenZipIsBlank() {
        assertThatThrownBy(() -> new Address("San Francisco", "123 Main St", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Zip must not be null or empty");
    }
}