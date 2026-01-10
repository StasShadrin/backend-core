package ru.mentee.power.crm.spring.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
}
