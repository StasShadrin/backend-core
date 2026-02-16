package ru.mentee.power.crm.spring.rest;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mentee.power.crm.entity.Lead;
import ru.mentee.power.crm.spring.repository.JpaLeadRepository;
import ru.mentee.power.crm.spring.service.JpaLeadService;

/** REST контроллер для работы с лидами (возвращает JSON) */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/leads")
public class LeadRestController {

  private final JpaLeadService leadService;
  private final JpaLeadRepository leadRepository;

  /** Возвращает список всех лидов с информацией о компаниях */
  @GetMapping
  public List<Lead> getAllLeads() {
    return leadRepository.findAllWithCompany();
  }

  /** Возвращает лида по ID или null если не найден */
  @GetMapping("/{id}")
  public Lead getLeadById(@PathVariable UUID id) {
    return leadRepository.findByIdWithCompany(id).orElse(null);
  }

  /** Создает нового лида из JSON и возвращает сохраненный объект */
  @PostMapping
  public Lead createLead(@RequestBody Lead lead) {
    return leadService.addLead(lead);
  }
}
