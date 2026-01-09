package ru.mentee.power.crm.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.repository.InMemoryLeadRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LeadServiceTest {

    private LeadService service;

    @BeforeEach
    void setUp() {
        var repository = new InMemoryLeadRepository();
        service = new LeadService(repository);

        service.addLead("bob1@example.com", "+123", "Google", LeadStatus.NEW);
        service.addLead("bob2@example.com", "+456", "Meta", LeadStatus.NEW);
        service.addLead("alice@example.com", "+789", "Apple", LeadStatus.NEW);

        service.addLead("john1@example.com", "+111", "Microsoft", LeadStatus.CONTACTED);
        service.addLead("john2@example.com", "+222", "Amazon", LeadStatus.CONTACTED);
        service.addLead("sara@example.com", "+333", "Netflix", LeadStatus.CONTACTED);
        service.addLead("charlie@example.com", "+444", "Tesla", LeadStatus.CONTACTED);
        service.addLead("diana@example.com", "+555", "SpaceX", LeadStatus.CONTACTED);

        service.addLead("satana@example.com", "+666", "Evil", LeadStatus.QUALIFIED);
        service.addLead("jim@example.com", "+777", "Yandex", LeadStatus.QUALIFIED);
    }


    @Test
    void shouldCreateLeadWhenEmailIsUnique() {
        // Given
        String email = "newuser@example.com";

        // When
        Lead lead = service.addLead(email, "+000", "Startup", LeadStatus.NEW);

        // Then
        assertThat(lead).isNotNull();
        assertThat(lead.email()).isEqualTo(email);
        assertThat(lead.status()).isEqualTo(LeadStatus.NEW);
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        assertThatThrownBy(() ->
                service.addLead("bob1@example.com", "+999", "Duplicate", LeadStatus.NEW)
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
}