package ru.mentee.power.crm.spring.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.spring.service.LeadService;

/** Контроллер для отображения списка лидов */
@Controller
@RequiredArgsConstructor
public class LeadController {

  private static final String REDIRECT_LEADS = "redirect:/leads";
  private static final String LEADS_LIST = "leads/list";
  private static final String LEADS_CREATE = "leads/create";
  private static final String LEADS_EDIT = "leads/edit";

  private final LeadService leadService;

  /** Обрабатывает GET-запрос /leads и отображает список лидов с фильтрацией */
  @GetMapping("/leads")
  public String showLeads(
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String status,
      Model model) {

    LeadStatus statusEnum = null;
    if (status != null && !status.isEmpty()) {
      statusEnum = LeadStatus.valueOf(status);
    }

    List<Lead> leads = leadService.findLeads(search, statusEnum);
    model.addAttribute("leads", leads);
    model.addAttribute("search", search != null ? search : "");
    model.addAttribute("status", status != null ? status : "");
    model.addAttribute("currentFilter", statusEnum);
    return LEADS_LIST;
  }

  /** Показывает форму для создания нового лида */
  @GetMapping("/leads/new")
  public String showCreateForm(Model model) {
    model.addAttribute(
        "lead",
        Lead.builder()
            .id(null)
            .name("")
            .email("")
            .phone("")
            .company("")
            .status(LeadStatus.NEW)
            .build());
    return LEADS_CREATE;
  }

  /** Обрабатывает отправку формы и создаёт нового лида */
  @PostMapping("/leads")
  public String createLead(@Valid @ModelAttribute Lead lead, BindingResult result, Model model) {

    if (result.hasErrors()) {
      model.addAttribute("errors", result);
      return LEADS_CREATE;
    }
    leadService.addLead(lead);
    return REDIRECT_LEADS;
  }

  /** Тестовый эндпоинт */
  @GetMapping("/")
  @ResponseBody
  public String home() {
    return "Spring Boot CRM is running! Beans created: " + leadService.findAll().size() + " leads.";
  }

  /** Показывает форму редактирования существующего лида */
  @GetMapping("/leads/{id}/edit")
  public String showEditForm(@PathVariable UUID id, Model model) {
    Lead lead =
        leadService
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lead not found"));
    model.addAttribute("lead", lead);
    return LEADS_EDIT;
  }

  /** Обрабатывает отправку формы и обновляет данные лида */
  @PostMapping("/leads/{id}")
  public String updateLead(
      @PathVariable UUID id, @Valid @ModelAttribute Lead lead, BindingResult result, Model model) {
    if (result.hasErrors()) {
      model.addAttribute("errors", result);
      return LEADS_EDIT;
    }
    leadService.update(id, lead);
    return REDIRECT_LEADS;
  }

  /** Удаление лида */
  @PostMapping("/leads/{id}/delete")
  public String deleteLead(@PathVariable UUID id) {
    leadService.delete(id);
    return REDIRECT_LEADS;
  }
}
