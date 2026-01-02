package ru.mentee.power.crm.model;

/**
 * Represents the current stage of a lead in the sales pipeline.
 */
public enum LeadStatus {
    NEW,          // Lead just registered
    CONTACTED,    // First contact established
    QUALIFIED     // Lead meets criteria for sales follow-up
}
