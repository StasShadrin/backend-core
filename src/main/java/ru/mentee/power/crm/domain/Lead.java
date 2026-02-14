package ru.mentee.power.crm.domain;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** Представляет потенциального клиента (лида) в CRM-системе. */
public class Lead {

  private final UUID id;
  private final Contact contact;
  private final String company;
  private final String status;

  private static final Set<String> ALLOWED_STATUSES = Set.of("NEW", "QUALIFIED", "CONVERTED");

  /** Создаёт нового Лида с заданными свойствами. */
  public Lead(UUID id, Contact contact, String company, String status) {
    if (id == null) {
      throw new IllegalArgumentException("ID must not be null");
    }
    if (contact == null) {
      throw new IllegalArgumentException("Contact must not be null");
    }
    if (status == null) {
      throw new IllegalArgumentException("Status must not be null");
    }
    if (!ALLOWED_STATUSES.contains(status)) {
      throw new IllegalArgumentException("Status must be one of: NEW, QUALIFIED, CONVERTED");
    }

    this.id = id;
    this.contact = contact;
    this.company = company;
    this.status = status;
  }

  public UUID getId() {
    return id;
  }

  public Contact getContact() {
    return contact;
  }

  public String getCompany() {
    return company;
  }

  public String getStatus() {
    return status;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    Lead lead = (Lead) obj;
    return Objects.equals(id, lead.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
