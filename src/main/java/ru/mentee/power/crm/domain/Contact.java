package ru.mentee.power.crm.domain;

/** Представляет контактную информацию человека. */
public record Contact(String email, String phone, Address address) {

  /** Компактный конструктор. */
  public Contact {
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("Email must not be null or empty");
    }
    if (phone == null || phone.isBlank()) {
      throw new IllegalArgumentException("Phone must not be null or empty");
    }
    if (address == null) {
      throw new IllegalArgumentException("Address must not be null");
    }
  }
}
