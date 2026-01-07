package ru.mentee.power.crm.spring.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.service.LeadService;

/** Контроллер для отображения списка лидов */
@Controller
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;

    /** Обрабатывает GET-запрос /leads и отображает список лидов */
    @GetMapping("/leads")
    public String showLeads(Model model) {
        List<Lead> list = leadService.findAll();
        model.addAttribute("leads", list);
        return "leads/list";
    }
}
