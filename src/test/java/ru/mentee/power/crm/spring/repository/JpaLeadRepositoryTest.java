package ru.mentee.power.crm.spring.repository;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import ru.mentee.power.crm.entity.Lead;
import ru.mentee.power.crm.model.LeadStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class JpaLeadRepositoryTest {

    @Autowired
    private JpaLeadRepository repository;

    @Test
    void shouldSaveAndFindLeadById_whenValidData() {
        // Given
        Lead lead = Lead.builder()
                .name("Lead 1")
                .email("test@example.com")
                .phone("123456789")
                .company("ACME")
                .status(LeadStatus.NEW)
                .build();

        // When
        Lead saved = repository.save(lead);
        Optional<Lead> found = repository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getStatus()).isEqualTo(LeadStatus.NEW);
    }

    @Test
    void shouldFindByEmailNative_whenLeadExists() {
        // Given
        Lead lead = Lead.builder()
                .name("test")
                .email("native@test.com")
                .phone("123456789")
                .company("TechCorp")
                .status(LeadStatus.NEW)
                .build();
        repository.save(lead);

        // When
        Optional<Lead> found = repository.findByEmailNative("native@test.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCompany()).isEqualTo("TechCorp");
    }

    @Test
    void shouldReturnEmptyOptional_whenEmailNotFound() {
        // When
        Optional<Lead> found = repository.findByEmailNative("nonexistent@test.com");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindAllLead() {
        // Given
        Lead lead1 = Lead.builder()
                .name("test")
                .email("native@test.com")
                .phone("123456789")
                .company("TechCorp")
                .status(LeadStatus.NEW)
                .build();
        Lead lead2 = Lead.builder()
                .name("Lead 1")
                .email("test@example.com")
                .phone("123456789")
                .company("ACME")
                .status(LeadStatus.NEW)
                .build();
        repository.save(lead1);
        repository.save(lead2);

        //Then
        assertThat(repository.findAll()).hasSize(2);
    }

    @Test
    void shouldDeleteLeadById_whenLeadExists() {
        //Given
        Lead lead1 = Lead.builder()
                .name("test")
                .email("native@test.com")
                .phone("123456789")
                .company("TechCorp")
                .status(LeadStatus.NEW)
                .build();
        repository.save(lead1);

        //When
        UUID id = lead1.getId();
        repository.deleteById(id);
        Optional<Lead> found = repository.findById(id);

        //Then
        assertThat(found).isEmpty();
    }
}
