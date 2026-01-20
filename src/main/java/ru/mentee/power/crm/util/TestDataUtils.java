package ru.mentee.power.crm.util;

import java.util.List;
import java.util.Random;

import ru.mentee.power.crm.model.LeadBuilder;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.spring.service.LeadService;

/**
 * Утилита для генерации тестовых лидов.
 * Создаёт 50 уникальных записей с разными статусами.
 */
public class TestDataUtils {

    private static final List<String> NAMES = List.of(
            "Alice", "Bob", "Charlie", "Diana", "Eve", "Frank", "Grace", "Henry", "Ivy", "Jack"
    );

    private static final List<String> COMPANIES = List.of(
            "Tech Corp", "Innovate Inc", "Future LLC", "Global Solutions", "NextGen Systems",
            "Alpha Dynamics", "Beta Ventures", "Gamma Industries", "Delta Enterprises", "Omega Group"
    );

    private static final List<LeadStatus> STATUSES = List.of(
            LeadStatus.NEW, LeadStatus.CONTACTED, LeadStatus.QUALIFIED
    );

    private static final Random RANDOM = new Random();

    /**
     * Генерирует 50 уникальных лидов для демо-данных.
     */
    public static void initializeTestData(LeadService leadService) {
        for (int i = 1; i <= 50; i++) {
            String name = NAMES.get(RANDOM.nextInt(NAMES.size()));
            String company = COMPANIES.get(RANDOM.nextInt(COMPANIES.size()));
            LeadStatus status = STATUSES.get(RANDOM.nextInt(STATUSES.size()));

            String email = "user" + i + "@example.com";
            String phone = "+7" + String.format("%010d", 1000000L + i);

            leadService.addLead(LeadBuilder.builder()
                    .name(name)
                    .email(email)
                    .phone(phone)
                    .company(company)
                    .status(status)
                    .build());
        }
    }
}