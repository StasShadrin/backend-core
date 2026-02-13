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
    private JpaLeadRepository leadRepository;

    @Autowired
    private CompanyRepository companyRepository;


    private Lead leadFirst;
    private Lead leadSecond;
    private Company companyFirst;
    private Company companySecond;


    @BeforeEach

    void setUpForDerivedTests() {
        companyFirst = Company.builder()
                .name("ACME Corp")
                .build();
        companyRepository.save(companyFirst);

        leadFirst = Lead.builder()
                .name("John")
                .email("john@example.com")
                .phone("123")
                .company(companyFirst)
                .status(LeadStatus.NEW)
                .createdAt(OffsetDateTime.now().minusDays(5))
                .build();
        leadRepository.save(leadFirst);

        companySecond = Company.builder()
                .name("Tech Inc")
                .build();
        companyRepository.save(companySecond);

        leadSecond = Lead.builder()
                .name("Jane")
                .email("jane@example.com")
                .phone("456")
                .company(companySecond)
                .status(LeadStatus.CONTACTED)
                .createdAt(OffsetDateTime.now().minusDays(2))
                .build();
        leadRepository.save(leadSecond);
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
        Lead saved = leadRepository.save(lead);
        Optional<Lead> found = leadRepository.findById(saved.getId());

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
        leadRepository.save(lead);

        // When
        Optional<Lead> found = leadRepository.findByEmailNative("native@test.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCompany().getName()).isEqualTo("TechCorp");
    }

    @Test
    void shouldReturnEmptyOptional_whenEmailNotFound() {
        // When
        Optional<Lead> found = leadRepository.findByEmailNative("nonexistent@test.com");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindAllLead() {
        //Then
        assertThat(leadRepository.findAll()).hasSize(2);
    }

    @Test
    void shouldDeleteLeadById_whenLeadExists() {
        //When
        UUID id = leadFirst.getId();
        leadRepository.deleteById(id);
        Optional<Lead> found = leadRepository.findById(id);

        //Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByEmail_shouldReturnLead_whenExists() {
        // When
        Optional<Lead> found = leadRepository.findByEmail("john@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCompany().getName()).isEqualTo("ACME Corp");
    }

    @Test
    void findByStatus_shouldReturnFilteredLeads() {
        // When
        List<Lead> newLeads = leadRepository.findByStatus(LeadStatus.NEW);

        // Then
        assertThat(newLeads).hasSize(1);
        assertThat(newLeads.getFirst().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void findByStatusIn_shouldReturnLeadsWithMultipleStatuses() {
        // Given
        List<LeadStatus> statuses = List.of(LeadStatus.NEW, LeadStatus.CONTACTED);

        // When
        List<Lead> found = leadRepository.findByStatusIn(statuses);

        // Then
        assertThat(found).hasSize(2);
    }

    @Test
    void findAll_withPageable_shouldReturnPage() {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 1);

        // When
        Page<Lead> page = leadRepository.findAll(pageRequest);

        // Then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getNumber()).isZero();
    }

    @Test
    void countByStatus_shouldReturnCorrectCount() {
        // When
        long count = leadRepository.countByStatus(LeadStatus.NEW);

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    void existsByEmail_shouldReturnTrue_whenEmailExists() {
        // When
        boolean exists = leadRepository.existsByEmail("john@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_shouldReturnFalse_whenEmailNotFound() {
        // When
        boolean exists = leadRepository.existsByEmail("nonexistent@test.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void findByStatusAndCompany_shouldReturnMatchingLeads() {
        // When
        List<Lead> leads = leadRepository.findByStatusAndCompany(LeadStatus.NEW, companyFirst);

        // Then
        assertThat(leads).hasSize(1);
        assertThat(leads.getFirst().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void findByStatusOrderByCreatedAtDesc_shouldReturnSortedLeads() {
        // When
        List<Lead> leads = leadRepository.findByStatusOrderByCreatedAtDesc(LeadStatus.CONTACTED);

        // Then
        assertThat(leads).hasSize(1);
        assertThat(leads.getFirst().getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    void findCreatedAfter_shouldReturnRecentLeads() {
        // When
        List<Lead> leads = leadRepository.findCreatedAfter(OffsetDateTime.now().minusDays(3));

        // Then
        assertThat(leads).hasSize(1);
        assertThat(leads.getFirst().getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    void findByCompanyOrderedByDate_shouldReturnSortedLeads() {
        // When
        List<Lead> leads = leadRepository.findByCompanyOrderedByDate(companySecond);

        // Then
        assertThat(leads).hasSize(1);
        assertThat(leads.getFirst().getEmail()).isEqualTo("jane@example.com");
    }
    @Test
    void shouldFindByEmailIgnoreCase_whenEmailExists() {
        // Given
        Lead lead = Lead.builder()
                .name("Test")
                .phone("+123")
                .email("Test@ex.com")
                .status(LeadStatus.NEW)
                .company(companyFirst)
                .build();
        leadRepository.save(lead);

        // When
        Optional<Lead> found = leadRepository.findByEmailIgnoreCase("test@ex.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(lead.getEmail());
    }

    @Test
    void shouldReturnEmpty_whenEmailNotFound() {
        Optional<Lead> found = leadRepository.findByEmailIgnoreCase("nonexist@ex.com");

        assertThat(found).isEmpty();
    }
}