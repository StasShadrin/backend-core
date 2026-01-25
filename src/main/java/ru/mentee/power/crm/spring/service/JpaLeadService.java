package ru.mentee.power.crm.spring.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import ru.mentee.power.crm.entity.Lead;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.spring.repository.JpaLeadRepository;

/** Сервисный слой бизнес логики */
@Service
@RequiredArgsConstructor
public class JpaLeadService {

    private final JpaLeadRepository repository;

    /** Сохраняет лида в БД*/
    @Transactional
    public Lead addLead(Lead lead) {
        // Проверка уникальности email
        if (repository.findByEmailNative(lead.getEmail()).isPresent()) {
            throw new IllegalStateException("Lead with email already exists: " + lead.getEmail());
        }
        return repository.save(lead);
    }

    /** Находит всех лидов в БД */
    @Transactional(readOnly = true)
    public List<Lead> findAll() {
        return repository.findAll();
    }

    /** Находит лида в БД по ID*/
    @Transactional(readOnly = true)
    public Optional<Lead> findById(UUID id) {
        return repository.findById(id);
    }

    /** Находит лида в БД по email */
    @Transactional(readOnly = true)
    public Optional<Lead> findByEmail(String email) {
        return repository.findByEmailNative(email);
    }

    /** Находит лида по статусу */
    @Transactional(readOnly = true)
    public List<Lead> findByStatus(LeadStatus status) {
        return repository.findByStatusNative(status.name());
    }

    /** Обновляет лида */
    @Transactional
    public void update(UUID id, Lead updatedLead) {
        Lead existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + id));

        existing.setName(updatedLead.getName());
        existing.setEmail(updatedLead.getEmail());
        existing.setPhone(updatedLead.getPhone());
        existing.setCompany(updatedLead.getCompany());
        existing.setStatus(updatedLead.getStatus());

        repository.save(existing);
    }

    /** Удаление существующего лида. */
    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        repository.deleteById(id);
    }

    /** Выполняет поиск и фильтрацию лидов по текстовому запросу и статусу. */
    @Transactional(readOnly = true)
    public List<Lead> findLeads(String search, LeadStatus status) {
        String statusStr = (status != null) ? status.name() : null;
        return repository.findLeadsNative(search, statusStr);
    }
}