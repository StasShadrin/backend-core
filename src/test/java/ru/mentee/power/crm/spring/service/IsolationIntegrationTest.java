package ru.mentee.power.crm.spring.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import ru.mentee.power.crm.entity.Lead;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.spring.repository.JpaLeadRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class IsolationIntegrationTest {

    @Autowired
    private IsolationDemoService demoService;

    @Autowired
    private JpaLeadRepository leadRepository;

    @BeforeEach
    void setUp() {
        leadRepository.deleteAll();
    }

    @Test
    void demonstrate_READ_COMMITTED_usage() {
        Lead lead = createLead();

        Lead result = demoService.readWithReadCommitted(lead.getId());

        assertThat(result.getStatus()).isEqualTo(LeadStatus.NEW);
    }

    @Test
    void demonstrate_REPEATABLE_READ_usage() {
        Lead lead = createLead();

        Lead result = demoService.readWithRepeatableRead(lead.getId());

        assertThat(result.getStatus()).isEqualTo(LeadStatus.NEW);
    }

    private Lead createLead() {
        Lead lead = new Lead();
        lead.setName("Test Lead");
        lead.setEmail("test@example.com");
        lead.setPhone("123");
        lead.setCompany("Test");
        lead.setStatus(LeadStatus.NEW);
        return leadRepository.save(lead);
    }
}