package ru.mentee.power.crm.spring.service;

import static org.assertj.core.api.Assertions.assertThat;

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

@SpringBootTest
class PropagationIntegrationTest {

  @Autowired private PropagationDemoService demoService;

  @Autowired private JpaLeadRepository leadRepository;

  @Autowired private CompanyRepository companyRepository;

  @BeforeEach
  void setUp() {
    leadRepository.deleteAll();
  }

  @Test
  void propagation_REQUIRED_shouldReuseTransaction() {
    Lead lead = createLead("test1@example.com");
    demoService.methodWithRequired(lead.getId());
    assertLeadStatus(lead.getId(), LeadStatus.CONTACTED);
  }

  @Test
  void propagation_REQUIRES_NEW_shouldCreateNewTransaction() {
    Lead lead = createLead("test2@example.com");
    demoService.methodWithRequiresNew(lead.getId());
    assertLeadStatus(lead.getId(), LeadStatus.QUALIFIED);
  }

  @Test
  @Transactional
  void propagation_MANDATORY_worksInExistingTransaction() {
    Lead lead = createLead("test3@example.com");
    demoService.methodWithMandatory(lead.getId());
    assertLeadStatus(lead.getId(), LeadStatus.LOST);
  }

  private Lead createLead(String email) {
    Company company = Company.builder().name("Test").build();
    companyRepository.save(company);

    Lead lead =
        Lead.builder()
            .name("Test")
            .email(email)
            .phone("123")
            .company(company)
            .status(LeadStatus.NEW)
            .build();
    return leadRepository.save(lead);
  }

  private void assertLeadStatus(UUID id, LeadStatus expected) {
    Lead lead = leadRepository.findById(id).orElseThrow();
    assertThat(lead.getStatus()).isEqualTo(expected);
  }
}
