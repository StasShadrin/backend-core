package ru.mentee.power.crm.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import ru.mentee.power.crm.domain.Address;
import ru.mentee.power.crm.domain.Contact;
import ru.mentee.power.crm.domain.Lead;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryLeadRepositoryTest {

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
    void shouldReturnSingleLeadInListWhenAddUniqueLeadToEmptyRepository() {
        // Given: пустой InMemoryLeadRepository
        InMemoryLeadRepository repository = new InMemoryLeadRepository();
        Lead firstLead = createLead("test@example.com", "+71234567890", "TechCorp");

        // When: добавляю лида firstLead через add(firstLead)
        repository.add(firstLead);

        // Then: findAll() возвращает список из одного элемента,
        //       findById(firstLead.getId()) возвращает Optional.of(firstLead)
        assertThat(repository.findAll())
                .hasSize(1)
                .containsExactly(firstLead);

        assertThat(repository.findById(firstLead.getId()))
                .isPresent()
                .get()
                .isEqualTo(firstLead);
    }

    @Test
    void shouldReturnEmptyOptionalWhenFindByIdWithNonExistentUuid() {
        // Given: InMemoryLeadRepository с 10 лидами (id от 1 до 10)
        InMemoryLeadRepository repository = new InMemoryLeadRepository();
        for (int i = 0; i < 10; i++) {
            repository.add(createLead("lead" + i + "@test.com", "+71234" + i, "Company" + i));
        }

        // When: вызываю findById для UUID несуществующего лида
        Optional<Lead> byId = repository.findById(UUID.randomUUID());

        // Then: получаю Optional.empty()
        assertThat(byId).isEmpty();
    }

    @Test
    void shouldRejectDuplicateLeadAndKeepSizeOneWhenAddLeadWithSameIdAsExisting() {
        // Given: InMemoryLeadRepository с лидом john@example.com (UUID: abc-123)
        InMemoryLeadRepository repository = new InMemoryLeadRepository();
        UUID sharedId = UUID.randomUUID();
        Lead lead1 = new Lead(sharedId,
                createContact("john1@example.com",
                        "+71234567899"),
                "Corp1",
                "NEW");
        Lead lead2 = new Lead(sharedId,
                createContact("john2@example.com",
                        "+71234567890"),
                "Corp2",
                "QUALIFIED");
        repository.add(lead1);

        // When: пытаюсь добавить второго лида с тем же UUID через add()
        repository.add(lead2);

        // Then: дубликат отклонен через contains() проверку, size() остается 1
        assertThat(repository.findAll())
                .hasSize(1)
                .extracting(Lead::getId)
                .containsExactly(sharedId);
    }

    @Test
    void shouldRemoveLeadAndReturnEmptyOptionalWhenRemoveExistingLeadById() {
        // Given: InMemoryLeadRepository с 5 лидами
        InMemoryLeadRepository repository = new InMemoryLeadRepository();
        Lead leadToRemove = createLead("test@test.com", "+7000", "TestCompany");
        UUID idToRemove = leadToRemove.getId();
        repository.add(leadToRemove);
        for (int i = 0; i < 4; i++) {
            repository.add(createLead("lead" + i + "@test.com", "+71234" + i, "Company" + i));
        }

        // When: вызываю remove(uuid) для существующего лида
        repository.remove(idToRemove);

        // Then: findAll() возвращает 4 лида, findById(uuid) возвращает Optional.empty()
        assertThat(repository.findAll()).hasSize(4);

        assertThat(repository.findById(idToRemove)).isEmpty();
    }

    @Test
    void shouldNotAffectInternalStorageWhenClientModifiesListReturnedByFindAll() {
        // Given: InMemoryLeadRepository с лидами
        InMemoryLeadRepository repository = new InMemoryLeadRepository();
        Lead leadToRemove = createLead("test@test.com", "+7000", "TestCompany");
        repository.add(leadToRemove);

        // When: клиент вызывает findAll() и пытается изменить возвращенный список
        List<Lead> list = repository.findAll();
        list.clear();

        // Then: изменения не влияют на internal storage (defensive copy)
        assertThat(repository.findAll()).hasSize(1);
    }

}