package ru.mentee.power.crm.spring;

import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import ru.mentee.power.crm.repository.InMemoryLeadRepository;
import ru.mentee.power.crm.service.LeadService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LeadServiceBeanTest {
    @Autowired
    private ApplicationContext context;

    @Test
    void shouldCreateLeadServiceBean() {
        LeadService service = context.getBean(LeadService.class);
        assertThat(service).isNotNull();
    }

    @Test
    void shouldCreateLeadRepositoryBean() {
        InMemoryLeadRepository repo = context.getBean(InMemoryLeadRepository.class);
        assertThat(repo).isNotNull();
    }

    @Test
    void shouldInjectLeadRepositoryIntoService() {
        LeadService service = context.getBean(LeadService.class);
        // Проверяем что DI работает: service использует repository
        assertThat(service.findAll()).isNotEmpty().allMatch(Objects::nonNull);
    }
}

