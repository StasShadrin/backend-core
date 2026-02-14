package ru.mentee.power.crm.domain;

/** Обозначает почтовый адрес с указанием города, улицы и почтового индекса. */
public record Address(String city, String street, String zip) {

  /** Компактный конструктор. */
  public Address {
    if (city == null || city.isBlank()) {
      throw new IllegalArgumentException("City must not be null or empty");
    }
    if (zip == null || zip.isBlank()) {
      throw new IllegalArgumentException("Zip must not be null or empty");
    }
  }
}
