package ru.mentee.power.crm.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.spring.repository.InMemoryLeadRepository;

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
    Lead lead =
        Lead.builder()
            .id(id)
            .email("test@example.com")
            .phone("+7123")
            .company("TechCorp")
            .status(LeadStatus.NEW)
            .build();

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
    Lead lead1 =
        Lead.builder()
            .id(UUID.randomUUID())
            .email("a@test.com")
            .phone("+1")
            .company("A")
            .status(LeadStatus.NEW)
            .build();
    Lead lead2 =
        Lead.builder()
            .id(UUID.randomUUID())
            .email("b@test.com")
            .phone("+2")
            .company("B")
            .status(LeadStatus.QUALIFIED)
            .build();
    Lead lead3 =
        Lead.builder()
            .id(UUID.randomUUID())
            .email("c@test.com")
            .phone("+3")
            .company("C")
            .status(LeadStatus.CONTACTED)
            .build();
    repository.save(lead1);
    repository.save(lead2);
    repository.save(lead3);

    // When - вызвать findAll()
    List<Lead> allLeads = repository.findAll();

    // Then - assertThat().hasSize(3)
    assertThat(allLeads).hasSize(3).containsExactlyInAnyOrder(lead1, lead2, lead3);
  }

  @Test
  void shouldDeleteByIdLeadWhenLeadExists() {
    // Given - сохранить лид
    UUID id = UUID.randomUUID();
    Lead lead =
        Lead.builder()
            .id(id)
            .email("test@test.com")
            .phone("+7000")
            .company("Test")
            .status(LeadStatus.NEW)
            .build();
    repository.save(lead);

    // When - удалить через delete(id)
    repository.deleteById(id);

    // Then - findById() вернет null, size() == 0
    assertThat(repository.findById(id)).isEmpty();
    assertThat(repository.size()).isZero();
  }

  @Test
  void shouldOverwriteLeadWhenSaveWithSameId() {
    // Given - сохранить Lead с id="lead-1"
    UUID id = UUID.randomUUID();
    Lead lead1 =
        Lead.builder()
            .id(id)
            .email("first@test.com")
            .phone("+1")
            .company("First")
            .status(LeadStatus.NEW)
            .build();
    repository.save(lead1);

    // When - сохранить другой Lead с id="lead-1" но другим email
    Lead lead2 =
        Lead.builder()
            .id(id)
            .email("second@test.com")
            .phone("+2")
            .company("Second")
            .status(LeadStatus.QUALIFIED)
            .build();
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
      Lead lead =
          Lead.builder()
              .id(UUID.randomUUID())
              .email("test" + i + "@example.com")
              .phone("+7123" + i)
              .company("TechCorp" + i)
              .status(LeadStatus.NEW)
              .build();
      repository.save(lead);
      leadList.add(lead);
    }

    UUID targetId = leadList.get(500).id(); // Средний элемент

    // When: Поиск через Map
    long mapStart = System.nanoTime();
    Optional<Lead> foundInMap = repository.findById(targetId);
    long mapDuration = System.nanoTime() - mapStart;

    // When: Поиск через List.stream().filter()
    long listStart = System.nanoTime();
    Lead foundInList =
        leadList.stream().filter(lead -> lead.id().equals(targetId)).findFirst().orElse(null);
    long listDuration = System.nanoTime() - listStart;

    // Then: Map должен быть минимум в 10 раз быстрее
    assertThat(foundInMap).isPresent().get().isEqualTo(foundInList);
    assertThat(listDuration).isGreaterThan(mapDuration * 10);

    System.out.println("Map поиск: " + mapDuration + " ns");
    System.out.println("List поиск: " + listDuration + " ns");
    System.out.println("Ускорение: " + (listDuration / mapDuration) + "x");
  }

  @Test
  void shouldSaveBothLeadsEvenWithSameEmailAndPhoneBecauseRepositoryDoesNotCheckBusinessRules() {
    // Given: два лида с разными ID, но одинаковыми контактами
    Lead originalLead =
        Lead.builder()
            .id(UUID.randomUUID())
            .email("ivan@mail.ru")
            .phone("+79001234567")
            .company("Acme Corp")
            .status(LeadStatus.NEW)
            .build();
    Lead duplicateLead =
        Lead.builder()
            .id(UUID.randomUUID())
            .email("ivan@mail.ru")
            .phone("+79001234567")
            .company("TechCorp")
            .status(LeadStatus.NEW)
            .build();

    // When: сохраняем оба
    repository.save(originalLead);
    repository.save(duplicateLead);

    // Then
    assertThat(repository.size()).isEqualTo(2);
  }
}
