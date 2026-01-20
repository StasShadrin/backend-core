package ru.mentee.power.crm.spring.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ru.mentee.power.crm.domain.Deal;
import ru.mentee.power.crm.domain.DealStatus;

/** Репозиторий для работы со сделками (Deal). */
public interface DealRepository {
    /** Сохраняет сделку в хранилище. */
    void save(Deal deal);

    /** Находит сделку по ID. */
    Optional<Deal> findById(UUID id);

    /** Возвращает все существующие сделки. */
    List<Deal> findAll();

    /** Возвращает все сделки с заданным статусом. */
    List<Deal> findByStatus(DealStatus status);

    /** Удаляет сделку по ID.*/
    void deleteById(UUID id);
}