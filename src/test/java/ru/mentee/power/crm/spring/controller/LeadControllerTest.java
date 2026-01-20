package ru.mentee.power.crm.spring.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.model.LeadBuilder;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.spring.service.LeadService;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(LeadController.class)
class LeadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LeadService leadService;

    private Lead existingLead;
    private UUID validId;

    @BeforeEach
    void setUp() {
        validId = UUID.randomUUID();
        existingLead = LeadBuilder.builder()
                .id(validId)
                .email("test@example.com")
                .phone("+79991234567")
                .company("Test")
                .status(LeadStatus.NEW)
                .build();
    }

    // GET /leads/{id}/edit - форма редактирования
    @Test
    void shouldShowEditFormForExistingLead() throws Exception {
        // Given
        when(leadService.findById(validId)).thenReturn(Optional.of(existingLead));

        // When & Then
        mockMvc.perform(get("/leads/{id}/edit", validId))
                .andExpect(status().isOk())
                .andExpect(view().name("leads/edit"))
                .andExpect(model().attribute("lead", existingLead));
    }

    // POST /leads/{id} - обновление лида + redirect
    @Test
    void shouldUpdateLeadAndRedirectToList() throws Exception {
        // Given
        when(leadService.findById(validId)).thenReturn(Optional.of(existingLead));

        // When & Then
        mockMvc.perform(post("/leads/{id}", validId)
                        .param("name", "test")
                        .param("email", "updated@example.com")
                        .param("phone", "+79999999999")
                        .param("company", "Updated Corp")
                        .param("status", "LOST")
                        .param("id", validId.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/leads"));

        // Проверяем, что update был вызван с правильными аргументами
        verify(leadService).update(eq(validId), argThat(lead ->
                "updated@example.com".equals(lead.email()) &&
                "+79999999999".equals(lead.phone()) &&
                "Updated Corp".equals(lead.company()) &&
                LeadStatus.LOST.equals(lead.status())
        ));
    }

    // GET /leads/{invalid-id}/edit - 404 Not Found
    @Test
    void shouldReturn404ForNonExistentLeadId() throws Exception {
        // Given
        UUID invalidId = UUID.randomUUID(); // не существует в репозитории
        when(leadService.findById(invalidId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/leads/{id}/edit", invalidId))
                .andExpect(status().isNotFound());
    }

    // GET /leads/new - форма создания
    @Test
    void shouldShowCreateForm() throws Exception {
        // When & Then
        mockMvc.perform(get("/leads/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("leads/create"))
                .andExpect(model().attributeExists("lead"));
    }

    // POST /leads - создание нового лида + redirect
    @Test
    void shouldCreateLeadAndRedirectToList() throws Exception {
        // Given
        Lead newLead = LeadBuilder.builder()
                .id(UUID.randomUUID())
                .email("new@example.com")
                .phone("+79991234567")
                .company("New Company")
                .status(LeadStatus.NEW)
                .build();

        // Мокируем addLead, чтобы он возвращал новый лид
        when(leadService.addLead(LeadBuilder.builder()
                .name("Test")
                .email("new@example.com")
                .phone("+79991234567")
                .company("New Company")
                .status(LeadStatus.NEW)
                .build()
        )).thenReturn(newLead);

        // When & Then
        mockMvc.perform(post("/leads")
                        .param("name", "Test")
                        .param("email", "new@example.com")
                        .param("phone", "+79991234567")
                        .param("company", "New Company")
                        .param("status", "NEW")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/leads"));

        // Проверяем, что сервис вызвал addLead с правильными параметрами
        verify(leadService).addLead(LeadBuilder.builder()
                .name("Test")
                .email("new@example.com")
                .phone("+79991234567")
                .company("New Company")
                .status(LeadStatus.NEW)
                .build()
        );
    }

    // POST /leads/{id}/delete - удаление лида + redirect
    @Test
    void shouldDeleteLeadAndRedirect() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(post("/leads/{id}/delete", id))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/leads"));

        verify(leadService).delete(id);
    }

    // GET /leads?search=ivan — поиск по email/company
    @Test
    void shouldFilterBySearchTerm() throws Exception {
        // Given
        String searchTerm = "test";
        Lead expectedLead = LeadBuilder.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .phone("+79991234567")
                .company("Test Company")
                .status(LeadStatus.NEW)
                .build();
        when(leadService.findLeads((searchTerm), (null)))
                .thenReturn(List.of(expectedLead));

        // When & Then
        mockMvc.perform(get("/leads")
                        .param("search", searchTerm))
                .andExpect(status().isOk())
                .andExpect(view().name("leads/list"))
                .andExpect(model().attribute("leads", List.of(expectedLead)))
                .andExpect(model().attribute("search", searchTerm))
                .andExpect(model().attribute("status", ""));
    }

    // GET /leads?status=NEW — фильтрация по статусу
    @Test
    void shouldFilterByStatus() throws Exception {
        // Given
        LeadStatus status = LeadStatus.NEW;
        Lead lead1 = LeadBuilder.builder()
                .id(UUID.randomUUID())
                .email("a@example.com")
                .phone("123")
                .company("A")
                .status(status)
                .build();
        Lead lead2 = LeadBuilder.builder()
                .id(UUID.randomUUID())
                .email("b@example.com")
                .phone("456")
                .company("B")
                .status(status)
                .build();
        List<Lead> expectedLeads = List.of(lead1, lead2);
        when(leadService.findLeads((null), (status)))
                .thenReturn(expectedLeads);

        // When & Then
        mockMvc.perform(get("/leads")
                        .param("status", status.name()))
                .andExpect(status().isOk())
                .andExpect(view().name("leads/list"))
                .andExpect(model().attribute("leads", expectedLeads))
                .andExpect(model().attribute("search", ""))
                .andExpect(model().attribute("status", status.name()));
    }

    // GET /leads — без параметров, возвращаются все лиды
    @Test
    void shouldReturnAllLeadsWhenNoParameters() throws Exception {
        // Given
        Lead lead1 = LeadBuilder.builder()
                .id(UUID.randomUUID())
                .email("a@example.com")
                .phone("123")
                .company("A")
                .status(LeadStatus.NEW)
                .build();
        Lead lead2 = LeadBuilder.builder()
                .id(UUID.randomUUID())
                .email("b@example.com")
                .phone("456")
                .company("B")
                .status(LeadStatus.CONTACTED)
                .build();
        List<Lead> allLeads = List.of(lead1, lead2);
        when(leadService.findLeads((null), null))
                .thenReturn(allLeads);

        // When & Then
        mockMvc.perform(get("/leads"))
                .andExpect(status().isOk())
                .andExpect(view().name("leads/list"))
                .andExpect(model().attribute("leads", allLeads))
                .andExpect(model().attribute("search", ""))
                .andExpect(model().attribute("status", ""));
    }

    // GET /leads?search=test&status=NEW — комбинированный фильтр
    @Test
    void shouldCombineSearchAndStatusFilters() throws Exception {
        // Given
        String searchTerm = "acme";
        LeadStatus status = LeadStatus.NEW;
        Lead expectedLead = LeadBuilder.builder()
                .id(UUID.randomUUID())
                .email("contact@acme.com")
                .phone("+79990000000")
                .company("Acme Inc")
                .status(status)
                .build();
        when(leadService.findLeads((searchTerm), (status)))
                .thenReturn(List.of(expectedLead));

        // When & Then
        mockMvc.perform(get("/leads")
                        .param("search", searchTerm)
                        .param("status", status.name()))
                .andExpect(status().isOk())
                .andExpect(view().name("leads/list"))
                .andExpect(model().attribute("leads", List.of(expectedLead)))
                .andExpect(model().attribute("search", searchTerm))
                .andExpect(model().attribute("status", status.name()));
    }

    @Test
    void shouldReturnFormWhenNameIsEmpty() throws Exception {
        mockMvc.perform(post("/leads")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "")
                        .param("email", "test@example.com")
                        .param("phone", "+71234567890")
                        .param("company", "Acme")
                        .param("status", "NEW"))
                .andExpect(status().isOk())
                .andExpect(view().name("leads/create"))
                .andExpect(model().attributeHasFieldErrors("lead", "name"));
    }

    @Test
    void shouldReturnFormWhenEmailIsInvalid() throws Exception {
        mockMvc.perform(post("/leads")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Иван")
                        .param("email", "invalid-email") // ← без @
                        .param("phone", "+71234567890")
                        .param("company", "Acme")
                        .param("status", "NEW"))
                .andExpect(status().isOk())
                .andExpect(view().name("leads/create"))
                .andExpect(model().attributeHasFieldErrors("lead", "email"))
                .andExpect(model().attributeHasFieldErrorCode("lead", "email", "Email"));
    }

    @Test
    void shouldRedirectWhenAllFieldsAreValid() throws Exception {
        mockMvc.perform(post("/leads")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Иван")
                        .param("email", "ivan@example.com")
                        .param("phone", "+71234567890")
                        .param("company", "Acme")
                        .param("status", "NEW"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/leads"));
    }

    @Test
    void shouldReturnFormWhenNameIsTooShort() throws Exception {
        mockMvc.perform(post("/leads")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "A") // ← 1 символ
                        .param("email", "test@example.com")
                        .param("phone", "+71234567890")
                        .param("company", "Acme")
                        .param("status", "NEW"))
                .andExpect(status().isOk())
                .andExpect(view().name("leads/create"))
                .andExpect(model().attributeHasFieldErrors("lead", "name"));
    }

    @Test
    void shouldReturnFormWhenCompanyIsTooLong() throws Exception {
        String longName = "A".repeat(101);
        mockMvc.perform(post("/leads")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Иван")
                        .param("email", "test@example.com")
                        .param("phone", "+71234567890")
                        .param("company", longName)
                        .param("status", "NEW"))
                .andExpect(status().isOk())
                .andExpect(view().name("leads/create"))
                .andExpect(model().attributeHasFieldErrors("lead", "company"));
    }
}