package ru.mentee.power.crm.repository;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import ru.mentee.power.crm.model.Lead;

class LeadRepositoryTest {
    private LeadRepository repository;

    @BeforeEach
    void setUp() {
        repository = new LeadRepository();
    }

    @Test
    void shouldSaveAndFindLeadByIdWhenLeadSaved() {
        // Given - создать Lead
        String id = "lead-1";
        Lead lead = new Lead(id,
                "test@example.com",
                "+7123",
                "TechCorp",
                "NEW");

        // When - сохранить через repository.save()
        repository.save(lead);
        Lead found = repository.findById(id);

        // Then - найти через findById(), assertThat().isNotNull()
        assertThat(found).isNotNull();
        assertThat(found.id()).isEqualTo(id);
        assertThat(found.email()).isEqualTo("test@example.com");
    }

    @Test
    void shouldReturnNullWhenLeadNotFound() {
        // Given - пустой repository
        String unknownId = "unknown-id";

        // When - вызвать findById("unknown-id")
        Lead found = repository.findById(unknownId);

        // Then - assertThat().isNull()
        assertThat(found).isNull();
    }

    @Test
    void shouldReturnAllLeadsWhenMultipleLeadsSaved() {
        // Given - сохранить 3 лида
        Lead lead1 = new Lead("lead-1", "a@test.com", "+1", "A", "NEW");
        Lead lead2 = new Lead("lead-2", "b@test.com", "+2", "B", "QUALIFIED");
        Lead lead3 = new Lead("lead-3", "c@test.com", "+3", "C", "CONVERTED");
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
        String id = "lead-1";
        Lead lead = new Lead(id, "test@test.com", "+7000", "Test", "NEW");
        repository.save(lead);

        // When - удалить через delete(id)
        repository.delete(id);

        // Then - findById() вернет null, size() == 0
        assertThat(repository.findById(id)).isNull();
        assertThat(repository.size()).isZero();
    }

    @Test
    void shouldOverwriteLeadWhenSaveWithSameId() {
        // Given - сохранить Lead с id="lead-1"
        String id = "lead-1";
        Lead lead1 = new Lead(id, "first@test.com", "+1", "First", "NEW");
        repository.save(lead1);

        // When - сохранить другой Lead с id="lead-1" но другим email
        Lead lead2 = new Lead(id, "second@test.com", "+2", "Second", "QUALIFIED");
        repository.save(lead2);
        Lead found = repository.findById(id);

        // Then - findById("lead-1") вернет второй Lead, size() == 1
        assertThat(found).isNotNull();
        assertThat(found.email()).isEqualTo("second@test.com");
        assertThat(repository.size()).isEqualTo(1);
    }

    @Test
    void shouldFindFasterWithMapThanWithListFilter() {
        // Given: Создать 1000 лидов
        List<Lead> leadList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Lead lead = new Lead("lead-" + i,
                    "test" + i + "@example.com",
                    "+7123" + i,
                    "TechCorp" + i,
                    "NEW");
            repository.save(lead);
            leadList.add(lead);
        }

        String targetId = "lead-500";  // Средний элемент

        // When: Поиск через Map
        long mapStart = System.nanoTime();
        Lead foundInMap = repository.findById(targetId);
        long mapDuration = System.nanoTime() - mapStart;

        // When: Поиск через List.stream().filter()
        long listStart = System.nanoTime();
        Lead foundInList = leadList.stream()
                .filter(lead -> lead.id().equals(targetId))
                .findFirst()
                .orElse(null);
        long listDuration = System.nanoTime() - listStart;

        // Then: Map должен быть минимум в 10 раз быстрее
        assertThat(foundInMap).isEqualTo(foundInList);
        assertThat(listDuration).isGreaterThan(mapDuration * 10);

        System.out.println("Map поиск: " + mapDuration + " ns");
        System.out.println("List поиск: " + listDuration + " ns");
        System.out.println("Ускорение: " + (listDuration / mapDuration) + "x");
    }

    @Test
    void shouldSaveBothLeadsEvenWithSameEmailAndPhoneBecauseRepositoryDoesNotCheckBusinessRules() {
        // Given: два лида с разными ID, но одинаковыми контактами
        Lead originalLead = new Lead(
                "lead-1",
                "ivan@mail.ru",
                "+79001234567",
                "Acme Corp",
                "NEW");
        Lead duplicateLead = new Lead(
                "lead-2",
                "ivan@mail.ru",
                "+79001234567",
                "TechCorp",
                "HOT");

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