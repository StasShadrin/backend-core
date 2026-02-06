package ru.mentee.power.crm.spring.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import ru.mentee.power.crm.entity.Product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class ProductJpaRepositoryTest {

    @Autowired
    private ProductJpaRepository productRepository;

    @Test
    void shouldSaveAndFindProduct_whenValidData() {
        // Given
        Product product = new Product();
        product.setName("Консультация по архитектуре");
        product.setSku("CONSULT-ARCH-001");
        product.setPrice(new BigDecimal("50000.00"));
        product.setActive(true);

        // When
        Product saved = productRepository.save(product);

        // Then
        assertThat(saved.getId()).isNotNull();
        Optional<Product> found = productRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getSku()).isEqualTo("CONSULT-ARCH-001");
    }

    @Test
    void shouldFindBySku_whenProductExists() {
        // Given
        Product product = new Product();
        product.setName("Ноутбук Dell XPS 15");
        product.setSku("DELL-XPS-15-2026");
        product.setPrice(new BigDecimal("199990.00"));
        product.setActive(true);
        productRepository.save(product);

        // When
        Optional<Product> found = productRepository.findBySku("DELL-XPS-15-2026");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Ноутбук Dell XPS 15");
    }

    @Test
    void shouldFindByActiveTrue_whenProductsExist() {
        // Given
        Product laptop = new Product();
        laptop.setName("Монитор LG 27 UltraFine");
        laptop.setSku("LG-27-ULTRAFINE");
        laptop.setPrice(new BigDecimal("45990.00"));
        laptop.setActive(true);
        productRepository.save(laptop);

        Product service = new Product();
        service.setName("Техническая поддержка (год)");
        service.setSku("SUPPORT-YEAR-PREMIUM");
        service.setPrice(new BigDecimal("120000.00"));
        service.setActive(true);
        productRepository.save(service);

        Product discontinued = new Product();
        discontinued.setName("Клавиатура Logitech K120");
        discontinued.setSku("LOGI-K120-DISC");
        discontinued.setPrice(new BigDecimal("890.00"));
        discontinued.setActive(false);
        productRepository.save(discontinued);

        // When
        List<Product> activeProducts = productRepository.findByActiveTrue();

        // Then
        assertThat(activeProducts).hasSize(2);
        assertThat(activeProducts)
                .extracting(Product::getSku)
                .containsExactlyInAnyOrder("LG-27-ULTRAFINE", "SUPPORT-YEAR-PREMIUM");
    }

    @Test
    void shouldEnforceUniqueSkuConstraint() {
        // Given
        Product original = new Product();
        original.setName("Сервер HP DL380");
        original.setSku("HP-PROLIANT-DL380");
        original.setPrice(new BigDecimal("850000.00"));
        original.setActive(true);
        productRepository.save(original);

        Product duplicate = new Product();
        duplicate.setName("Сервер HP DL380 (дубль)");
        duplicate.setSku("HP-PROLIANT-DL380");
        duplicate.setPrice(new BigDecimal("900000.00"));
        duplicate.setActive(true);

        // When & Then
        assertThatThrownBy(() -> {
            productRepository.save(duplicate);
            productRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}