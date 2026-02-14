package ru.mentee.power.crm.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import ru.mentee.power.crm.domain.Lead;

/** Сущности лида с использованием HashSet для автоматической дедупликации. */
public class LeadRepository {
  private final Set<Lead> leads = new HashSet<>();

  /** Добавляет лид в репозиторий, если он ещё не присутствует. */
  public boolean add(Lead lead) {
    if (lead == null) {
      throw new IllegalArgumentException("Lead must not be null");
    }
    return leads.add(lead);
  }

  /** Проверяет, содержит ли репозиторий указанный лид. */
  public boolean contains(Lead lead) {
    return leads.contains(lead);
  }

  /** Возвращает неизменяемый вид всех лидов в репозитории. */
  public Set<Lead> findAll() {
    return Collections.unmodifiableSet(leads);
  }

  /** Возвращает количество лидов в репозитории. */
  public int size() {
    return leads.size();
  }
}
