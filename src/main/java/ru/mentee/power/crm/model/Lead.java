package ru.mentee.power.crm.model;

import java.util.UUID;

/**
 * Immutable value object representing a lead in the CRM system.
 */
public record Lead(
        UUID id,
        String email,
        String phone,
        String company,
        LeadStatus status) {
}