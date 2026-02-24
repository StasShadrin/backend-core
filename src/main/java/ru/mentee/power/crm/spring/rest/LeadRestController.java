package ru.mentee.power.crm.spring.rest;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.mentee.power.crm.spring.dto.CreateLeadRequest;
import ru.mentee.power.crm.spring.dto.LeadResponse;
import ru.mentee.power.crm.spring.dto.UpdateLeadRequest;
import ru.mentee.power.crm.spring.service.LeadRestServiceAdapter;

/** REST контроллер для работы с лидами (возвращает JSON) */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/leads")
@Validated
public class LeadRestController {
  private final LeadRestServiceAdapter leadRestServiceAdapter;

  /** Возвращает список всех лидов с информацией о компаниях */
  @GetMapping
  public ResponseEntity<List<LeadResponse>> getAllLeads() {
    return ResponseEntity.ok(leadRestServiceAdapter.findAllLeads());
  }

  @GetMapping("/{id}")
  public ResponseEntity<LeadResponse> getLeadById(@PathVariable UUID id) {
    LeadResponse lead = leadRestServiceAdapter.findLeadById(id);
    return ResponseEntity.ok(lead);
  }

  /** Создает нового лида из JSON и возвращает сохраненный объект */
  @PostMapping
  public ResponseEntity<LeadResponse> createLead(@Valid @RequestBody CreateLeadRequest lead) {
    LeadResponse createdLead = leadRestServiceAdapter.createLead(lead);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(createdLead.id())
            .toUri();
    return ResponseEntity.created(location).body(createdLead);
  }

  /** Обновляет текущего лида и возвращает сохраненный объект */
  @PutMapping("/{id}")
  public ResponseEntity<LeadResponse> updateLead(
      @PathVariable UUID id, @Valid @RequestBody UpdateLeadRequest request) {
    LeadResponse updatedLead = leadRestServiceAdapter.updateLead(id, request);
    log.info("Lead successfully updated: {}", id);
    return ResponseEntity.ok(updatedLead);
  }

  /** Удаляет лида, если есть(204), ели нет(404) */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteLead(@PathVariable UUID id) {
    leadRestServiceAdapter.deleteLead(id);
    return ResponseEntity.noContent().build();
  }
}
