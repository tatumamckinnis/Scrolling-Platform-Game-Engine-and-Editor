/**
 * Defines valid event outcomes and associated parameters
 * @author Gage Garcia
 */
package oogasalad.engine.model.event;

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

    //defines expected parameters for each outcome type
    private Map<OutcomeType, Map<String, String>> outcomeParamMap;

    /**
     * @return the associated parameter mapping for a specific outcome type
     */
    public Map<String, String> getOutcomeParamMap(OutcomeType outcomeType) {
        return outcomeParamMap.get(outcomeType);
    }
}
