/**
 * Defines valid event outcomes and associated parameters
 * @author Gage Garcia
 */
package oogasalad.engine.event;

import java.util.Map;

public class EventOutcome {
    /**
     * Defines valid outcome types
     */
    public enum OutcomeType {
        MOVE_RIGHT,
        JUMP,
        APPLY_GRAVITY,
        LOSE_GAME,
        PATROL
    }

    private OutcomeType outcomeType;

    /**
     * Constructor sets outcome type
     * @param outcomeType
     */
    public EventOutcome(OutcomeType outcomeType) {
        this.outcomeType = outcomeType;
    }

    /**
     * @returns outcome type enum
     */
    public OutcomeType getOutcomeType() {
        return outcomeType;
    }
}
