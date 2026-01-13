package ru.mentee.power.crm.spring.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.service.LeadService;

/** Контроллер для отображения списка лидов */
@Controller
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;

    /** Обрабатывает GET-запрос /leads и отображает список лидов */
    @GetMapping("/leads")
    public String showLeads(
            @RequestParam(required = false) LeadStatus status,
            Model model) {
        List<Lead> list = (status == null)
                ? leadService.findAll()
                : leadService.findByStatus(status);
        model.addAttribute("leads", list);
        model.addAttribute("currentFilter", status);
        return "leads/list";
    }

    /** Показывает форму для создания нового лида */
    @GetMapping("/leads/new")
    public String showCreateForm(Model model) {
        model.addAttribute("lead", new Lead(null, "", "", "", LeadStatus.NEW));
        return "leads/create";
    }

    /** Обрабатывает отправку формы и создаёт нового лида */
    @PostMapping("/leads")
    public String createLead(@ModelAttribute Lead lead) {
        leadService.addLead(lead.email(), lead.phone(), lead.company(), lead.status());
        return "redirect:/leads";
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
        Lead lead = leadService.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Lead not found"));
        model.addAttribute("lead", lead);
        return "leads/edit";
    }

    /** Обрабатывает отправку формы и обновляет данные лида */
    @PostMapping("/leads/{id}")
    public String updateLead(@PathVariable UUID id, @ModelAttribute Lead lead) {
        leadService.update(id, lead);
        return "redirect:/leads";
    }
}
