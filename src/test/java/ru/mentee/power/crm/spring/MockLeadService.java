package ru.mentee.power.crm.spring;

import java.util.List;
import java.util.UUID;

import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.model.LeadBuilder;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.spring.service.LeadService;

/** Mock-реализация LeadService для unit-тестов */
public class MockLeadService extends LeadService {
    private final List<Lead> mockLeads;

    /** Создаёт сервис с фиксированным списком тестовых лидов */
    public MockLeadService() {
        super(null); // repository не используется в mock
        this.mockLeads = List.of(
                LeadBuilder.builder()
                        .id(UUID.randomUUID())
                        .email("test1@example.com")
                        .phone("+1234567890")
                        .company("New Comp")
                        .status(LeadStatus.NEW)
                        .build(),
                LeadBuilder.builder()
                        .id(UUID.randomUUID())
                        .email("test2@example.com")
                        .phone("+0987654321")
                        .company("Old Comp")
                        .status(LeadStatus.NEW)
                        .build()
        );
    }

    @Override
    public List<Lead> findAll() {
        return mockLeads;
    }
}