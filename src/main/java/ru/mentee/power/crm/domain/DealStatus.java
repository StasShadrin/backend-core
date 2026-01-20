package ru.mentee.power.crm.domain;

import java.util.Map;
import java.util.Set;

/**
 * Статусы сделки в воронке продаж.
 * Реализует паттерн State Machine: определяет допустимые переходы между состояниями.
 */
public enum DealStatus {
    NEW,
    QUALIFIED,
    PROPOSAL_SENT,
    NEGOTIATION,
    WON,
    LOST;

    private static final Map<DealStatus, Set<DealStatus>> VALID_TRANSITIONS = Map.of(
            NEW, Set.of(QUALIFIED, LOST),
            QUALIFIED, Set.of(PROPOSAL_SENT, LOST),
            PROPOSAL_SENT, Set.of(NEGOTIATION, LOST),
            NEGOTIATION, Set.of(WON, LOST),
            WON, Set.of(),
            LOST, Set.of()
    );

    /**
     * Проверяет, можно ли перейти из текущего статуса в целевой.
     * Терминальные состояния (WON, LOST) не допускают никаких переходов.
     *
     * @param target целевой статус
     * @return true, если переход разрешён; false - если запрещён или target == null
     */
    public boolean canTransitionTo(DealStatus target) {
        if (target == null) {
            return false;
        }
        return VALID_TRANSITIONS.get(this).contains(target);
    }
}