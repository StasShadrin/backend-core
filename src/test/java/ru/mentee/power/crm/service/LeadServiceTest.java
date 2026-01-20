package ru.mentee.power.crm.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.model.LeadBuilder;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.spring.repository.InMemoryLeadRepository;
import ru.mentee.power.crm.spring.service.LeadService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LeadServiceTest {

    private LeadService service;

    @BeforeEach
    void setUp() {
        var repository = new InMemoryLeadRepository();
        service = new LeadService(repository);

        service.addLead(LeadBuilder.builder()
                .name("Bob")
                .email("bob1@example.com")
                .phone("+123")
                .company("Google")
                .status(LeadStatus.NEW)
                .build());
        service.addLead(LeadBuilder.builder()
                .name("Bob")
                .email("bob2@example.com")
                .phone("+456")
                .company("Meta")
                .status(LeadStatus.NEW)
                .build());
        service.addLead(LeadBuilder.builder()
                .name("Alice")
                .email("alice@example.com")
                .phone("+789")
                .company("Apple")
                .status(LeadStatus.NEW)
                .build());

        service.addLead(LeadBuilder.builder()
                .name("John")
                .email("john1@example.com")
                .phone("+111")
                .company("Microsoft")
                .status(LeadStatus.CONTACTED)
                .build());
        service.addLead(LeadBuilder.builder()
                .name("John")
                .email("john2@example.com")
                .phone("+222")
                .company("Amazon")
                .status(LeadStatus.CONTACTED)
                .build());
        service.addLead(LeadBuilder.builder()
                .name("Sara")
                .email("sara@example.com")
                .phone("+333")
                .company("Netflix")
                .status(LeadStatus.CONTACTED)
                .build());
        service.addLead(LeadBuilder.builder()
                .name("Charlie")
                .email("charlie@example.com")
                .phone("+444")
                .company("Tesla")
                .status(LeadStatus.CONTACTED)
                .build());
        service.addLead(LeadBuilder.builder()
                .name("Diana")
                .email("diana@example.com")
                .phone("+555")
                .company("SpaceX")
                .status(LeadStatus.CONTACTED)
                .build());

        service.addLead(LeadBuilder.builder()
                .name("Satan")
                .email("satan@example.com")
                .phone("+666")
                .company("Evil")
                .status(LeadStatus.QUALIFIED)
                .build());
        service.addLead(LeadBuilder.builder()
                .name("Jim")
                .email("jim@example.com")
                .phone("+777")
                .company("Yandex")
                .status(LeadStatus.QUALIFIED)
                .build());
    }


    @Test
    void shouldCreateLeadWhenEmailIsUnique() {
        // Given
        String email = "newuser@example.com";

        // When
        Lead lead = service.addLead(LeadBuilder.builder()
                .name("Test")
                .email(email)
                .phone("+000")
                .company("Startup")
                .status(LeadStatus.NEW)
                .build());

        // Then
        assertThat(lead).isNotNull();
        assertThat(lead.email()).isEqualTo(email);
        assertThat(lead.status()).isEqualTo(LeadStatus.NEW);
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        assertThatThrownBy(() ->
                service.addLead(LeadBuilder.builder()
                        .name("Test")
                        .email("bob1@example.com")
                        .phone("+999")
                        .company("Duplicate")
                        .status(LeadStatus.NEW)
                        .build())
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Lead with email already exists");
    }

    @Test
    void shouldFindAllLeads() {
        List<Lead> all = service.findAll();
        assertThat(all).hasSize(10);
    }

    @Test
    void shouldFindLeadById() {
        Lead first = service.findAll().getFirst();
        Optional<Lead> found = service.findById(first.id());
        assertThat(found).isPresent().get().extracting(Lead::email).isEqualTo(first.email());
    }

    @Test
    void shouldFindLeadByEmail() {
        Optional<Lead> found = service.findByEmail("bob1@example.com");
        assertThat(found).isPresent().get().extracting(Lead::status).isEqualTo(LeadStatus.NEW);
    }

    @Test
    void shouldReturnEmptyWhenLeadNotFound() {
        // Given/When
        Optional<Lead> result = service.findByEmail("nonexistent@example.com");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenLeadByIdNotFound() {
        // Given/When
        Optional<Lead> result = service.findById(UUID.randomUUID());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnOnlyNewLeads_whenFindByStatusNew() {
        // Given When
        List<Lead> result = service.findByStatus(LeadStatus.NEW);

        // Then
        assertThat(result).hasSize(3)
                .allMatch(lead -> lead.status().equals(LeadStatus.NEW));
    }

    @Test
    void shouldReturnOnlyContactedLeads_whenFindByStatusContacted() {
        // Given When
        List<Lead> result = service.findByStatus(LeadStatus.CONTACTED);

        // Then
        assertThat(result)
                .hasSize(5)
                .allMatch(lead -> lead.status() == LeadStatus.CONTACTED);
    }

    @Test
    void shouldReturnOnlyQualifiedLeads_whenFindByStatusQualified() {
        // Given When
        List<Lead> result = service.findByStatus(LeadStatus.QUALIFIED);

        // Then
        assertThat(result)
                .hasSize(2)
                .allMatch(lead -> lead.status() == LeadStatus.QUALIFIED);
    }

    @Test
    void shouldReturnEmptyListWhenNoLeadsWithStatus() {
        // Given: repository с лидами, но НЕТ QUALIFIED
        var emptyRepository = new InMemoryLeadRepository();
        var serviceWithEmptyRepo = new LeadService(emptyRepository);

        // When: findByStatus(QUALIFIED)
        List<Lead> result = serviceWithEmptyRepo.findByStatus(LeadStatus.QUALIFIED);

        // Then: пустой список (size 0)
        assertThat(result).isEmpty();
    }

    @Test
    void shouldUpdateExistingLead() {
        // Given
        Lead original = service.findAll().getFirst();
        UUID id = original.id();

        Lead updatedLead = LeadBuilder.builder(original)
                .name("Updated Name")
                .email("updated@example.com")
                .phone("+999")
                .company("Updated Company")
                .status(LeadStatus.CONTACTED)
                .build();

        // When
        service.update(id, updatedLead);

        // Then
        Optional<Lead> result = service.findById(id);
        assertThat(result).isPresent();
        assertThat(result.get().name()).isEqualTo("Updated Name");
        assertThat(result.get().email()).isEqualTo("updated@example.com");
        assertThat(result.get().status()).isEqualTo(LeadStatus.CONTACTED);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonexistentLead() {
        // Given
        Lead updatedLead = LeadBuilder.builder()
                .name("Test")
                .email("test@example.com")
                .phone("+000")
                .company("Test")
                .status(LeadStatus.NEW)
                .build();

        UUID nonExistentId = UUID.randomUUID();

        // When & Then
        assertThatThrownBy(() -> service.update(nonExistentId, updatedLead))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Lead not found");
    }

    @Test
    void shouldDeleteExistingLead() {
        // Given
        Lead lead = service.findAll().getFirst();
        UUID id = lead.id();

        // When
        service.delete(id);

        // Then
        Optional<Lead> result = service.findById(id);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonexistentLead() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When & Then
        assertThatThrownBy(() -> service.delete(nonExistentId))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void shouldFindLeadsByTextSearch() {
        // Given/When
        List<Lead> result = service.findLeads("Bob", null);

        // Then
        assertThat(result).hasSize(2) // ← Bob1 и Bob2
                .allMatch(lead -> lead.name().contains("Bob"));
    }

    @Test
    void shouldFindLeadsByStatus() {
        // Given/When
        List<Lead> result = service.findLeads(null, LeadStatus.CONTACTED);

        // Then
        assertThat(result).hasSize(5)
                .allMatch(lead -> lead.status() == LeadStatus.CONTACTED);
    }

    @Test
    void shouldFindLeadsByTextAndStatus() {
        // Given/When
        List<Lead> result = service.findLeads("John", LeadStatus.CONTACTED);

        // Then
        assertThat(result).hasSize(2) // ← John1 и John2
                .allMatch(lead -> lead.name().contains("John") && lead.status() == LeadStatus.CONTACTED);
    }

    @Test
    void shouldReturnAllLeadsWhenSearchAndStatusAreNull() {
        // Given/When
        List<Lead> result = service.findLeads(null, null);

        // Then
        assertThat(result).hasSize(10); // ← все лиды
    }
}