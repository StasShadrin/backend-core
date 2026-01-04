package ru.mentee.power.crm.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ru.mentee.power.crm.domain.Address;
import ru.mentee.power.crm.domain.Contact;
import ru.mentee.power.crm.domain.Lead;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LeadRepositoryTest {
    private static Address createAddress() {
        return new Address("San Francisco", "123 Main St", "94105");
    }

    private static Contact createContact(String email, String phone) {
        return new Contact(email, phone, createAddress());
    }

    private static Lead createLead(String email, String phone, String company) {
        return new Lead(UUID.randomUUID(), createContact(email, phone), company, "NEW");
    }

    @Test
    @DisplayName("Should automatically deduplicate leads by id")
    void shouldDeduplicateLeadsById() {
        // Given - создать LeadRepository и лид с UUID
        LeadRepository leadRepository = new LeadRepository();
        Lead firstLead = createLead("test@example.com", "+71234567890", "TechCorp");

        // When - добавить лид дважды через add
        leadRepository.add(firstLead);
        leadRepository.add(firstLead);

        // Then - проверить что size == 1, второй add вернул false
        assertThat(leadRepository.size()).isEqualTo(1);
        assertThat(leadRepository.add(firstLead)).isFalse();
    }

    @Test
    @DisplayName("Should allow different leads with different ids")
    void shouldAllowDifferentLeads() {
        // Given - создать два Lead с разными UUID
        LeadRepository leadRepository = new LeadRepository();
        Lead lead1 = new Lead(UUID.randomUUID(),
                createContact("john1@example.com",
                        "+71234567899"),
                "Corp1",
                "NEW");
        Lead lead2 = new Lead(UUID.randomUUID(),
                createContact("john2@example.com",
                        "+71234567890"),
                "Corp2",
                "QUALIFIED");

        // When - добавить оба лида
        boolean added1 = leadRepository.add(lead1);
        boolean added2 = leadRepository.add(lead2);

        // Then - проверить что size == 2, оба add вернули true
        assertThat(leadRepository.size()).isEqualTo(2);
        assertThat(added1).isTrue();
        assertThat(added2).isTrue();
    }

    @Test
    @DisplayName("Should find existing lead through contains")
    void shouldFindExistingLead() {
        // Given - добавить Лид в репозиторий
        LeadRepository leadRepository = new LeadRepository();
        Lead firstLead = createLead("test@example.com", "+71234567890", "TechCorp");
        leadRepository.add(firstLead);

        // When - вызвать contains с тем же лидом
        leadRepository.contains(firstLead);

        // Then - проверить что contains вернул true
        assertThat(leadRepository.contains(firstLead)).isTrue();
    }

    @Test
    @DisplayName("Should return unmodifiable set from findAll")
    void shouldReturnUnmodifiableSet() {
        // Given - добавить Лид в репозиторий
        LeadRepository leadRepository = new LeadRepository();
        Lead firstLead = createLead("test@example.com", "+71234567890", "TechCorp");
        leadRepository.add(firstLead);

        // When - вызвать findAll и попытаться изменить результат
        Set<Lead> result = leadRepository.findAll();

        // Then - проверить что выбрасывается UnsupportedOperationException
        assertThatThrownBy(() -> result.add(createLead("new@test.com", "+7999", "New")))
                .isInstanceOf(UnsupportedOperationException.class);

    }

    @Test
    @DisplayName("Should perform contains() faster than ArrayList")
    void shouldPerformFasterThanArrayList() {
        // Given - создать HashSet и ArrayList с 10000 одинаковых лидов
        int size = 10000;
        Set<Lead> hashSet = new HashSet<>();
        List<Lead> arrayList = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            Lead lead = createLead("lead" + i + "@test.com", "+7" + i, "Company" + i);

            hashSet.add(lead);
            arrayList.add(lead);
        }

        Lead lead = arrayList.get(5000);

        // When - выполнить contains() 1000 раз на каждой коллекции
        long hashSetStart = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            hashSet.contains(lead);
        }
        long hashSetDuration = System.nanoTime() - hashSetStart;

        long arrayListStart = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            arrayList.contains(lead);
        }
        long arrayListDuration = System.nanoTime() - arrayListStart;

        // Then - замерить время, HashSet быстрее минимум в 100 раз
        assertThat(arrayListDuration).isGreaterThan(hashSetDuration * 100);
    }
}