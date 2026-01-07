package ru.mentee.power.crm.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Точка входа Spring Boot-приложения */
@SpringBootApplication
public class Application {
    /** Запускает встроенное Spring Boot-приложение */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}