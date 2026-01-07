package ru.mentee.power.crm.config;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import ru.mentee.power.crm.service.LeadService;
import ru.mentee.power.crm.util.TestDataUtils;

/** Инициализатор тестовых данных при запуске приложения */
@Component
@RequiredArgsConstructor
public class TestDataInitializer {

    private final LeadService leadService;

    /** Загружает демо-данные (лиды) в сервис при старте контекста Spring */
    @PostConstruct
    public void init() {
        TestDataUtils.initializeTestData(leadService);
    }
}