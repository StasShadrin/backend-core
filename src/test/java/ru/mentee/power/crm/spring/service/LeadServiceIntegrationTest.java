package ru.mentee.power.crm.spring.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import ru.mentee.power.crm.entity.Company;
import ru.mentee.power.crm.entity.Lead;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.spring.dto.CreateDealRequest;
import ru.mentee.power.crm.spring.repository.CompanyRepository;
import ru.mentee.power.crm.spring.repository.JpaDealRepository;
import ru.mentee.power.crm.spring.repository.JpaLeadRepository;

@SpringBootTest
@ActiveProfiles("test")
class LeadServiceIntegrationTest {

  @Autowired private JpaDealService dealService;

  @Autowired private JpaLeadService leadService;

  @Autowired private JpaLeadRepository leadRepository;

  @Autowired private JpaDealRepository dealRepository;

  @Autowired private CompanyRepository companyRepository;

  @BeforeEach
  void setUp() {
    leadRepository.deleteAll();
    dealRepository.deleteAll();
    companyRepository.deleteAll();

    Company company = Company.builder().name("Test Company").build();
    companyRepository.save(company);

    Lead lead =
        Lead.builder()
            .name("Test Lead")
            .email("test@example.com")
            .phone("123456789")
            .company(company)
            .status(LeadStatus.QUALIFIED)
            .build();
    leadRepository.save(lead);
  }

  @Test
  void convertLeadToDeal_shouldRollbackOnConstraintViolation() {
    // Given
    Lead lead = leadRepository.findAll().getFirst();
    UUID leadId = lead.getId();

    CreateDealRequest request = new CreateDealRequest();
    request.setTitle("Test Deal");
    request.setAmount(null);
    request.setCompanyId(UUID.randomUUID());

    // When & Then
    assertThatThrownBy(() -> leadService.convertLeadToDeal(leadId, request))
        .isInstanceOf(DataIntegrityViolationException.class);

    Lead updatedLead = leadRepository.findById(leadId).orElseThrow();
    assertThat(updatedLead.getStatus()).isEqualTo(LeadStatus.QUALIFIED);

    assertThat(dealRepository.findAll()).isEmpty();
  }

  //    @Test
  //    void demonstrateSelfInvocationProblem() {
  //        // Given
  //        Lead lead1 = createLead("lead1@example.com");
  //        Lead lead2 = createLead("lead2@example.com");
  //        Lead lead3 = createLead("lead3@example.com");
  //        Lead failingLead = createLead("fail@example.com");
  //
  //        List<UUID> ids = List.of(
  //                lead1.getId(),
  //                lead2.getId(),
  //                failingLead.getId(), // ← Используем реальный ID
  //                lead3.getId()
  //        );
  //
  //        // When & Then
  //        assertThatThrownBy(() -> leadService.processLeads(ids))
  //                .isInstanceOf(RuntimeException.class)
  //                .hasMessageContaining("Simulated failure");
  //
  //
  //        assertLeadStatusUnchanged(lead1.getId());
  //        assertLeadStatusUnchanged(lead2.getId());
  //        assertLeadStatusUnchanged(lead3.getId());
  //    }

  //    private void assertLeadStatusUnchanged(UUID id) {
  //        Lead lead = leadRepository.findById(id)
  //                .orElseThrow(() -> new AssertionError("Lead not found: " + id));
  //        assertThat(lead.getStatus()).isEqualTo(LeadStatus.NEW);
  //    }

  // Вспомогательный метод
  private Lead createLead(String email) {
    Company company = Company.builder().name("Test").build();
    companyRepository.save(company);

    Lead lead =
        Lead.builder()
            .name("Test Lead")
            .email(email)
            .phone("123")
            .company(company)
            .status(LeadStatus.NEW)
            .build();
    return leadRepository.save(lead);
  }

  @Test
  void processLeads_shouldIsolateTransactionsPerLead() {
    // Given
    Lead lead1 = createLead("lead1@example.com");
    Lead lead2 = createLead("lead2@example.com");
    Lead failingLead = createLead("fail@example.com");
    Lead lead3 = createLead("lead3@example.com");

    List<UUID> ids = List.of(lead1.getId(), lead2.getId(), failingLead.getId(), lead3.getId());

    // When & Then
    assertThatThrownBy(() -> leadService.processLeads(ids))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Simulated failure");

    // Проверяем результат:
    // lead1 и lead2 должны быть обновлены (обработка до ошибки)
    assertLeadStatus(lead1.getId(), LeadStatus.CONTACTED);
    assertLeadStatus(lead2.getId(), LeadStatus.CONTACTED);

    // failingLead должен остаться без изменений (ошибка в его транзакции)
    assertLeadStatus(failingLead.getId(), LeadStatus.NEW);

    // lead3 не должен быть обработан (цикл прервался на ошибке)
    assertLeadStatus(lead3.getId(), LeadStatus.NEW);
  }

  private void assertLeadStatus(UUID id, LeadStatus expectedStatus) {
    Lead lead =
        leadRepository.findById(id).orElseThrow(() -> new AssertionError("Lead not found: " + id));
    assertThat(lead.getStatus()).isEqualTo(expectedStatus);
  }
}
