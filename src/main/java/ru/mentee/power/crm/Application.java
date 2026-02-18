package ru.mentee.power.crm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;

/** Точка входа Spring Boot-приложения */
@SpringBootApplication
@EnableFeignClients
@EnableRetry
public class Application {
  /** Запускает встроенное Spring Boot-приложение */
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
