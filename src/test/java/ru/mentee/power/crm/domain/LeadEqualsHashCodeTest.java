package ru.mentee.power.crm.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import ru.mentee.power.crm.storage.LeadStorage;

class LeadEqualsHashCodeTest {
    private final UUID randomUUID = UUID.randomUUID();
    private final Lead base = new Lead(randomUUID,
            "ivan@mail.ru",
            "+7123",
            "TechCorp",
            "NEW");

    @Test
    void shouldBeReflexiveWhenEqualsCalledOnSameObject() {
        // Given
        Lead lead = base;

        // Then: Объект равен сам себе (isEqualTo использует equals() внутри)
        assertThat(lead).isEqualTo(lead);
    }

    @Test
    void shouldBeSymmetricWhenEqualsCalledOnTwoObjects() {
        // Given
        Lead firstLead = base;
        Lead secondLead = base;

        // Then: Симметричность — порядок сравнения не важен
        assertThat(firstLead).isEqualTo(secondLead);
        assertThat(secondLead).isEqualTo(firstLead);
    }

    @Test
    void shouldBeTransitiveWhenEqualsChainOfThreeObjects() {
        // Given
        Lead firstLead = base;
        Lead secondLead = base;
        Lead thirdLead = base;

        // Then: Транзитивность — если A=B и B=C, то A=C
        assertThat(firstLead).isEqualTo(secondLead);
        assertThat(secondLead).isEqualTo(thirdLead);
        assertThat(firstLead).isEqualTo(thirdLead);
    }

    @Test
    void shouldBeConsistentWhenEqualsCalledMultipleTimes() {
        // Given
        Lead firstLead = base;
        Lead secondLead = base;

        // Then: Результат одинаковый при многократных вызовах
        assertThat(firstLead).isEqualTo(secondLead);
        assertThat(firstLead).isEqualTo(secondLead);
        assertThat(firstLead).isEqualTo(secondLead);
    }

    @Test
    void shouldReturnFalseWhenEqualsComparedWithNull() {
        // Then: Объект не равен null (isNotEqualTo проверяет equals(null) = false)
        assertThat(base).isNotEqualTo(null);
    }

    @Test
    void shouldHaveSameHashCodeWhenObjectsAreEqual() {
        // Given
        Lead firstLead = base;
        Lead secondLead = base;

        // Then: Если объекты равны, то hashCode должен быть одинаковым
        assertThat(firstLead).isEqualTo(secondLead);
        assertThat(firstLead.hashCode()).hasSameHashCodeAs(secondLead.hashCode());
    }

    @Test
    void shouldWorkInHashMapWhenLeadUsedAsKey() {
        // Given

        Map<Lead, String> map = new HashMap<>();
        map.put(base, "CONTACTED");

        // When: Получаем значение по другому объекту с тем же id
        String status = map.get(base);

        // Then: HashMap нашел значение благодаря equals/hashCode
        assertThat(status).isEqualTo("CONTACTED");
    }

    @Test
    void shouldNotBeEqualWhenIdsAreDifferent() {
        // Given
        Lead differentLead = new Lead(UUID.randomUUID(),
                "ivan@mail.ru",
                "+7123",
                "TechCorp",
                "NEW");

        // Then: Разные id = разные объекты (isNotEqualTo использует equals() внутри)
        assertThat(base).isNotEqualTo(differentLead);
    }


    @Test
    void shouldPreventStringConfusionWhenUsingUUID() {
        // Given
        LeadStorage storage = new LeadStorage();
        storage.add(base);

        // When — вызов с правильным типом UUID
        Lead found = storage.findById(base.getId());

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(base.getId());
    }
}