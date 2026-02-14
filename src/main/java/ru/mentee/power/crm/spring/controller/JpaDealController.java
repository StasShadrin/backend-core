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
import ru.mentee.power.crm.entity.Lead;
import ru.mentee.power.crm.spring.dto.CreateDealRequest;
import ru.mentee.power.crm.spring.service.JpaDealService;
import ru.mentee.power.crm.spring.service.JpaLeadService;

/** JPA-версия контроллера для управления сделками. */
@Controller
@RequestMapping("/jpa-deals")
@RequiredArgsConstructor
public class JpaDealController {

  private static final String DEALS_LIST = "jpa-deals/list";
  private static final String DEALS_KANBAN = "jpa-deals/kanban";
  private static final String DEALS_CONVERT = "jpa-deals/convert";
  private static final String REDIRECT_DEALS = "redirect:/jpa-deals";
  private static final String REDIRECT_DEALS_KANBAN = "redirect:/jpa-deals/kanban";

  private final JpaDealService dealService;
  private final JpaLeadService leadService;

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
    Lead lead =
        leadService
            .findById(leadId)
            .orElseThrow(() -> new RuntimeException("Lead not found: " + leadId));
    model.addAttribute("lead", lead);
    return DEALS_CONVERT;
  }

  /** Создаёт новую сделку из существующего лида. */
  @PostMapping("/convert")
  public String convertLeadToDeal(
      @RequestParam UUID leadId,
      @RequestParam String title,
      @RequestParam BigDecimal amount,
      @RequestParam UUID companyId) {
    CreateDealRequest request = new CreateDealRequest();
    request.setTitle(title);
    request.setAmount(amount);
    request.setCompanyId(companyId);

    leadService.convertLeadToDeal(leadId, request);
    return REDIRECT_DEALS;
  }

  /** Изменяет статус существующей сделки. */
  @PostMapping("/{id}/transition")
  public String transitionStatus(@PathVariable UUID id, @RequestParam DealStatus newStatus) {
    dealService.transitionDealStatus(id, newStatus);
    return REDIRECT_DEALS_KANBAN;
  }
}
