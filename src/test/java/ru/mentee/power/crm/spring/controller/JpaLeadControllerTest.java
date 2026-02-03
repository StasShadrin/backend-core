package ru.mentee.power.crm.spring.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import ru.mentee.power.crm.entity.Lead;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.spring.service.JpaLeadService;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(JpaLeadController.class)
class JpaLeadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JpaLeadService leadService;

    @Test
    void shouldShowLeadsList() throws Exception {
        // Given
        Lead lead = Lead.builder()
                .id(UUID.randomUUID())
                .name("Test")
                .email("test@example.com")
                .status(LeadStatus.NEW)
                .build();

        when(leadService.findLeads(null, null)).thenReturn(List.of(lead));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/jpa-leads"))
                .andExpect(status().isOk())
                .andExpect(view().name("jpa-leads/list"))
                .andExpect(model().attributeExists("leads"));
    }

    @Test
    void shouldShowCreateForm() throws Exception {
        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/jpa-leads/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("jpa-leads/create"))
                .andExpect(model().attributeExists("lead"));
    }

    @Test
    void shouldCreateLeadAndRedirect() throws Exception {
        // Given
        Lead lead = Lead.builder()
                .id(UUID.randomUUID())
                .name("Test")
                .email("test@example.com")
                .status(LeadStatus.NEW)
                .build();

        when(leadService.addLead(any())).thenReturn(lead);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/jpa-leads")
                        .param("name", "Test")
                        .param("email", "test@example.com")
                        .param("phone", "123")
                        .param("company", "ACME")
                        .param("status", "NEW"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/jpa-leads"));
    }

    @Test
    void shouldShowEditForm() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        Lead lead = Lead.builder()
                .id(id)
                .name("Test")
                .email("test@example.com")
                .status(LeadStatus.NEW)
                .build();

        when(leadService.findById(id)).thenReturn(Optional.of(lead));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/jpa-leads/" + id + "/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("jpa-leads/edit"))
                .andExpect(model().attributeExists("lead"));
    }

    @Test
    void shouldUpdateLeadAndRedirect() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        Lead lead = Lead.builder()
                .id(id)
                .name("Updated")
                .email("updated@example.com")
                .status(LeadStatus.NEW)
                .build();

        when(leadService.findById(id)).thenReturn(Optional.of(lead));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/jpa-leads/" + id)
                        .param("name", "Updated")
                        .param("email", "updated@example.com")
                        .param("phone", "123")
                        .param("company", "ACME")
                        .param("status", "NEW"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/jpa-leads"));
    }

    @Test
    void shouldDeleteLeadAndRedirect() throws Exception {
        // Given
        UUID id = UUID.randomUUID();

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/jpa-leads/" + id + "/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/jpa-leads"));
    }
}