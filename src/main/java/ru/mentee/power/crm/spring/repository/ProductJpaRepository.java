package ru.mentee.power.crm.spring.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mentee.power.crm.entity.Product;

/** Репозиторий для работы с товарами (Product). */
@Repository
public interface ProductJpaRepository extends JpaRepository<Product, UUID> {

  /** Находит продукт по уникальному артикулу (SKU). */
  Optional<Product> findBySku(String sku);

  /** Возвращает список всех активных продуктов. */
  List<Product> findByActiveTrue();
}
