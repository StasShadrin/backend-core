package ru.mentee.power.crm.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/***/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "companies")
public class Company {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private String name;

  private String industry;

  @OneToMany(mappedBy = "company", cascade = CascadeType.PERSIST)
  @Builder.Default
  private List<Lead> leads = new ArrayList<>();

  /** Добавляет лид к компании и устанавливает обратную связь. */
  public void addLead(Lead lead) {
    leads.add(lead);
    lead.setCompany(this);
  }

  /** Удаляет лид из компании и разрывает обратную связь. */
  public void removeLead(Lead lead) {
    leads.remove(lead);
    lead.setCompany(null);
  }
}
