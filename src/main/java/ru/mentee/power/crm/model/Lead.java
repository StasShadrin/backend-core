package ru.mentee.power.crm.model;

/**
 * Immutable value object representing a lead in the CRM system.
 */
public record Lead(
        String id,
        String email,
        String phone,
        String company,
        String status) {
}