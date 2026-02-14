package ru.mentee.power.crm.spring.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.mentee.power.crm.entity.Deal;

/** Репозиторий для работы со сделками (Deal) */
public interface JpaDealRepository extends JpaRepository<Deal, UUID> {

  /**
   * Находит сделку по ID и загружает связанные позиции (DealProduct) вместе с информацией о
   * продуктах за один запрос (решение проблемы N+1).
   */
  @EntityGraph(attributePaths = {"dealProducts", "dealProducts.product"})
  @Query("SELECT d FROM Deal d WHERE d.id = :id")
  Optional<Deal> findDealWithProducts(@Param("id") UUID id);
}
