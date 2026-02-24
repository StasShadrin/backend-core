package ru.mentee.power.crm.spring.exception;

public abstract class BusinessException extends RuntimeException {
  BusinessException(String message) {
    super(message);
  }
}
