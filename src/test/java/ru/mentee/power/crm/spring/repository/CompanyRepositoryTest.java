package ru.mentee.power.crm.spring.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.mentee.power.crm.entity.Company;
import ru.mentee.power.crm.entity.Lead;
import ru.mentee.power.crm.model.LeadStatus;

@SpringBootTest
class CompanyRepositoryTest {

  @Autowired private CompanyRepository companyRepository;

  @Autowired private JpaLeadRepository leadRepository;

  @Autowired private EntityManager entityManager;

  @Test
  @Transactional
  void shouldSaveCompanyWithLeads() {
    // Given
    Company company = Company.builder().name("Сбербанк").industry("Finance").build();

    company = companyRepository.save(company);

    Lead lead1 =
        Lead.builder()
            .name("Ivan")
            .email("ivan@sber.ru")
            .phone("123456789")
            .status(LeadStatus.NEW)
            .build();
    Lead lead2 =
        Lead.builder()
            .name("Maria")
            .email("maria@sber.ru")
            .phone("123459999")
            .status(LeadStatus.CONTACTED)
            .build();
    Lead lead3 =
        Lead.builder()
            .name("Bob")
            .email("bob@sber.ru")
            .phone("999999999")
            .status(LeadStatus.QUALIFIED)
            .build();

    company.addLead(lead1);
    company.addLead(lead2);
    company.addLead(lead3);

    // Проверяем, что в БД создались записи
    Company found = companyRepository.findById(company.getId()).orElseThrow();
    assertThat(found.getLeads()).hasSize(3);
  }

  @Test
  void shouldAvoidN1WithEntityGraph() {
    // Создаём и сохраняем компанию
    Company company = Company.builder().name("Тинькофф").industry("Finance").build();
    company = companyRepository.save(company);

    // Создаём и сохраняем лидов
    for (int i = 0; i < 5; i++) {
      Lead lead =
          Lead.builder()
              .name("lead" + i)
              .email("lead" + i + "@tinkoff.ru")
              .phone("+123" + i)
              .status(LeadStatus.NEW)
              .build();

      company.addLead(lead);
      leadRepository.save(lead);
    }

    entityManager.clear();

    Company found = companyRepository.findByIdWithLeads(company.getId()).orElseThrow();
    assertThat(found.getLeads()).hasSize(5);
  }
}
