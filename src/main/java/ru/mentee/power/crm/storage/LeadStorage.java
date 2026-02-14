package ru.mentee.power.crm.storage;

import java.util.Objects;
import java.util.UUID;
import ru.mentee.power.crm.domain.Lead;

/** Хранилище лидов (потенциальных клиентов) на основе массива фиксированного размера. */
public class LeadStorage {

  /** Максимальное количество проводов, которые можно хранить. */
  private static final int MAX_SIZE = 100;

  /** Внутренний массив для хранения лидов. */
  private final Lead[] leads = new Lead[MAX_SIZE];

  /** Добавляет новый кабель в хранилище. */
  public boolean add(Lead lead) {
    if (lead == null) {
      throw new NullPointerException("Lead must not be null");
    }

    int firstNullIndex = -1;

    for (int i = 0; i < leads.length; i++) {
      if (leads[i] == null) {
        if (firstNullIndex == -1) {
          firstNullIndex = i;
        }
      } else {
        if (Objects.equals(leads[i].getContact().email(), lead.getContact().email())) {
          return false;
        }
      }
    }

    if (firstNullIndex != -1) {
      leads[firstNullIndex] = lead;
      return true;
    } else {
      throw new IllegalStateException("Storage is full, cannot add more leads");
    }
  }

  /** Возвращает копию всех сохранённых лидов. */
  public Lead[] findAll() {
    Lead[] result = new Lead[size()];
    int resultIndex = 0;
    for (Lead lead : leads) {
      if (lead != null) {
        result[resultIndex++] = lead;
      }
    }
    return result;
  }

  /** Возвращает текущее количество проводов в хранилище. */
  public int size() {
    int count = 0;
    for (Lead lead : leads) {
      if (lead != null) {
        count++;
      }
    }
    return count;
  }

  /** Находит и возвращает зацепку по уникальному идентификатору. */
  public Lead findById(UUID id) {
    if (id == null) {
      throw new NullPointerException("Lead ID must not be null");
    }

    for (Lead lead : leads) {
      if (lead != null && Objects.equals(lead.getId(), id)) {
        return lead;
      }
    }
    return null;
  }
}
