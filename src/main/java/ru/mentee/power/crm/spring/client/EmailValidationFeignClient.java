package ru.mentee.power.crm.spring.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/***/
@FeignClient(name = "email-validation", url = "${email.validation.base-url}")
public interface EmailValidationFeignClient {
  /***/
  @GetMapping("/api/validate/email")
  EmailValidationResponse validateEmail(@RequestParam("email") String email);
}
