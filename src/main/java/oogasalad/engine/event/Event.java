/**
 * Core Event class for engine gameplay
 * Has predefined Condition variables that need to be fulfilled to execute list of Events
 * Associated with a gameObjectId
 * Defined with map of parameters representing data for condition checker and outcome execution
 * @author Gage Garcia
 */
package oogasalad.engine.event;

import oogasalad.engine.model.object.DynamicVariableCollection;
import oogasalad.engine.model.object.GameObject;

import java.util.List;
import java.util.Map;

public class Event {
    private GameObject gameObject;
    private List<EventCondition> conditions;
    private List<EventOutcome> outcomes;
    //stored within game object
    private DynamicVariableCollection params;

    /**
     * Event constructor
     * @param gameObject -> object associated with the event
     * @param conditions -> List of Conditions that need to be met to execute events
     * @param outcomes -> List of Events to execute
     */
    public Event(GameObject gameObject, List<EventCondition> conditions, List<EventOutcome> outcomes) {
        this.gameObject = gameObject;
        this.conditions = conditions;
        this.outcomes = outcomes;
        this.params = gameObject.getParams();
    }

    public List<EventCondition> getConditions() {
        return this.conditions;
    }

    public List<EventOutcome> getOutcomes() {
        return this.outcomes;
    }



}
