package ru.mentee.power.crm.spring.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import jakarta.persistence.EntityManager;
import ru.mentee.power.crm.entity.Deal;
import ru.mentee.power.crm.entity.DealProduct;
import ru.mentee.power.crm.entity.Lead;
import ru.mentee.power.crm.entity.Product;
import ru.mentee.power.crm.model.LeadStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DealProductIntegrationTest {

    @Autowired
    private JpaDealRepository dealRepository;

    @Autowired
    private JpaLeadRepository leadRepository;

    @Autowired
    private ProductJpaRepository productRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void testSaveDealWithProducts() {
        Lead lead = Lead.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .phone("+79991234567")
                .status(LeadStatus.NEW)
                .build();
        lead = leadRepository.save(lead);

        Product product1 = Product.builder()
                .name("Ноутбук Dell")
                .sku("LAPTOP-001")
                .price(BigDecimal.valueOf(90000))
                .active(true)
                .build();

        Product product2 = Product.builder()
                .name("Монитор LG")
                .sku("MONITOR-001")
                .price(BigDecimal.valueOf(25000))
                .active(true)
                .build();

        productRepository.save(product1);
        productRepository.save(product2);

        Deal deal = Deal.builder()
                .amount(BigDecimal.valueOf(187000))
                .title("Заказ клиента")
                .status(ru.mentee.power.crm.domain.DealStatus.NEW)
                .leadId(lead.getId())
                .build();

        DealProduct dealProduct1 = DealProduct.builder()
                .product(product1)
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(81000))
                .build();

        DealProduct dealProduct2 = DealProduct.builder()
                .product(product2)
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(25000))
                .build();

        deal.addDealProduct(dealProduct1);
        deal.addDealProduct(dealProduct2);
        Deal savedDeal = dealRepository.save(deal);

        Optional<Deal> found = dealRepository.findById(savedDeal.getId());
        assertThat(found).isPresent();

        Deal loadedDeal = found.get();
        List<DealProduct> products = loadedDeal.getDealProducts();
        assertThat(products).hasSize(2);

        DealProduct dp1 = products.stream()
                .filter(dp -> dp.getProduct().getSku().equals("LAPTOP-001"))
                .findFirst()
                .orElseThrow();
        assertThat(dp1.getQuantity()).isEqualTo(2);
        assertThat(dp1.getUnitPrice()).isEqualTo(BigDecimal.valueOf(81000));

        DealProduct dp2 = products.stream()
                .filter(dp -> dp.getProduct().getSku().equals("MONITOR-001"))
                .findFirst()
                .orElseThrow();
        assertThat(dp2.getQuantity()).isEqualTo(1);
        assertThat(dp2.getUnitPrice()).isEqualTo(BigDecimal.valueOf(25000));
    }

    @Test
    void testEntityGraphSolvesNPlusOne() {
        Lead lead = leadRepository.save(Lead.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .phone("+79991234567")
                .status(LeadStatus.NEW)
                .build());

        Product p1 = productRepository.save(Product.builder()
                .name("Клавиатура")
                .sku("KEYBOARD-001")
                .price(BigDecimal.valueOf(3000))
                .active(true)
                .build());

        Product p2 = productRepository.save(Product.builder()
                .name("Мышь")
                .sku("MOUSE-001")
                .price(BigDecimal.valueOf(2000))
                .active(true)
                .build());

        Product p3 = productRepository.save(Product.builder()
                .name("Подставка")
                .sku("STAND-001")
                .price(BigDecimal.valueOf(1500))
                .active(true)
                .build());

        Deal deal = Deal.builder()
                .title("Аксессуары")
                .amount(BigDecimal.valueOf(6500))
                .status(ru.mentee.power.crm.domain.DealStatus.NEW)
                .leadId(lead.getId())
                .build();

        deal.addDealProduct(DealProduct.builder()
                .product(p1)
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(3000))
                .build());

        deal.addDealProduct(DealProduct.builder()
                .product(p2)
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(2000))
                .build());

        deal.addDealProduct(DealProduct.builder()
                .product(p3)
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(1500))
                .build());

        Deal savedDeal = dealRepository.save(deal);
        entityManager.flush();

        //Тест без @EntityGraph (проблема N+1)
        System.out.println("БЕЗ @EntityGraph (N+1 проблема)");
        entityManager.clear();
        Deal dealWithoutGraph = dealRepository.findById(savedDeal.getId()).orElseThrow();

        List<DealProduct> withoutGraph = dealWithoutGraph.getDealProducts();
        System.out.println("Загружено " + withoutGraph.size() + " позиций в сделке");


        for (DealProduct dp : withoutGraph) {
            System.out.println("  - " + dp.getProduct().getName() + " (SKU: " + dp.getProduct().getSku() + ")");
        }

        //Тест с @EntityGraph (решение N+1)
        System.out.println("С @EntityGraph (1 запрос)");
        entityManager.clear();
        Deal dealWithGraph = dealRepository.findDealWithProducts(savedDeal.getId()).orElseThrow();

        List<DealProduct> withGraph = dealWithGraph.getDealProducts();
        System.out.println("Загружено " + withGraph.size() + " позиций в сделке");

        for (DealProduct dp : withGraph) {
            System.out.println("  - " + dp.getProduct().getName() + " (SKU: " + dp.getProduct().getSku() + ")");
        }

        assertThat(withGraph).hasSize(3);
        assertThat(withoutGraph).hasSize(3);
        assertThat(withGraph.stream().map(dp -> dp.getProduct().getSku()))
                .containsExactlyInAnyOrder("KEYBOARD-001", "MOUSE-001", "STAND-001");
        assertThat(withoutGraph.stream().map(dp -> dp.getProduct().getSku()))
                .containsExactlyInAnyOrder("KEYBOARD-001", "MOUSE-001", "STAND-001");
    }
}