package ru.mentee.power.crm.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DealStatusTest {

  @ParameterizedTest
  @CsvSource({
    "NEW, QUALIFIED, true",
    "NEW, LOST, true",
    "NEW, WON, false",
    "QUALIFIED, PROPOSAL_SENT, true",
    "PROPOSAL_SENT, NEGOTIATION, true",
    "NEGOTIATION, WON, true",
    "NEGOTIATION, LOST, true",
    "WON, NEW, false",
    "LOST, QUALIFIED, false"
  })
  void shouldValidateTransitions(DealStatus from, DealStatus to, boolean expected) {

    assertThat(from.canTransitionTo(to)).isEqualTo(expected);
  }

  @Test
  void terminalStates_shouldNotAllowAnyTransitions() {
    // Проверяем, что из WON нельзя перейти ни в какой статус
    for (DealStatus target : DealStatus.values()) {
      assertThat(DealStatus.WON.canTransitionTo(target))
          .describedAs("WON should not allow transition to %s", target)
          .isFalse();
    }

    // Проверяем, что из LOST нельзя перейти ни в какой статус
    for (DealStatus target : DealStatus.values()) {
      assertThat(DealStatus.LOST.canTransitionTo(target))
          .describedAs("LOST should not allow transition to %s", target)
          .isFalse();
    }
  }
}
