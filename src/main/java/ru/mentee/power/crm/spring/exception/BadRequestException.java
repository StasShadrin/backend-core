package ru.mentee.power.crm.spring.exception;

public class BadRequestException extends BusinessException {
  public BadRequestException(String message) {
    super(message);
  }
}
