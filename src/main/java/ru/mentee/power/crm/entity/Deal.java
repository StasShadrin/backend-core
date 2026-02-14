package ru.mentee.power.crm.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.mentee.power.crm.domain.DealStatus;

/**
 * Сущность сделки (Deal) для работы с CRM-системой. Представляет коммерческую сделку, связанную с
 * лидом и содержащую продукты.
 */
@Entity
@Table(name = "deals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deal {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Version
  @Column(name = "version", nullable = false)
  @Setter(AccessLevel.NONE)
  private Long version;

  @Column(name = "lead_id", nullable = false)
  private UUID leadId;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DealStatus status;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  /**
   * Список позиций в сделке (связь один-ко-многим через junction-таблицу). Каждая позиция содержит
   * ссылку на продукт, количество и цену на момент сделки.
   */
  @OneToMany(mappedBy = "deal", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<DealProduct> dealProducts = new ArrayList<>();

  @PrePersist
  protected void onCreate() {
    if (this.createdAt == null) {
      this.createdAt = OffsetDateTime.now(java.time.ZoneOffset.UTC);
    }
  }

  /** Добавляет позицию в сделку и устанавливает обратную связь. */
  public void addDealProduct(DealProduct dealProduct) {
    dealProducts.add(dealProduct);
    dealProduct.setDeal(this);
  }

  /** Удаляет позицию из сделки и разрывает обратную связь. */
  public void removeDealProduct(DealProduct dealProduct) {
    dealProducts.remove(dealProduct);
    dealProduct.setDeal(null);
  }
}
