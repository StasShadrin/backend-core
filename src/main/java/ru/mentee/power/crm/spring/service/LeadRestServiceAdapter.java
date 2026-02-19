package ru.mentee.power.crm.spring.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mentee.power.crm.entity.Company;
import ru.mentee.power.crm.entity.Lead;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.spring.dto.CreateLeadRequest;
import ru.mentee.power.crm.spring.dto.LeadResponse;
import ru.mentee.power.crm.spring.dto.UpdateLeadRequest;
import ru.mentee.power.crm.spring.exception.EntityNotFoundException;
import ru.mentee.power.crm.spring.mapper.LeadMapper;
import ru.mentee.power.crm.spring.repository.CompanyRepository;

/** Адаптер сервис для работы Rest контроллера с DTO*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeadRestServiceAdapter {

  private final JpaLeadService leadService;
  private final CompanyRepository companyRepository;
  private final LeadMapper leadMapper;

  /** Находит все лиды и преобразует в DTO */
  public List<LeadResponse> findAllLeads() {
    return leadService.findAll().stream().map(leadMapper::toResponse).toList();
  }

  /** Находит лида по ID  */
  public LeadResponse findLeadById(UUID id) {
    return leadService
        .findById(id)
        .map(leadMapper::toResponse)
        .orElseThrow(() -> new EntityNotFoundException(Lead.class, id));
  }

  /** Создает нового лида с компанией из БД и статусом NEW  */
  @Transactional
  public LeadResponse createLead(CreateLeadRequest request) {
    Company company =
        companyRepository
            .findById(request.getCompanyId())
            .orElseThrow(() -> new EntityNotFoundException(Company.class, request.getCompanyId()));

    Lead lead = leadMapper.toEntity(request);
    lead.setCompany(company);
    lead.setStatus(LeadStatus.NEW);

    return leadMapper.toResponse(leadService.createLead(lead));
  }

  /** Обновляет существующего лида */
  @Transactional
  public LeadResponse updateLead(UUID id, UpdateLeadRequest request) {
    Lead existingLead =
        leadService.findById(id).orElseThrow(() -> new EntityNotFoundException(Lead.class, id));

    leadMapper.updateEntity(request, existingLead);

    request
        .getCompanyId()
        .ifPresent(
            companyId -> {
              Company company =
                  companyRepository
                      .findById(companyId)
                      .orElseThrow(() -> new EntityNotFoundException(Company.class, companyId));
              existingLead.setCompany(company);
            });

    Lead updatedLead =
        leadService
            .updateLead(id, existingLead)
            .orElseThrow(() -> new EntityNotFoundException(Lead.class, id));
    return leadMapper.toResponse(updatedLead);
  }

  /** Удаляет лида по ID */
  @Transactional
  public void deleteLead(UUID id) {
    if (!leadService.deleteLead(id)) {
      throw new EntityNotFoundException(Lead.class, id);
    }
  }
}
