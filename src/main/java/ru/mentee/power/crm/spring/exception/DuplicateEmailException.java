package ru.mentee.power.crm.spring.exception;

public class DuplicateEmailException extends BusinessException {

  public DuplicateEmailException(String email) {
    super(String.format("Lead with email already exists: %s", email));
  }
}
