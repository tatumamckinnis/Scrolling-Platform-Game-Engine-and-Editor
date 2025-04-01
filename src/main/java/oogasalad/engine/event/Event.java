/**
 * Core Event class for engine gameplay
 * Has predefined Condition variables that need to be fulfilled to execute list of Events
 * Associated with a gameObject
 * GameObject stores DynamicVariableCollection of parameters representing data for condition checker and outcome execution
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
    private EventType eventType;
    //stored within game object
    private DynamicVariableCollection params;

    public enum EventType {
        INPUT,
        PHYSICS,
        COLLISION
    }

    /**
     * Event constructor
     * @param gameObject -> object associated with the event
     * @param conditions -> List of Conditions that need to be met to execute events
     * @param outcomes -> List of Events to execute
     */
    public Event(GameObject gameObject, List<EventCondition> conditions, List<EventOutcome> outcomes, EventType eventType) {
        this.gameObject = gameObject;
        this.conditions = conditions;
        this.outcomes = outcomes;
        this.params = gameObject.getParams();
        this.eventType = eventType;
    }

    /**
     *
     * @return this Event's list of event conditions
     */
    public List<EventCondition> getConditions() {
        return this.conditions;
    }

    /**
     *
     * @return this Event's list of event outcomes
     */
    public List<EventOutcome> getOutcomes() {
        return this.outcomes;
    }

    /**
     *
     * @return this Event's event type
     */
    public EventType getEventType() {
        return eventType;
    }

    /**
     *
     * @return this Event's gameobjects's user-defined parameters
     */
    public DynamicVariableCollection getParams() {
        return params;
    }
}
