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
        JUMP,
        DESTROY_OBJECT
    }

    private OutcomeType outcomeType;

    /**
     * @returns outcome type enum
     */
    public OutcomeType getOutcomeType() {
        return outcomeType;
    }
}
