package ru.mentee.power.crm.spring.rest;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.mentee.power.crm.entity.Company;
import ru.mentee.power.crm.entity.Lead;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.spring.repository.JpaLeadRepository;
import ru.mentee.power.crm.spring.service.JpaLeadService;

@WebMvcTest(LeadRestController.class)
class LeadRestControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private JpaLeadService leadService;

  @MockitoBean private JpaLeadRepository jpaLeadRepository;

  @Test
  @DisplayName("Должен вернуть 200 OK со списком лидов")
  void shouldReturn200_whenGetAllLeads() throws Exception {
    Company company =
        Company.builder().id(UUID.randomUUID()).name("Тестовая компания").industry("IT").build();

    Lead leadFirst =
        Lead.builder()
            .id(UUID.randomUUID())
            .name("Иван Иванов")
            .email("ivan@example.com")
            .phone("+79991234567")
            .status(LeadStatus.NEW)
            .company(company)
            .createdAt(OffsetDateTime.now())
            .updatedAt(OffsetDateTime.now())
            .build();

    Lead leadSecond =
        Lead.builder()
            .id(UUID.randomUUID())
            .name("Петр Петров")
            .email("petr@example.com")
            .phone("+79992345678")
            .status(LeadStatus.CONTACTED)
            .company(company)
            .createdAt(OffsetDateTime.now())
            .updatedAt(OffsetDateTime.now())
            .build();

    List<Lead> leads = List.of(leadFirst, leadSecond);

    when(jpaLeadRepository.findAllWithCompany()).thenReturn(leads);

    mockMvc
        .perform(get("/api/leads").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id").value(leadFirst.getId().toString()))
        .andExpect(jsonPath("$[0].name").value("Иван Иванов"))
        .andExpect(jsonPath("$[0].email").value("ivan@example.com"))
        .andExpect(jsonPath("$[0].status").value("NEW"))
        .andExpect(jsonPath("$[1].id").value(leadSecond.getId().toString()))
        .andExpect(jsonPath("$[1].name").value("Петр Петров"))
        .andExpect(jsonPath("$[1].email").value("petr@example.com"))
        .andExpect(jsonPath("$[1].status").value("CONTACTED"));
  }

  @Test
  @DisplayName("Должен вернуть 404 Not Found при запросе несуществующего лида")
  void shouldReturn404_whenGetNonExistentLead() throws Exception {
    UUID nonExistentId = UUID.randomUUID();

    when(jpaLeadRepository.findByIdWithCompany(nonExistentId)).thenReturn(Optional.empty());

    mockMvc
        .perform(get("/api/leads/" + nonExistentId).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Должен вернуть 201 Created с Location header при создании лида")
  void shouldReturn201WithLocation_whenCreateLead() throws Exception {
    UUID generatedId = UUID.randomUUID();

    Company company =
        Company.builder().id(UUID.randomUUID()).name("Тестовая компания").industry("IT").build();

    Lead leadToCreate =
        Lead.builder()
            .name("Иван Иванов")
            .email("ivan@example.com")
            .phone("+79991234567")
            .status(LeadStatus.NEW)
            .company(company)
            .build();

    Lead createdLead =
        Lead.builder()
            .id(generatedId)
            .name(leadToCreate.getName())
            .email(leadToCreate.getEmail())
            .phone(leadToCreate.getPhone())
            .status(leadToCreate.getStatus())
            .company(leadToCreate.getCompany())
            .createdAt(OffsetDateTime.now())
            .updatedAt(OffsetDateTime.now())
            .build();

    when(leadService.createLead(any(Lead.class))).thenReturn(createdLead);

    mockMvc
        .perform(
            post("/api/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(leadToCreate)))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(header().string("Location", containsString("/api/leads/" + generatedId)))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(generatedId.toString()))
        .andExpect(jsonPath("$.name").value("Иван Иванов"))
        .andExpect(jsonPath("$.email").value("ivan@example.com"))
        .andExpect(jsonPath("$.status").value("NEW"));
  }

  @Test
  @DisplayName("Должен вернуть 204 No Content при удалении существующего лида")
  void shouldReturn204_whenDeleteExistingLead() throws Exception {
    UUID existingId = UUID.randomUUID();

    when(leadService.deleteLead(existingId)).thenReturn(true);

    mockMvc
        .perform(delete("/api/leads/" + existingId).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent())
        .andExpect(content().string(""));

    verify(leadService, times(1)).deleteLead(existingId);
  }

  @Test
  @DisplayName("Должен вернуть 404 Not Found при удалении несуществующего лида")
  void shouldReturn404_whenDeleteNonExistentLead() throws Exception {
    UUID nonExistentId = UUID.randomUUID();

    when(leadService.deleteLead(nonExistentId)).thenReturn(false);

    mockMvc
        .perform(delete("/api/leads/" + nonExistentId).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }
}
