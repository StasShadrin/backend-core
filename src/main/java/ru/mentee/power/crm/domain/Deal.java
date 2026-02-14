package ru.mentee.power.crm.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Сделка (Deal) - основная сущность воронки продаж. Является Aggregate Root: сам управляет своим
 * состоянием и защищает бизнес-правила.
 */
@AllArgsConstructor
@Getter
@ToString
public class Deal {
  private final UUID id;
  private final UUID leadId;
  private final BigDecimal amount;
  private DealStatus status;
  private final LocalDateTime createdAt;

  /** Создаёт новую сделку из существующего лида. Статус автоматически устанавливается в NEW. */
  public Deal(UUID leadId, BigDecimal amount) {
    this.id = UUID.randomUUID();
    this.leadId = Objects.requireNonNull(leadId, "leadId must not be null");
    this.amount = Objects.requireNonNull(amount, "amount must not be null");
    this.status = DealStatus.NEW;
    this.createdAt = LocalDateTime.now();
  }

  /**
   * Безопасно изменяет статус сделки, проверяя валидность перехода. Выбрасывает исключение, если
   * переход запрещён State Machine.
   */
  public void transitionTo(DealStatus newStatus) {
    Objects.requireNonNull(newStatus, "newStatus must not be null");
    if (!this.status.canTransitionTo(newStatus)) {
      throw new IllegalStateException("Cannot transition from " + this.status + " to " + newStatus);
    }
    this.status = newStatus;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    Deal deal = (Deal) obj;
    return Objects.equals(id, deal.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
