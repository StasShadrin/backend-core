package ru.mentee.power.crm.spring.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/***/
@Component
public class EmailValidationClient {

  private final RestTemplate restTemplate;
  private final String baseUrl;

  /** Конструктор с параметрами из контекста Spring */
  public EmailValidationClient(
      RestTemplate restTemplate, @Value("${email.validation.base-url}") String baseUrl) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
  }

  /** Проверяет email через внешний API */
  public EmailValidationResponse validateEmail(String email) {
    String url = baseUrl + "/api/validate/email?email=" + email;

    try {
      return restTemplate.getForObject(url, EmailValidationResponse.class);
    } catch (RestClientException e) {
      System.err.println("Ошибка при вызове Email Validation API: " + e.getMessage());
      return new EmailValidationResponse(email, true, "Сервис временно недоступен");
    }
  }
}
