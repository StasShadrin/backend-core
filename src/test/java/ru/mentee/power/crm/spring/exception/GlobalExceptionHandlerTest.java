package ru.mentee.power.crm.spring.exception;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.mentee.power.crm.spring.rest.LeadRestController;
import ru.mentee.power.crm.spring.service.LeadRestServiceAdapter;

@WebMvcTest(LeadRestController.class)
class GlobalExceptionHandlerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private LeadRestServiceAdapter service;

  @Test
  void shouldReturn404_whenEntityNotFound() throws Exception {
    UUID id = UUID.randomUUID();

    when(service.findLeadById(id)).thenThrow(new EntityNotFoundException("Lead", id.toString()));

    mockMvc
        .perform(get("/api/leads/{id}", id))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Lead not found with id: " + id))
        .andExpect(jsonPath("$.path").value("/api/leads/" + id))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void shouldReturn400WithFieldErrors_whenValidationFails() throws Exception {
    String invalidJson =
        """
                {
                    "name": "A",
                    "email": "",
                    "phone": "",
                    "companyId": "123e4567-e89b-12d3-a456-426614174000"
                }
                """;

    mockMvc
        .perform(post("/api/leads").contentType(MediaType.APPLICATION_JSON).content(invalidJson))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.path").value("/api/leads"))
        .andExpect(jsonPath("$.errors").exists())
        .andExpect(jsonPath("$.errors.name").exists())
        .andExpect(jsonPath("$.errors.email").exists())
        .andExpect(jsonPath("$.errors.phone").exists());
  }

  @Test
  void shouldReturn500_whenUnexpectedExceptionOccurs() throws Exception {
    UUID id = UUID.randomUUID();

    when(service.findLeadById(id)).thenThrow(new RuntimeException());
    mockMvc
        .perform(get("/api/leads/{id}", id))
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.error").value("Internal Server Error"))
        .andExpect(jsonPath("$.message").value("Internal server error occurred. Contact support."))
        .andExpect(jsonPath("$.path").value("/api/leads/" + id))
        .andExpect(
            jsonPath("$.message")
                .value(
                    org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("Database connection failed"))));
  }
}
