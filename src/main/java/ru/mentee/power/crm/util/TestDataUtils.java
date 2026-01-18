package ru.mentee.power.crm.util;

import ru.mentee.power.crm.model.LeadBuilder;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.service.LeadService;

/** Класс для теста leads */
public class TestDataUtils {
    /** Создание lead*/
    public static void initializeTestData(LeadService leadService) {

        leadService.addLead(LeadBuilder.builder()
                .name("Bob")
                .email("bob1@example.com")
                .phone("+123")
                .company("Google")
                .status(LeadStatus.NEW)
                .build());
        leadService.addLead(LeadBuilder.builder()
                .name("Bob")
                .email("bob2@example.com")
                .phone("+456")
                .company("Meta")
                .status(LeadStatus.CONTACTED)
                .build());
        leadService.addLead(LeadBuilder.builder()
                .name("Alice")
                .email("alice@example.com")
                .phone("+789")
                .company("Apple")
                .status(LeadStatus.QUALIFIED)
                .build());
        leadService.addLead(LeadBuilder.builder()
                .name("John")
                .email("john@example.com")
                .phone("+111")
                .company("Microsoft")
                .status(LeadStatus.NEW)
                .build());
        leadService.addLead(LeadBuilder.builder()
                .name("Sara")
                .email("sara@example.com")
                .phone("+222")
                .company("Amazon")
                .status(LeadStatus.CONTACTED)
                .build());
    }
}