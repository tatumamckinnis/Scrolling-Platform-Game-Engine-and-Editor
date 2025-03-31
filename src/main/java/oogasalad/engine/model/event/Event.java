/**
 * Core Event class for engine gameplay
 * Has predefined Condition variables that need to be fulfilled to execute list of Events
 * Associated with a gameObjectId
 * Defined with map of parameters representing data for condition checker and outcome execution
 * @author Gage Garcia
 */
package oogasalad.engine.model.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;

public class Event {
    private int gameObjectId;
    private List<EventCondition> condition;
    private List<EventOutcome> outcome;
    private Map<String, String> params;

    /**
     * Event constructor
     * @param gameObjectId -> object associated with the event
     * @param conditions -> List of Conditions that need to be met to execute events
     * @param outcomes -> List of Events to execute
     * @param params -> Mapping of paramName -> paramValue
     */
    public Event(int gameObjectId, List<EventCondition> conditions, List<EventOutcome> outcomes, Map<String, String> params) {
        this.gameObjectId = gameObjectId;
        this.condition = conditions;
        this.outcome = outcomes;
        this.params = params;
    }

}
