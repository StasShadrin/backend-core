package ru.mentee.power.crm.spring.exception;

import java.util.UUID;
import lombok.Getter;

/** Custom RuntimeException */
@Getter
public class IllegalLeadStateException extends RuntimeException {
  private final UUID leadId;
  private final String currentStatus;

  /** Лид не может быть конвертирован */
  public IllegalLeadStateException(UUID leadId, String currentStatus) {
    super("Lead " + leadId + " cannot be converted. Current status: " + currentStatus);
    this.leadId = leadId;
    this.currentStatus = currentStatus;
  }
}
