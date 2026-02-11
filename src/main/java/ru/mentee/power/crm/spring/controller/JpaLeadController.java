package ru.mentee.power.crm.spring.controller;

import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ru.mentee.power.crm.entity.Company;
import ru.mentee.power.crm.entity.Lead;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.spring.service.JpaCompanyService;
import ru.mentee.power.crm.spring.service.JpaLeadService;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/** Контроллер для отображения списка лидов и работы с БД*/
@Controller
@RequestMapping("/jpa-leads")
@RequiredArgsConstructor
public class JpaLeadController {

    private static final String REDIRECT_LEADS = "redirect:/jpa-leads";
    private static final String LEADS_LIST = "jpa-leads/list";
    private static final String LEADS_CREATE = "jpa-leads/create";
    private static final String LEADS_EDIT = "jpa-leads/edit";

    private final JpaLeadService leadService;
    private final JpaCompanyService companyService;

    /** Обрабатывает GET-запрос /jpa-leads и отображает список лидов с фильтрацией */
    @GetMapping
    public String showLeads(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            Model model) {

        LeadStatus statusEnum = null;
        if (status != null && !status.isEmpty()) {
            statusEnum = LeadStatus.valueOf(status);
        }

        var leads = leadService.findLeads(search, statusEnum);
        model.addAttribute("leads", leads);
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("status", status != null ? status : "");
        model.addAttribute("currentFilter", statusEnum);
        return LEADS_LIST;
    }

    /** Показывает форму для создания нового лида */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        Lead lead = Lead.builder()
                .name("")
                .email("")
                .phone("")
                .status(LeadStatus.NEW)
                .build();

        model.addAttribute("lead", lead);
        return LEADS_CREATE;
    }

    /** Обрабатывает отправку формы и создаёт нового лида */
    @PostMapping
    public String createLead(
            @RequestParam(required = false) String companyName,
            @Valid @ModelAttribute Lead lead,
            BindingResult result) {

        if (result.hasErrors()) {
            return LEADS_CREATE;
        }

        Company company = null;
        if (companyName != null && !companyName.trim().isEmpty()) {
            company = companyService.findOrCreateByName(companyName.trim());
        }
        lead.setCompany(company);

        leadService.addLead(lead);
        return REDIRECT_LEADS;
    }

    /** Показывает форму редактирования существующего лида */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable UUID id, Model model) {
        Lead lead = leadService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Lead not found"));
        model.addAttribute("lead", lead);
        return LEADS_EDIT;
    }

    /** Обрабатывает отправку формы и обновляет данные лида */
    @PostMapping("/{id}")
    public String updateLead(
            @PathVariable UUID id,
            @RequestParam(required = false) String companyName,
            @Valid @ModelAttribute Lead lead,
            BindingResult result) {

        if (result.hasErrors()) {
            return LEADS_EDIT;
        }

        Company company = null;
        if (companyName != null && !companyName.trim().isEmpty()) {
            company = companyService.findOrCreateByName(companyName.trim());
        }
        lead.setCompany(company);

        leadService.update(id, lead);
        return REDIRECT_LEADS;
    }

    /** Удаление лида */
    @PostMapping("/{id}/delete")
    public String deleteLead(@PathVariable UUID id) {
        leadService.delete(id);
        return REDIRECT_LEADS;
    }
}