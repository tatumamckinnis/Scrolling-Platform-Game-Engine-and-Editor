/**
 * Core Event class for engine gameplay
 * Has predefined Condition variables that need to be fulfilled to execute list of Events
 * Associated with a gameObject
 * GameObject stores DynamicVariableCollection of parameters representing data for condition checker and outcome execution
 * @author Gage Garcia
 */
package oogasalad.engine.event;

import oogasalad.engine.model.object.GameObject;

import java.util.List;
import java.util.Map;

public class Event {
    private final GameObject gameObject;
    private final List<List<EventCondition>> conditions;
    private final List<EventOutcome> outcomes;
    private final EventType eventType;
    //stored within game object
    private final Map<String, String> params;

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
    public Event(GameObject gameObject, List<List<EventCondition>> conditions, List<EventOutcome> outcomes, EventType eventType) {
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
    public List<List<EventCondition>> getConditions() {
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
    public Map<String, String> getParams() {
        return params;
    }

    /**
     *
     * @return this Event's gameObject
     */
    public GameObject getGameObject() {
        return gameObject;
    }
}
