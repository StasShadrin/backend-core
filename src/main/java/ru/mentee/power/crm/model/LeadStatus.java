package ru.mentee.power.crm.model;

/**
 * Представляет текущий этап лида в конвейере продаж.
 */
public enum LeadStatus {
    NEW,          // Лид только что зарегистрировался
    CONTACTED,    // Установлен первый контакт
    QUALIFIED,    // Лид соответствует критериям последующих продаж
    LOST          //
}
