package ru.mentee.power.crm.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Контракт на репозиторий сущности. */
public interface Repository<T> {
  /** Добавляет новую сущность в репозиторий. Дубликаты игнорируется. */
  void add(T entity);

  /** Удаляет сущность из репозитория по его уникальному идентификатору. */
  void remove(UUID id);

  /** Находит сущность по её уникальному идентификатору. */
  Optional<T> findById(UUID id);

  /** Возвращает все сущности в репозитории. */
  List<T> findAll();
}
