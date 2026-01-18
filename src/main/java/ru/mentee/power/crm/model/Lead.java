package ru.mentee.power.crm.model;

import java.util.UUID;

import io.soabase.recordbuilder.core.RecordBuilder;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Неизменяемый объект-лид в CRM-системе.
 * Содержит контактные данные, компанию и статус.
 * Все обязательные поля защищены аннотациями Bean Validation.
 */
@RecordBuilder
public record Lead(
        UUID id,

        @NotBlank(message = "{lead.name.notblank}")
        @Size(min = 2, max = 100, message = "{lead.name.size}")
        String name,

        @NotBlank(message = "{lead.email.notblank}")
        @Email(message = "{lead.email.email}")
        @Size(max = 100, message = "{lead.email.size}")
        String email,

        @NotBlank(message = "{lead.phone.notblank}")
        @Size(max = 20, message = "{lead.phone.size}")
        String phone,

        @Size(min = 2, max = 100, message = "{lead.company.size}")
        String company,

        @NotNull(message = "{lead.status.notnull}")
        LeadStatus status) {}