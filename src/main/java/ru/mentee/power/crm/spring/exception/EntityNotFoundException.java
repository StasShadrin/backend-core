package ru.mentee.power.crm.spring.exception;

import java.util.UUID;
import lombok.Getter;

/** Универсальное исключение для Entity. */
@Getter
public class EntityNotFoundException extends RuntimeException {
  private final UUID uuid;
  private final Object entity;

  public EntityNotFoundException(Class<?> entityClass, UUID uuid) {
    super(entityClass.getSimpleName() + " not found: " + uuid);
    this.entity = entityClass;
    this.uuid = uuid;
  }
}
