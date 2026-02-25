package ru.mentee.power.crm.spring.rest;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.mentee.power.crm.spring.dto.generated.CreateLeadRequest;
import ru.mentee.power.crm.spring.dto.generated.LeadResponse;
import ru.mentee.power.crm.spring.dto.generated.UpdateLeadRequest;
import ru.mentee.power.crm.spring.rest.generated.LeadManagementApi;
import ru.mentee.power.crm.spring.service.LeadRestServiceAdapter;

/** REST контроллер для работы с лидами (возвращает JSON) */
@Slf4j
@RequiredArgsConstructor
@RestController
@Validated
public class LeadRestController implements LeadManagementApi {
  private final LeadRestServiceAdapter leadRestServiceAdapter;

  @Override
  public ResponseEntity<List<LeadResponse>> getLeads() {
    return ResponseEntity.ok(leadRestServiceAdapter.findAllLeads());
  }

  @Override
  public ResponseEntity<LeadResponse> getLeadById(UUID id) {
    return ResponseEntity.ok(leadRestServiceAdapter.findLeadById(id));
  }

  @Override
  public ResponseEntity<LeadResponse> createLead(@Valid CreateLeadRequest lead) {
    LeadResponse createdLead = leadRestServiceAdapter.createLead(lead);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(createdLead.getId())
            .toUri();
    return ResponseEntity.created(location).body(createdLead);
  }

  @Override
  public ResponseEntity<LeadResponse> updateLead(UUID id, @Valid UpdateLeadRequest request) {
    LeadResponse updatedLead = leadRestServiceAdapter.updateLead(id, request);
    log.info("Lead successfully updated: {}", id);
    return ResponseEntity.ok(updatedLead);
  }

  @Override
  public ResponseEntity<Void> deleteLead(UUID id) {
    leadRestServiceAdapter.deleteLead(id);
    return ResponseEntity.noContent().build();
  }
}
