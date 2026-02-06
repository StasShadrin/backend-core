package ru.mentee.power.crm.spring.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import ru.mentee.power.crm.entity.Company;
import ru.mentee.power.crm.entity.Lead;
import ru.mentee.power.crm.model.LeadStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class JpaLeadRepositoryTest {

    @Autowired
    private JpaLeadRepository repository;

    @Autowired
    private CompanyRepository companyRepository;


    private Lead lead1;
    private Lead lead2;
    private Company company1;
    private Company company2;


    @BeforeEach
    void setUpForDerivedTests() {
        company1 = Company.builder()
                .name("ACME Corp")
                .build();
        companyRepository.save(company1);

        lead1 = Lead.builder()
                .name("John")
                .email("john@example.com")
                .phone("123")
                .company(company1)
                .status(LeadStatus.NEW)
                .createdAt(OffsetDateTime.now().minusDays(5))
                .build();
        repository.save(lead1);

        company2 = Company.builder()
                .name("Tech Inc")
                .build();
        companyRepository.save(company2);

        lead2 = Lead.builder()
                .name("Jane")
                .email("jane@example.com")
                .phone("456")
                .company(company2)
                .status(LeadStatus.CONTACTED)
                .createdAt(OffsetDateTime.now().minusDays(2))
                .build();
        repository.save(lead2);
    }

    @Test
    void shouldSaveAndFindLeadById_whenValidData() {
        // Given
        Company company = Company.builder()
                .name("ACME Corp")
                .build();
        companyRepository.save(company);

        Lead lead = Lead.builder()
                .name("Lead 1")
                .email("test@example.com")
                .phone("123456789")
                .company(company)
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
        Company company = Company.builder()
                .name("TechCorp")
                .build();
        company = companyRepository.save(company);

        Lead lead = Lead.builder()
                .name("test")
                .email("native@test.com")
                .phone("123456789")
                .company(company)
                .status(LeadStatus.NEW)
                .build();
        repository.save(lead);

        // When
        Optional<Lead> found = repository.findByEmailNative("native@test.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCompany().getName()).isEqualTo("TechCorp");
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
        //Then
        assertThat(repository.findAll()).hasSize(2);
    }

    @Test
    void shouldDeleteLeadById_whenLeadExists() {
        //When
        UUID id = lead1.getId();
        repository.deleteById(id);
        Optional<Lead> found = repository.findById(id);

        //Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByEmail_shouldReturnLead_whenExists() {
        // When
        Optional<Lead> found = repository.findByEmail("john@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCompany().getName()).isEqualTo("ACME Corp");
    }

    @Test
    void findByStatus_shouldReturnFilteredLeads() {
        // When
        List<Lead> newLeads = repository.findByStatus(LeadStatus.NEW);

        // Then
        assertThat(newLeads).hasSize(1);
        assertThat(newLeads.getFirst().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void findByStatusIn_shouldReturnLeadsWithMultipleStatuses() {
        // Given
        List<LeadStatus> statuses = List.of(LeadStatus.NEW, LeadStatus.CONTACTED);

        // When
        List<Lead> found = repository.findByStatusIn(statuses);

        // Then
        assertThat(found).hasSize(2);
    }

    @Test
    void findAll_withPageable_shouldReturnPage() {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 1);

        // When
        Page<Lead> page = repository.findAll(pageRequest);

        // Then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getNumber()).isZero();
    }

    @Test
    void countByStatus_shouldReturnCorrectCount() {
        // When
        long count = repository.countByStatus(LeadStatus.NEW);

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    void existsByEmail_shouldReturnTrue_whenEmailExists() {
        // When
        boolean exists = repository.existsByEmail("john@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_shouldReturnFalse_whenEmailNotFound() {
        // When
        boolean exists = repository.existsByEmail("nonexistent@test.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void findByStatusAndCompany_shouldReturnMatchingLeads() {
        // When
        List<Lead> leads = repository.findByStatusAndCompany(LeadStatus.NEW, company1);

        // Then
        assertThat(leads).hasSize(1);
        assertThat(leads.getFirst().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void findByStatusOrderByCreatedAtDesc_shouldReturnSortedLeads() {
        // When
        List<Lead> leads = repository.findByStatusOrderByCreatedAtDesc(LeadStatus.CONTACTED);

        // Then
        assertThat(leads).hasSize(1);
        assertThat(leads.getFirst().getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    void findCreatedAfter_shouldReturnRecentLeads() {
        // When
        List<Lead> leads = repository.findCreatedAfter(OffsetDateTime.now().minusDays(3));

        // Then
        assertThat(leads).hasSize(1);
        assertThat(leads.getFirst().getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    void findByCompanyOrderedByDate_shouldReturnSortedLeads() {
        // When
        List<Lead> leads = repository.findByCompanyOrderedByDate(company2);

        // Then
        assertThat(leads).hasSize(1);
        assertThat(leads.getFirst().getEmail()).isEqualTo("jane@example.com");
    }
}