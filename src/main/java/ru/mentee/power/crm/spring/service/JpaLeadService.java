package ru.mentee.power.crm.spring.service;

import feign.FeignException;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.mentee.power.crm.domain.DealStatus;
import ru.mentee.power.crm.entity.Company;
import ru.mentee.power.crm.entity.Deal;
import ru.mentee.power.crm.entity.Lead;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.spring.client.EmailValidationFeignClient;
import ru.mentee.power.crm.spring.client.EmailValidationResponse;
import ru.mentee.power.crm.spring.dto.CreateDealRequest;
import ru.mentee.power.crm.spring.exception.EntityNotFoundException;
import ru.mentee.power.crm.spring.exception.IllegalLeadStateException;
import ru.mentee.power.crm.spring.repository.JpaDealRepository;
import ru.mentee.power.crm.spring.repository.JpaLeadRepository;

/** Сервисный слой бизнес логики */
@Service
@RequiredArgsConstructor
public class JpaLeadService {
  private static final Logger LOG = LoggerFactory.getLogger(JpaLeadService.class);

  private final JpaLeadRepository leadRepository;
  private final LeadProcessor leadProcessor;
  private final JpaDealRepository dealRepository;
  private final EmailValidationFeignClient emailValidationClient;

  /**
   * Создает нового лида с валидацией email через внешний сервис Если сервис валидации недоступен -
   * создает лида без валидации (graceful degradation)
   */
  @Retry(name = "email-validation", fallbackMethod = "createLeadFallback")
  public Lead createLead(Lead lead) {
    if (leadRepository.findByEmailNative(lead.getEmail()).isPresent()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Lead with email already exists: " + lead.getEmail());
    }

    try {
      EmailValidationResponse validation = emailValidationClient.validateEmail(lead.getEmail());

      if (!validation.valid()) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Invalid email: " + validation.reason());
      }

      return leadRepository.save(lead);

    } catch (FeignException.BadRequest _) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Email validation service rejected the email");
    }
  }

  /**
   * Fallback метод - вызывается, когда все попытки retry исчерпаны Создает лида без валидации email
   * (graceful degradation)
   */
  @SuppressWarnings("unused")
  public Lead createLeadFallback(Lead lead, @NonNull FeignException feignException) {
    LOG.warn(
        "Email validation service unavailable after retries. "
            + "Creating lead without validation. Error: {}",
        feignException.getMessage());

    return leadRepository.save(lead);
  }

  /** Находит всех лидов в БД */
  @Transactional(readOnly = true)
  public List<Lead> findAll() {
    return leadRepository.findAll();
  }

  /** Находит лида в БД по ID */
  @Transactional(readOnly = true)
  public Optional<Lead> findById(UUID id) {
    return leadRepository.findById(id);
  }

  /** Находит лида по статусу */
  @Transactional(readOnly = true)
  public List<Lead> findByStatus(LeadStatus status) {
    return leadRepository.findByStatusNative(status.name());
  }

  /** Обновляет лида */
  @Transactional
  public void update(UUID id, Lead updatedLead) {
    Lead existing =
        leadRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(Lead.class, id));

    existing.setName(updatedLead.getName());
    existing.setEmail(updatedLead.getEmail());
    existing.setPhone(updatedLead.getPhone());
    existing.setCompany(updatedLead.getCompany());
    existing.setStatus(updatedLead.getStatus());

    leadRepository.save(existing);
  }

  /** Удаление существующего лида. */
  @Transactional
  public void delete(UUID id) {
    if (!leadRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    leadRepository.deleteById(id);
  }

  /** Выполняет поиск и фильтрацию лидов по текстовому запросу и статусу. */
  @Transactional(readOnly = true)
  public List<Lead> findLeads(String search, LeadStatus status) {
    String statusStr = (status != null) ? status.name() : null;
    return leadRepository.findLeadsNative(search, statusStr);
  }

  /** Поиск лида по email (derived method). */
  public Optional<Lead> findByEmail(String email) {
    return leadRepository.findByEmail(email);
  }

  /** Поиск лидов по списку статусов (JPQL). */
  public List<Lead> findByStatuses(LeadStatus... statuses) {
    return leadRepository.findByStatusIn(List.of(statuses));
  }

  /** Получить первую страницу лидов с сортировкой. */
  public Page<Lead> getFirstPage(int pageSize) {
    PageRequest pageRequest =
        PageRequest.of(
            0, // первая страница (нумерация с 0)
            pageSize,
            Sort.by("createdAt").descending());
    return leadRepository.findAll(pageRequest);
  }

  /** Поиск по компании с пагинацией */
  public Page<Lead> searchByCompany(Company company, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    return leadRepository.findByCompany(company, pageable);
  }

  /**
   * Массовое обновление статуса (используется @Modifying метод). ВАЖНО: @Transactional обязательна
   * для @Modifying!
   */
  @Transactional
  public int convertNewToContacted() {
    int updated = leadRepository.updateStatusBulk(LeadStatus.NEW, LeadStatus.CONTACTED);
    // Логируем для observability
    System.out.printf("Converted %d leads from NEW to CONTACTED%n", updated);
    return updated;
  }

  /***/
  @Transactional
  public int archiveOldLeads(LeadStatus status) {
    return leadRepository.deleteByStatusBulk(status);
  }

  /**
   * Обрабатывает список лидов. Демонстрирует self-invocation problem: вызов
   * this.processSingleLead() обходит Spring proxy, поэтому @Transactional(propagation =
   * REQUIRES_NEW) игнорируется.
   */
  @Transactional
  public void processLeads(List<UUID> ids) {
    for (UUID id : ids) {
      leadProcessor.processSingleLead(id);
    }
  }

  /**
   * Конвертирует существующий лид в новую сделку. Обновляет статус лида на CONVERTED. Демонстрирует
   * транзакционность: если amount = null -> rollback.
   */
  @Transactional
  public void convertLeadToDeal(UUID leadId, CreateDealRequest request) {
    Lead lead =
        leadRepository
            .findById(leadId)
            .orElseThrow(() -> new EntityNotFoundException(Lead.class, leadId));

    if (lead.getStatus() != LeadStatus.QUALIFIED) {
      throw new IllegalLeadStateException(leadId, lead.getStatus().name());
    }

    Deal deal = new Deal();
    deal.setLeadId(leadId);
    deal.setAmount(request.getAmount());
    deal.setTitle(request.getTitle());
    deal.setStatus(DealStatus.NEW);
    dealRepository.save(deal);

    lead.setStatus(LeadStatus.CONVERTED);
    leadRepository.save(lead);
  }

  /** Поиск всех лидов по компании и обновление статуса */
  @Transactional
  public void changeStatus(Company company, LeadStatus status) {
    if (company == null || status == null) {
      throw new IllegalArgumentException("Company and LeadStatus must not be null");
    }
    leadRepository.updateStatuses(company, status);
  }

  /** Обновляет лида и возвращает Optional. Проверяем, что новый email не содержится в БД */
  @Transactional
  public Optional<Lead> updateLead(UUID id, Lead updatedLead) {
    return leadRepository
        .findById(id)
        .map(
            existing -> {
              if (!existing.getEmail().equals(updatedLead.getEmail())) {
                boolean emailExists =
                    leadRepository
                        .findByEmailNative(updatedLead.getEmail())
                        .map(lead -> !lead.getId().equals(id))
                        .orElse(false);

                if (emailExists) {
                  throw new ResponseStatusException(
                      HttpStatus.BAD_REQUEST,
                      "Email already in use by another lead: " + updatedLead.getEmail());
                }
              }
              existing.setName(updatedLead.getName());
              existing.setEmail(updatedLead.getEmail());
              existing.setPhone(updatedLead.getPhone());
              existing.setCompany(updatedLead.getCompany());
              existing.setStatus(updatedLead.getStatus());

              return leadRepository.save(existing);
            });
  }

  /** Удаляет лида, возвращает boolean */
  @Transactional
  public boolean deleteLead(UUID id) {
    if (leadRepository.existsById(id)) {
      leadRepository.deleteById(id);
      return true;
    }
    return false;
  }
}
