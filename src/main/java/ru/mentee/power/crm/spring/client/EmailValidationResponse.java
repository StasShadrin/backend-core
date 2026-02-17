package ru.mentee.power.crm.spring.client;

/** DTO ответа от внешнего API валидации email */
public record EmailValidationResponse(String email, boolean valid, String reason) {}
