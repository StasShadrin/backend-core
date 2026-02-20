package ru.mentee.power.crm.spring.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.spring.dto.CreateLeadRequest;
import ru.mentee.power.crm.spring.dto.LeadResponse;
import ru.mentee.power.crm.spring.repository.CompanyRepository;
import ru.mentee.power.crm.spring.service.LeadRestServiceAdapter;

@WebMvcTest(LeadRestController.class)
class LeadRestControllerValidationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private LeadRestServiceAdapter leadService;

  @MockitoBean private CompanyRepository companyRepository;

  @Test
  void shouldReturn400_whenEmailIsBlank() throws Exception {
    // Given: CreateLeadRequest с пустым email
    CreateLeadRequest request = new CreateLeadRequest();
    request.setName("Bob");
    request.setEmail("");
    request.setPhone("+7123456789");

    String requestJson = objectMapper.writeValueAsString(request);

    mockMvc
        .perform(post("/api/leads").contentType(MediaType.APPLICATION_JSON).content(requestJson))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn400_whenEmailIsInvalidFormat() throws Exception {
    // Given: CreateLeadRequest с email = "not-an-email" (без @)
    CreateLeadRequest request = new CreateLeadRequest();
    request.setName("Bob");
    request.setEmail("bobex.com");
    request.setPhone("+7123456789");

    String requestJson = objectMapper.writeValueAsString(request);

    mockMvc
        .perform(post("/api/leads").contentType(MediaType.APPLICATION_JSON).content(requestJson))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn400_whenFirstNameIsTooShort() throws Exception {
    // Given: CreateLeadRequest с firstName = "A" (1 символ, нужно минимум 2)
    CreateLeadRequest request = new CreateLeadRequest();
    request.setName("A");
    request.setEmail("bob@ex.com");
    request.setPhone("+7123456789");

    String requestJson = objectMapper.writeValueAsString(request);

    mockMvc
        .perform(post("/api/leads").contentType(MediaType.APPLICATION_JSON).content(requestJson))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn201_whenAllFieldsAreValid() throws Exception {
    // Given: CreateLeadRequest со всеми корректными полями
    UUID companyId = UUID.randomUUID();

    LeadResponse expectedResponse =
        new LeadResponse(
            UUID.randomUUID(),
            "Bob",
            "bob@ex.com",
            "+7123456789",
            companyId,
            LeadStatus.NEW,
            OffsetDateTime.now(ZoneOffset.UTC));

    when(leadService.createLead(any(CreateLeadRequest.class))).thenReturn(expectedResponse);

    CreateLeadRequest request = new CreateLeadRequest();
    request.setName("Bob");
    request.setEmail("bob@ex.com");
    request.setPhone("+7123456789");
    request.setCompanyId(companyId);

    String requestJson = objectMapper.writeValueAsString(request);

    mockMvc
        .perform(post("/api/leads").contentType(MediaType.APPLICATION_JSON).content(requestJson))
        .andExpect(status().isCreated());

    verify(leadService).createLead(any(CreateLeadRequest.class));
  }
}
