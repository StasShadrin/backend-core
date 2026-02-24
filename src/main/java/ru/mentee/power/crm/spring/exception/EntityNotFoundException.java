package ru.mentee.power.crm.spring.exception;

import lombok.Getter;

/** Универсальное исключение для Entity. */
@Getter
public class EntityNotFoundException extends BusinessException {

  private final String entityType;
  private final String entityId;

  public EntityNotFoundException(String entityType, String entityId) {
    super(String.format("%s not found with id: %s", entityType, entityId));
    this.entityType = entityType;
    this.entityId = entityId;
  }

  public EntityNotFoundException(String message) {
    super(message);
    this.entityType = null;
    this.entityId = null;
  }
}
