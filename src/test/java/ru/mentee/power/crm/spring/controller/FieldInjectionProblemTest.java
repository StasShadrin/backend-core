package ru.mentee.power.crm.spring.controller;

import org.junit.jupiter.api.Test;

class FieldInjectionProblemTest {

    @Test
    void fieldInjectionCausesNullPointerWithoutSpring() {
        // Демонстрация проблемы: Field Injection не работает без Spring
        // DemoController имеет @Autowired поле fieldRepository
        // Если создать через new — поле останется null

        DemoController controller = new DemoController(null);
        // fieldRepository осталось null — Spring не внедрил (нет контейнера)

        // Этот тест показывает что Field Injection untestable без @SpringBootTest
        // Uncomment следующую строку чтобы увидеть NPE:
        // controller.demo(); // NullPointerException при обращении к fieldRepository

        // Constructor Injection позволил бы передать mock через конструктор
        // Field Injection требует Spring контейнер или Reflection hacks
    }
}