package ru.mentee.power.crm.spring.service;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.mentee.power.crm.spring.dto.generated.LeadResponse.StatusEnum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.mentee.power.crm.entity.Company;
import ru.mentee.power.crm.entity.Lead;
import ru.mentee.power.crm.spring.repository.JpaLeadRepository;

@SpringBootTest
@Transactional
class IsolationIntegrationTest {

  @Autowired private IsolationDemoService demoService;

  @Autowired private JpaLeadRepository leadRepository;

  @BeforeEach
  void setUp() {
    leadRepository.deleteAll();
  }

  @Test
  void demonstrate_READ_COMMITTED_usage() {
    Lead lead = createLead();

    Lead result = demoService.readWithReadCommitted(lead.getId());

    assertThat(result.getStatus()).isEqualTo(StatusEnum.NEW);
  }

  @Test
  void demonstrate_REPEATABLE_READ_usage() {
    Lead lead = createLead();

    Lead result = demoService.readWithRepeatableRead(lead.getId());

    assertThat(result.getStatus()).isEqualTo(StatusEnum.NEW);
  }

  private Lead createLead() {
    Lead lead =
        Lead.builder()
            .name("Test Lead")
            .email("test@example.com")
            .phone("123")
            .company(Company.builder().name("Test").build())
            .status(StatusEnum.NEW)
            .build();
    return leadRepository.save(lead);
  }
}
