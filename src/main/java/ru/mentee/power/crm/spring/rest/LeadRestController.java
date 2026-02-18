package ru.mentee.power.crm.spring.rest;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
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
  public ResponseEntity<List<Lead>> getAllLeads() {
    List<Lead> leads = leadRepository.findAllWithCompany();
    return ResponseEntity.ok(leads);
  }

  /** Возвращает лида по ID или null если не найден */
  @GetMapping("/{id}")
  public ResponseEntity<Lead> getLeadById(@PathVariable UUID id) {
    return leadRepository
        .findByIdWithCompany(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /** Создает нового лида из JSON и возвращает сохраненный объект */
  @PostMapping
  public ResponseEntity<Lead> createLead(@RequestBody Lead lead) {
    Lead createdLead = leadService.createLead(lead);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(createdLead.getId())
            .encode()
            .toUri();
    return ResponseEntity.created(location).body(createdLead);
  }

  /** Обновляет текущего лида и возвращает сохраненный объект */
  @PutMapping("/{id}")
  public ResponseEntity<Lead> updateLead(@PathVariable UUID id, @RequestBody Lead lead) {
    return leadService
        .updateLead(id, lead)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /** Удаляет лида, если есть(204), ели нет(404) */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteLead(@PathVariable UUID id) {
    if (leadService.deleteLead(id)) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.notFound().build();
  }
}
