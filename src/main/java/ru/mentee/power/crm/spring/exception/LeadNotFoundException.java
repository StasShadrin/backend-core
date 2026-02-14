package ru.mentee.power.crm.spring.exception;

import java.util.UUID;
import lombok.Getter;

/** Исключение, выбрасываемое когда Lead не найден. */
@Getter
public class LeadNotFoundException extends RuntimeException {
  private final UUID leadId;

  /** Lead не найден по ID */
  public LeadNotFoundException(UUID leadId) {
    super("Lead not found: " + leadId);
    this.leadId = leadId;
  }
}
