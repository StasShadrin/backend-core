package ru.mentee.power.crm.spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.mentee.power.crm.spring.repository.LeadRepository;
import ru.mentee.power.crm.spring.service.LeadService;

/** Тестовый контроллер */
@Controller
public class DemoController {

  /** Constructor Injection (recommended) — final field */
  private final LeadService constructorService;

  /** Field Injection (not recommended) — cannot be final */
  @Autowired private LeadRepository fieldRepository;

  /** Setter Injection (for optional deps) — cannot be final */
  private LeadService setterService;

  /** Constructor для Constructor Injection */
  public DemoController(LeadService constructorService) {
    this.constructorService = constructorService;
  }

  /** Setter для Setter Injection */
  @Autowired(required = false)
  public void setSetterService(LeadService setterService) {
    this.setterService = setterService;
  }

  /** Демонстрационный эндпоинт */
  @GetMapping("/demo")
  @ResponseBody
  public String demo() {
    StringBuilder sb = new StringBuilder("DI Types Demo:\\n\\n");

    sb.append("Constructor Injection (final): ")
        .append(constructorService != null ? "✓ Injected" : "✗ NULL")
        .append("\\n");

    sb.append("Field Injection (@Autowired field): ")
        .append(fieldRepository != null ? "✓ Injected" : "✗ NULL")
        .append("\\n");

    sb.append("Setter Injection (@Autowired setter): ")
        .append(setterService != null ? "✓ Injected" : "✗ NULL")
        .append("\\n\\n");

    sb.append("Recommendation: Use Constructor Injection with final fields.");

    return sb.toString().replace("\\n", "<br>");
  }
}
