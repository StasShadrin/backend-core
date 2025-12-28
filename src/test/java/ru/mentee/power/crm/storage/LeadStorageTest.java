package ru.mentee.power.crm.storage;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ru.mentee.power.crm.domain.Address;
import ru.mentee.power.crm.domain.Contact;
import ru.mentee.power.crm.domain.Lead;

class LeadStorageTest {

    // Вспомогательные данные для переиспользования
    private static Address createAddress(String city) {
        return new Address(city, "123 Main St", "94105");
    }

    private static Contact createContact(String email, String phone) {
        return new Contact(email, phone, createAddress("San Francisco"));
    }

    private static Lead createLead(String email, String phone, String company) {
        return new Lead(UUID.randomUUID(), createContact(email, phone), company, "NEW");
    }

    @Test
    void shouldAddLeadWhenLeadIsUnique() {
        // Given
        LeadStorage storage = new LeadStorage();
        Lead uniqueLead = createLead("test@example.com", "+71234567890", "TechCorp");

        // When
        boolean added = storage.add(uniqueLead);

        // Then
        assertThat(added).isTrue();
        assertThat(storage.size()).isEqualTo(1);
        assertThat(storage.findAll()).containsExactly(uniqueLead);
    }

    @Test
    void shouldRejectDuplicateWhenEmailAlreadyExists() {
        // Given
        LeadStorage storage = new LeadStorage();
        Lead existingLead = createLead("ivan@mail.ru", "+7123", "TechCorp");
        Lead duplicateLead = createLead("ivan@mail.ru", "+7456", "Other");
        storage.add(existingLead);

        // When
        boolean added = storage.add(duplicateLead);

        // Then
        assertThat(added).isFalse();
        assertThat(storage.size()).isEqualTo(1);
        assertThat(storage.findAll()).containsExactly(existingLead);
    }

    @Test
    void shouldThrowExceptionWhenStorageIsFull() {
        // Given: Заполни хранилище 100 лидами
        LeadStorage storage = new LeadStorage();
        for (int index = 0; index < 100; index++) {
            storage.add(createLead("lead" + index + "@mail.ru", "+7000", "Company"));
        }

        // When + Then: 101-й лид должен выбросить исключение
        Lead hundredFirstLead = createLead("lead101@mail.ru", "+7001", "Company");

        assertThatThrownBy(() -> storage.add(hundredFirstLead))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Storage is full");
    }

    @Test
    void shouldReturnOnlyAddedLeadsWhenFindAllCalled() {
        // Given
        LeadStorage storage = new LeadStorage();
        Lead firstLead = createLead("ivan@mail.ru", "+7123", "TechCorp");
        Lead secondLead = createLead("maria@startup.io", "+7456", "StartupLab");
        storage.add(firstLead);
        storage.add(secondLead);

        // When
        Lead[] result = storage.findAll();

        // Then
        assertThat(result)
                .hasSize(2)
                .containsExactly(firstLead, secondLead);
    }

    @Test
    void shouldThrowOnNullLead() {
        LeadStorage storage = new LeadStorage();
        assertThatThrownBy(() -> storage.add(null))
                .isInstanceOf(NullPointerException.class);
    }
}