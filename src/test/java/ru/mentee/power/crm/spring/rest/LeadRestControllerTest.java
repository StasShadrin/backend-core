package ru.mentee.power.crm.spring.rest;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.mentee.power.crm.spring.dto.generated.LeadResponse.StatusEnum;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.mentee.power.crm.spring.dto.generated.CreateLeadRequest;
import ru.mentee.power.crm.spring.dto.generated.LeadResponse;
import ru.mentee.power.crm.spring.dto.generated.UpdateLeadRequest;
import ru.mentee.power.crm.spring.exception.EntityNotFoundException;
import ru.mentee.power.crm.spring.repository.JpaLeadRepository;
import ru.mentee.power.crm.spring.service.JpaLeadService;
import ru.mentee.power.crm.spring.service.LeadRestServiceAdapter;

@WebMvcTest(LeadRestController.class)
class LeadRestControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private JpaLeadService leadService;

  @MockitoBean private LeadRestServiceAdapter leadRestServiceAdapter;

  @MockitoBean private JpaLeadRepository jpaLeadRepository;

  @Test
  @DisplayName("Должен вернуть 200 OK со списком лидов")
  void shouldReturn200_whenGetAllLeads() throws Exception {
    UUID companyId = UUID.randomUUID();
    UUID leadFirstId = UUID.randomUUID();
    UUID leadSecondId = UUID.randomUUID();
    OffsetDateTime now = OffsetDateTime.now();

    LeadResponse responseFirst =
        new LeadResponse(
            leadFirstId,
            "Иван Иванов",
            "ivan@example.com",
            "+79991234567",
            companyId,
            StatusEnum.NEW,
            now);

    LeadResponse responseSecond =
        new LeadResponse(
            leadSecondId,
            "Петр Петров",
            "petr@example.com",
            "+79992345678",
            companyId,
            StatusEnum.CONTACTED,
            now);

    when(leadRestServiceAdapter.findAllLeads()).thenReturn(List.of(responseFirst, responseSecond));

    mockMvc
        .perform(get("/api/leads"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id").value(leadFirstId.toString()))
        .andExpect(jsonPath("$[0].name").value("Иван Иванов"))
        .andExpect(jsonPath("$[0].email").value("ivan@example.com"))
        .andExpect(jsonPath("$[0].status").value("NEW"))
        .andExpect(jsonPath("$[1].id").value(leadSecondId.toString()))
        .andExpect(jsonPath("$[1].name").value("Петр Петров"))
        .andExpect(jsonPath("$[1].email").value("petr@example.com"))
        .andExpect(jsonPath("$[1].status").value("CONTACTED"));
  }

  @Test
  @DisplayName("Должен вернуть 200 OK с лидом по ID")
  void shouldReturn200_whenGetLeadById() throws Exception {
    UUID leadId = UUID.randomUUID();
    UUID companyId = UUID.randomUUID();
    OffsetDateTime now = OffsetDateTime.now();

    LeadResponse response =
        new LeadResponse(
            leadId,
            "Иван Иванов",
            "ivan@example.com",
            "+79991234567",
            companyId,
            StatusEnum.NEW,
            now);

    when(leadRestServiceAdapter.findLeadById(leadId)).thenReturn(response);

    mockMvc
        .perform(get("/api/leads/" + leadId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(leadId.toString()))
        .andExpect(jsonPath("$.name").value("Иван Иванов"));
  }

  @Test
  @DisplayName("Должен вернуть 404 Not Found при запросе несуществующего лида")
  void shouldReturn404_whenGetNonExistentLead() throws Exception {
    UUID nonExistentId = UUID.randomUUID();

    when(leadRestServiceAdapter.findLeadById(nonExistentId))
        .thenThrow(new EntityNotFoundException("Lead", nonExistentId.toString()));

    mockMvc.perform(get("/api/leads/" + nonExistentId)).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Должен вернуть 201 Created с Location header при создании лида")
  void shouldReturn201WithLocation_whenCreateLead() throws Exception {
    UUID companyId = UUID.randomUUID();
    UUID generatedId = UUID.randomUUID();
    OffsetDateTime now = OffsetDateTime.now();

    CreateLeadRequest request =
        new CreateLeadRequest("Иван Иванов", "ivan@example.com", "+79991234567", companyId);

    LeadResponse response =
        new LeadResponse(
            generatedId,
            "Иван Иванов",
            "ivan@example.com",
            "+79991234567",
            companyId,
            StatusEnum.NEW,
            now);

    when(leadRestServiceAdapter.createLead(any(CreateLeadRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/api/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(header().string("Location", containsString("/api/leads/" + generatedId)))
        .andExpect(jsonPath("$.id").value(generatedId.toString()))
        .andExpect(jsonPath("$.name").value("Иван Иванов"))
        .andExpect(jsonPath("$.email").value("ivan@example.com"))
        .andExpect(jsonPath("$.status").value("NEW"));
  }

  @Test
  @DisplayName("Должен вернуть 200 OK при обновлении лида")
  void shouldReturn200_whenUpdateLead() throws Exception {
    UUID leadId = UUID.randomUUID();
    UUID companyId = UUID.randomUUID();
    OffsetDateTime now = OffsetDateTime.now();

    UpdateLeadRequest request =
        new UpdateLeadRequest()
            .name("Иван Петров")
            .email("ivan.petrov@example.com")
            .phone("+79991112233")
            .companyId(companyId)
            .status(UpdateLeadRequest.StatusEnum.CONTACTED);

    LeadResponse response =
        new LeadResponse(
            leadId,
            "Иван Петров",
            "ivan.petrov@example.com",
            "+79991112233",
            companyId,
            StatusEnum.CONTACTED,
            now);

    when(leadRestServiceAdapter.updateLead(any(UUID.class), any(UpdateLeadRequest.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            put("/api/leads/" + leadId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(leadId.toString()))
        .andExpect(jsonPath("$.name").value("Иван Петров"))
        .andExpect(jsonPath("$.status").value("CONTACTED"));
  }

  @Test
  @DisplayName("Должен вернуть 204 No Content при удалении существующего лида")
  void shouldReturn204_whenDeleteExistingLead() throws Exception {
    UUID existingId = UUID.randomUUID();

    mockMvc.perform(delete("/api/leads/" + existingId)).andExpect(status().isNoContent());

    verify(leadRestServiceAdapter, times(1)).deleteLead(existingId);
  }

  @Test
  @DisplayName("Должен вернуть 404 Not Found при удалении несуществующего лида")
  void shouldReturn404_whenDeleteNonExistentLead() throws Exception {
    UUID nonExistentId = UUID.randomUUID();

    doThrow(new EntityNotFoundException("Lead", nonExistentId.toString()))
        .when(leadRestServiceAdapter)
        .deleteLead(nonExistentId);

    mockMvc.perform(delete("/api/leads/" + nonExistentId)).andExpect(status().isNotFound());

    verify(leadRestServiceAdapter, times(1)).deleteLead(nonExistentId);
  }
}
