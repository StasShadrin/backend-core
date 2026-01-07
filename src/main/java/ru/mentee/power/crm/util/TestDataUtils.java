package ru.mentee.power.crm.util;

import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.service.LeadService;

/** Класс для теста leads */
public class TestDataUtils {
    /** Создание lead*/
    public static void initializeTestData(LeadService leadService) {

        leadService.addLead("bob1@example.com", "+123", "Google", LeadStatus.NEW);
        leadService.addLead("bob2@example.com", "+456", "Meta", LeadStatus.CONTACTED);
        leadService.addLead("alice@example.com", "+789", "Apple", LeadStatus.QUALIFIED);
        leadService.addLead("john@example.com", "+111", "Microsoft", LeadStatus.NEW);
        leadService.addLead("sara@example.com", "+222", "Amazon", LeadStatus.CONTACTED);

        leadService.addLead("<script>alert('XSS Attack!')</script>", "+999", "Hacker Corp", LeadStatus.NEW);
        leadService.addLead("\"><img src=x onerror=alert('XSS2')>", "+888", "Evil Corp", LeadStatus.CONTACTED);
        leadService.addLead("'; DROP TABLE users; --", "+777", "SQL Injection", LeadStatus.QUALIFIED);
    }
}