package ru.mentee.power.crm.spring.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ru.mentee.power.crm.model.Lead;

/** Контракт на операции по сохранению лидов с использованием CRUD и поиска по email адресу. */
public interface LeadRepository {
  /** Сохраняет лида и возвращает сохраненный экземпляр. */
  Lead save(Lead lead);

  /** Находит лида по-уникальному ID. */
  Optional<Lead> findById(UUID id);

  /** Находит лида по email адресу. */
  Optional<Lead> findByEmail(String email);

  /** Возвращает все записи лидов. */
  List<Lead> findAll();

  /** Удаляет лида по его ID. */
  void deleteById(UUID id);

  /** Возвращает общее количество сохранённых лидов. */
  int size();
}
