package ru.mentee.power.crm.spring.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import ru.mentee.power.crm.entity.Company;
import ru.mentee.power.crm.entity.Lead;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.spring.repository.CompanyRepository;
import ru.mentee.power.crm.spring.repository.JpaLeadRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class JpaLeadServiceTest {

    @Autowired
    private JpaLeadService leadService;

    @Autowired
    private JpaLeadRepository leadRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @BeforeEach
    void setUp() {
        leadRepository.deleteAll();
        companyRepository.deleteAll();

        // Создаём 3 NEW лида
        for (int i = 1; i <= 3; i++) {
            // Сначала сохраняем компанию
            Company company = Company.builder()
                    .name("Company " + i)
                    .build();
            company = companyRepository.save(company); // ← СОХРАНЯЕМ КОМПАНИЮ

            // Потом создаём лид с сохранённой компанией
            Lead lead = Lead.builder()
                    .name("Lead" + i)
                    .email("lead" + i + "@example.com")
                    .phone(i + "123")
                    .company(company) // ← ИСПОЛЬЗУЕМ СОХРАНЁННУЮ КОМПАНИЮ
                    .status(LeadStatus.NEW)
                    .build();
            leadRepository.save(lead);
        }

        // Добавляем лиды со статусом LOST для теста удаления
        for (int i = 4; i <= 5; i++) {
            // Сначала сохраняем компанию
            Company company = Company.builder()
                    .name("LostCompany " + i)
                    .build();
            company = companyRepository.save(company); // ← СОХРАНЯЕМ КОМПАНИЮ

            // Потом создаём лид с сохранённой компанией
            Lead lead = Lead.builder()
                    .name("LostLead" + i)
                    .email("lost" + i + "@example.com")
                    .phone(i + "456")
                    .company(company) // ← ИСПОЛЬЗУЕМ СОХРАНЁННУЮ КОМПАНИЮ
                    .status(LeadStatus.LOST)
                    .build();
            leadRepository.save(lead);
        }
    }

    @Test
    void convertNewToContacted_shouldUpdateMultipleLeads() {
        // When
        int updated = leadService.convertNewToContacted();

        // Then
        assertThat(updated).isEqualTo(3);

        // Проверяем что статус изменился
        long contactedCount = leadRepository.countByStatus(LeadStatus.CONTACTED);
        assertThat(contactedCount).isEqualTo(3);

        long newCount = leadRepository.countByStatus(LeadStatus.NEW);
        assertThat(newCount).isZero();
    }

    @Test
    void archiveOldLeads_shouldDeleteLeadsByStatus() {
        // When
        int deleted = leadService.archiveOldLeads(LeadStatus.LOST);

        // Then
        assertThat(deleted).isEqualTo(2);

        // Проверяем, что лиды со статусом LOST удалены
        long lostCount = leadRepository.countByStatus(LeadStatus.LOST);
        assertThat(lostCount).isZero();

        // Проверяем, что другие лиды остались
        long newCount = leadRepository.countByStatus(LeadStatus.NEW);
        assertThat(newCount).isEqualTo(3);
    }

    @Test
    void addLead_shouldSaveNewLead() {
        // Given
        Company company = Company.builder()
                .name("New Company")
                .build();
        companyRepository.save(company);

        Lead newLead = Lead.builder()
                .name("New Lead")
                .email("new@example.com")
                .phone("123")
                .company(company)
                .status(LeadStatus.NEW)
                .build();

        // When
        Lead saved = leadService.addLead(newLead);

        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void addLead_shouldThrowException_whenEmailExists() {
        // When & Then
        Company company = Company.builder()
                .name("Dup Company")
                .build();
        companyRepository.save(company);

        Lead duplicate = Lead.builder()
                .name("Duplicate")
                .email("lead1@example.com") // уже существует
                .phone("123")
                .company(company)
                .status(LeadStatus.NEW)
                .build();

        assertThatThrownBy(() -> leadService.addLead(duplicate))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Lead with email already exists");
    }

    @Test
    void findAll_shouldReturnAllLeads() {
        // When
        List<Lead> leads = leadService.findAll();

        // Then
        assertThat(leads).hasSize(5); // 3 NEW + 2 LOST
    }

    @Test
    void findById_shouldReturnLead_whenExists() {
        // Given
        UUID id = leadRepository.findAll().getFirst().getId();

        // When
        Optional<Lead> found = leadService.findById(id);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(id);
    }

    @Test
    void findByStatus_shouldReturnFilteredLeads() {
        // When
        List<Lead> newLeads = leadService.findByStatus(LeadStatus.NEW);

        // Then
        assertThat(newLeads).hasSize(3);
        assertThat(newLeads.getFirst().getStatus()).isEqualTo(LeadStatus.NEW);
    }

    @Test
    void update_shouldUpdateLeadFields() {
        // Given
        UUID id = leadRepository.findAll().getFirst().getId();
        Company company = Company.builder()
                .name("Updated Company")
                .build();
        companyRepository.save(company);

        Lead updatedLead = Lead.builder()
                .name("Updated Name")
                .email("updated@example.com")
                .phone("999")
                .company(company)
                .status(LeadStatus.CONTACTED)
                .build();

        // When
        leadService.update(id, updatedLead);

        // Then
        Optional<Lead> result = leadService.findById(id);
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Updated Name");
        assertThat(result.get().getStatus()).isEqualTo(LeadStatus.CONTACTED);
    }

    @Test
    void delete_shouldRemoveLead() {
        // Given
        UUID id = leadRepository.findAll().getFirst().getId();

        // When
        leadService.delete(id);

        // Then
        assertThat(leadService.findById(id)).isEmpty();
    }

    @Test
    void findLeads_shouldFilterBySearchAndStatus() {
        // When
        List<Lead> results = leadService.findLeads("Lead1", LeadStatus.NEW);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getEmail()).isEqualTo("lead1@example.com");
    }

    @Test
    void findByEmail_shouldReturnLead_whenExists() {
        // When
        Optional<Lead> found = leadService.findByEmail("lead1@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("lead1@example.com");
    }

    @Test
    void findByStatuses_shouldReturnLeadsWithMultipleStatuses() {
        // When
        List<Lead> leads = leadService.findByStatuses(LeadStatus.NEW, LeadStatus.LOST);

        // Then
        assertThat(leads).hasSize(5); // все лиды
    }

    @Test
    void getFirstPage_shouldReturnPagedResults() {
        // When
        var page = leadService.getFirstPage(2);

        // Then
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(5);
    }

    @Test
    void searchByCompany_shouldReturnPagedResults() {
        // Найдём существующую компанию по названию
        Company company = companyRepository.findByName("Company 1")
                .orElseThrow(() -> new RuntimeException("Company 'Company 1' not found"));

        var page = leadService.searchByCompany(company, 0, 1);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getEmail()).isEqualTo("lead1@example.com");
    }

    @Test
    void changStatus_shouldUpdateLeadsByCompany() {
        Company company = companyRepository.findByName("Company 1")
                .orElseThrow(() -> new RuntimeException("Company 'Company 1' not found"));

        long initialNewCount = leadRepository.countByStatus(LeadStatus.NEW);
        assertThat(initialNewCount).isGreaterThanOrEqualTo(1);

        leadService.changStatus(company, LeadStatus.CONTACTED);


        long newCount = leadRepository.countByStatus(LeadStatus.NEW);
        long contactedCount = leadRepository.countByStatus(LeadStatus.CONTACTED);

        assertThat(newCount).isEqualTo(initialNewCount - 1);
        assertThat(contactedCount).isEqualTo(1);
    }
}