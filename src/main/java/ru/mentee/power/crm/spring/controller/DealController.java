package ru.mentee.power.crm.spring.controller;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.mentee.power.crm.domain.DealStatus;
import ru.mentee.power.crm.spring.service.DealService;
import ru.mentee.power.crm.spring.service.LeadService;

/**
 * Контроллер для управления сделками (Deal) в CRM. Обрабатывает операции: просмотр списка,
 * конвертация лида, изменение статуса, отображение воронки.
 */
@Controller
@RequestMapping("/deals")
@RequiredArgsConstructor
public class DealController {

  private static final String DEALS_LIST = "deals/list";
  private static final String DEALS_KANBAN = "deals/kanban";
  private static final String DEALS_CONVERT = "deals/convert";
  private static final String REDIRECT_DEALS = "redirect:/deals";
  private static final String REDIRECT_DEALS_KANBAN = "redirect:/deals/kanban";

  private final DealService dealService;
  private final LeadService leadService;

  /** Отображает список всех сделок. */
  @GetMapping
  public String listDeals(Model model) {
    model.addAttribute("deals", dealService.getAllDeals());
    return DEALS_LIST;
  }

  /** Отображает Kanban-доску воронки продаж. */
  @GetMapping("/kanban")
  public String kanbanView(Model model) {
    model.addAttribute("dealsByStatus", dealService.getDealsByStatusForKanban());
    return DEALS_KANBAN;
  }

  /** Показывает форму конвертации лида в сделку. */
  @GetMapping("/convert/{leadId}")
  public String showConvertForm(@PathVariable UUID leadId, Model model) {
    model.addAttribute(
        "lead",
        leadService
            .findById(leadId)
            .orElseThrow(() -> new RuntimeException("Lead not found: " + leadId)));
    return DEALS_CONVERT;
  }

  /** Создаёт новую сделку из существующего лида. */
  @PostMapping("/convert")
  public String convertLeadToDeal(@RequestParam UUID leadId, @RequestParam BigDecimal amount) {
    leadService.convertLeadToDeal(leadId, amount);
    return REDIRECT_DEALS;
  }

  /** Изменяет статус существующей сделки. */
  @PostMapping("/{id}/transition")
  public String transitionStatus(@PathVariable UUID id, @RequestParam DealStatus newStatus) {
    dealService.transitionDealStatus(id, newStatus);
    return REDIRECT_DEALS_KANBAN;
  }
}
