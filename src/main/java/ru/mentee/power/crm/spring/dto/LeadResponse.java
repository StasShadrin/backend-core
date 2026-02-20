package ru.mentee.power.crm.spring.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import ru.mentee.power.crm.model.LeadStatus;

/** DTO формы ответа */
public record LeadResponse(
    UUID id,
    String name,
    String email,
    String phone,
    UUID companyId,
    LeadStatus status,
    OffsetDateTime createdAt) {}
