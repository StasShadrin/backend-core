package ru.mentee.power.crm.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.model.LeadStatus;

class InMemoryLeadRepositoryTest {
    private InMemoryLeadRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryLeadRepository();
    }

    @Test
    void shouldSaveAndFindLeadByIdWhenLeadSaved() {
        // Given - создать Lead
        UUID id = UUID.randomUUID();
        Lead lead = new Lead(id,
                "test@example.com",
                "+7123",
                "TechCorp",
                LeadStatus.NEW);

        // When - сохранить через repository.save()
        repository.save(lead);
        Optional<Lead> found = repository.findById(id);

        // Then - найти через findById(), assertThat().isNotNull()
        assertThat(found).isPresent();
        assertThat(found.get().id()).isEqualTo(id);
        assertThat(found.get().email()).isEqualTo("test@example.com");
    }

    @Test
    void shouldReturnNullWhenLeadNotFound() {
        // Given - пустой repository
        UUID unknownId = UUID.randomUUID();

        // When - вызвать findById("unknown-id")
        Optional<Lead> found = repository.findById(unknownId);

        // Then - assertThat().isNull()
        assertThat(found).isEmpty();
    }

    @Test
    void shouldReturnAllLeadsWhenMultipleLeadsSaved() {
        // Given - сохранить 3 лида
        Lead lead1 = new Lead(UUID.randomUUID(), "a@test.com", "+1", "A", LeadStatus.NEW);
        Lead lead2 = new Lead(UUID.randomUUID(), "b@test.com", "+2", "B", LeadStatus.QUALIFIED);
        Lead lead3 = new Lead(UUID.randomUUID(), "c@test.com", "+3", "C", LeadStatus.CONTACTED);
        repository.save(lead1);
        repository.save(lead2);
        repository.save(lead3);

        // When - вызвать findAll()
        List<Lead> allLeads = repository.findAll();

        // Then - assertThat().hasSize(3)
        assertThat(allLeads).hasSize(3)
                .containsExactlyInAnyOrder(lead1, lead2, lead3);
    }

    @Test
    void shouldDeleteLeadWhenLeadExists() {
        // Given - сохранить лид
        UUID id = UUID.randomUUID();
        Lead lead = new Lead(id, "test@test.com", "+7000", "Test", LeadStatus.NEW);
        repository.save(lead);

        // When - удалить через delete(id)
        repository.delete(id);

        // Then - findById() вернет null, size() == 0
        assertThat(repository.findById(id)).isEmpty();
        assertThat(repository.size()).isZero();
    }

    @Test
    void shouldOverwriteLeadWhenSaveWithSameId() {
        // Given - сохранить Lead с id="lead-1"
        UUID id = UUID.randomUUID();
        Lead lead1 = new Lead(id, "first@test.com", "+1", "First", LeadStatus.NEW);
        repository.save(lead1);

        // When - сохранить другой Lead с id="lead-1" но другим email
        Lead lead2 = new Lead(id, "second@test.com", "+2", "Second", LeadStatus.QUALIFIED);
        repository.save(lead2);
        Optional<Lead> found = repository.findById(id);

        // Then - findById("lead-1") вернет второй Lead, size() == 1
        assertThat(found).isPresent();
        assertThat(found.get().email()).isEqualTo("second@test.com");
        assertThat(repository.size()).isEqualTo(1);
    }

    @Test
    void shouldFindFasterWithMapThanWithListFilter() {
        // Given: Создать 1000 лидов
        List<Lead> leadList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Lead lead = new Lead(UUID.randomUUID(),
                    "test" + i + "@example.com",
                    "+7123" + i,
                    "TechCorp" + i,
                    LeadStatus.NEW);
            repository.save(lead);
            leadList.add(lead);
        }

        UUID targetId = leadList.get(500).id();  // Средний элемент

        // When: Поиск через Map
        long mapStart = System.nanoTime();
        Optional<Lead> foundInMap = repository.findById(targetId);
        long mapDuration = System.nanoTime() - mapStart;

        // When: Поиск через List.stream().filter()
        long listStart = System.nanoTime();
        Lead foundInList = leadList.stream()
                .filter(lead -> lead.id().equals(targetId))
                .findFirst()
                .orElse(null);
        long listDuration = System.nanoTime() - listStart;

        // Then: Map должен быть минимум в 10 раз быстрее
        assertThat(foundInMap)
                .isPresent()
                .get()
                .isEqualTo(foundInList);
        assertThat(listDuration).isGreaterThan(mapDuration * 10);

        System.out.println("Map поиск: " + mapDuration + " ns");
        System.out.println("List поиск: " + listDuration + " ns");
        System.out.println("Ускорение: " + (listDuration / mapDuration) + "x");
    }

    @Test
    void shouldSaveBothLeadsEvenWithSameEmailAndPhoneBecauseRepositoryDoesNotCheckBusinessRules() {
        // Given: два лида с разными ID, но одинаковыми контактами
        Lead originalLead = new Lead(
                UUID.randomUUID(),
                "ivan@mail.ru",
                "+79001234567",
                "Acme Corp",
                LeadStatus.NEW);
        Lead duplicateLead = new Lead(
                UUID.randomUUID(),
                "ivan@mail.ru",
                "+79001234567",
                "TechCorp",
                LeadStatus.NEW);

        // When: сохраняем оба
        repository.save(originalLead);
        repository.save(duplicateLead);

        // Then: Repository сохранил оба (это технически правильно!)
        assertThat(repository.size()).isEqualTo(2);

        // But: Бизнес недоволен — в CRM два контакта на одного человека
        // Решение: Service Layer в Sprint 5 будет проверять бизнес-правила
        // перед вызовом repository.save()
    }
}