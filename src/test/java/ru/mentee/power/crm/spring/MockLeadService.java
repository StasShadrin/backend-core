package ru.mentee.power.crm.spring;

import java.util.List;
import java.util.UUID;

import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.service.LeadService;

/** Mock-реализация LeadService для unit-тестов */
public class MockLeadService extends LeadService {
    private final List<Lead> mockLeads;

    /** Создаёт сервис с фиксированным списком тестовых лидов */
    public MockLeadService() {
        super(null); // repository не используется в mock
        this.mockLeads = List.of(
                new Lead(UUID.randomUUID(), "test1@example.com", "+1234567890", "New Comp", LeadStatus.NEW),
                new Lead(UUID.randomUUID(), "test2@example.com", "+0987654321", "Old Comp", LeadStatus.NEW)
        );
    }

    @Override
    public List<Lead> findAll() {
        return mockLeads;
    }
}