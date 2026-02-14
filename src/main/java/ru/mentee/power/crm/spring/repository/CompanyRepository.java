package ru.mentee.power.crm.spring.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.mentee.power.crm.entity.Company;

/** Репозиторий для работы с компаниями. */
public interface CompanyRepository extends JpaRepository<Company, UUID> {

  /** Находит компанию по ID и загружает связанные лиды за один запрос (решение N+1). */
  @EntityGraph(attributePaths = {"leads"})
  @Query("SELECT c FROM Company c WHERE c.id = :id")
  Optional<Company> findByIdWithLeads(@Param("id") UUID id);

  /** Находит компанию по точному названию. */
  Optional<Company> findByName(String name);
}
