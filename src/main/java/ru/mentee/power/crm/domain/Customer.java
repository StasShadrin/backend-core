package ru.mentee.power.crm.domain;

import java.util.Set;
import java.util.UUID;

/** Представляет клиента в CRM-системе. */
public record Customer(UUID id, Contact contact, Address billingAddress, String loyaltyTier) {

  private static final Set<String> LOYALTY_LEVELS = Set.of("BRONZE", "SILVER", "GOLD");

  /** Компактный конструктор */
  public Customer {
    if (id == null) {
      throw new IllegalArgumentException("ID must not be null");
    }
    if (contact == null) {
      throw new IllegalArgumentException("Contact must not be null");
    }
    if (billingAddress == null) {
      throw new IllegalArgumentException("Billing Address must not be null");
    }
    if (loyaltyTier == null) {
      throw new IllegalArgumentException("Loyalty Tier must not be null");
    }
    if (!LOYALTY_LEVELS.contains(loyaltyTier)) {
      throw new IllegalArgumentException("Loyalty Tier must be one of: BRONZE, SILVER, GOLD");
    }
  }
}
