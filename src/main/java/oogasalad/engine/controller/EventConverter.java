package oogasalad.engine.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import oogasalad.engine.model.event.Event;
import oogasalad.engine.model.event.condition.EventCondition;
import oogasalad.engine.model.event.outcome.EventOutcome;
import oogasalad.engine.model.object.GameObject;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.ConditionData;
import oogasalad.fileparser.records.EventData;
import oogasalad.fileparser.records.GameObjectData;
import oogasalad.fileparser.records.OutcomeData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class responsible for converting {@link EventData} from the file parser into fully
 * constructed {@link Event} objects tied to a specific {@link GameObject}.
 *
 * <p>This conversion includes parsing the event's conditions, outcomes, and type.
 */
public class EventConverter {

  private static final org.apache.logging.log4j.Logger log = LogManager.getLogger(
      EventConverter.class);
  private static final Logger LOG = LogManager.getLogger();
  /**
   * Converts all event data associated with a {@link GameObjectData} instance into a list of
   * {@link Event} objects tied to the provided {@link GameObject}.
   *
   * @param gameObjectData the parsed data containing raw event information
   * @param gameObject     the game object the events will be attached to
   * @return a list of constructed {@link Event} objects
   */
  public static List<Event> convertEventData(GameObjectData gameObjectData,
      GameObject gameObject, Map<Integer, BlueprintData> bluePrintMap) {
    List<Event> events = new ArrayList<>();
    for (EventData event : bluePrintMap.get(gameObjectData.blueprintId()).eventDataList()) {
      if (event == null) {
        continue;
      }
      Event e = makeEventObject(event, gameObject);
      events.add(e);
    }
    return events;
  }

  /**
   * Converts a single {@link EventData} record into an {@link Event} object.
   *
   * @param eventData  the raw data for a single event
   * @param gameObject the object that owns the event
   * @return a constructed {@link Event} instance
   */
  private static Event makeEventObject(EventData eventData, GameObject gameObject) {
    List<List<EventCondition>> eventConditions = makeEventConditions(eventData);
    List<EventOutcome> eventOutcomes = makeEventOutcomes(eventData);
    Event.EventType type = makeEventType(eventData);
    return new Event(gameObject, eventConditions, eventOutcomes, type);
  }

  /**
   * Parses the list of condition strings from an {@link EventData} and converts them into a list of
   * {@link EventCondition} objects.
   *
   * @param eventData the raw event data containing conditions
   * @return a list of parsed {@link EventCondition} instances
   */
  private static List<List<EventCondition>> makeEventConditions(EventData eventData) {
    List<List<EventCondition>> eventConditions = new ArrayList<>();
    for (List<ConditionData> eventCondition : eventData.conditions()) {
      List<EventCondition> conditions = new ArrayList<>();
      for (ConditionData condition : eventCondition) {
        EventCondition newCondition = new EventCondition(
            EventCondition.ConditionType.valueOf(condition.name()), condition.stringProperties(),
            condition.doubleProperties());

        for (Map.Entry<String,String> e : condition.stringProperties().entrySet()) {
          LOG.info("stringProperty: " + e.getKey() + " -> " + e.getValue());
        }

        for (Map.Entry<String,Double> e : condition.doubleProperties().entrySet()) {
          LOG.info("doubleProperty: " + e.getKey() + " -> " + e.getValue());
        }
        conditions.add(newCondition);
      }
      eventConditions.add(conditions);
    }
    return eventConditions;
  }

  /**
   * Parses the list of outcome strings from an {@link EventData} and converts them into a list of
   * {@link EventOutcome} objects.
   *
   * @param eventData the raw event data containing outcomes
   * @return a list of parsed {@link EventOutcome} instances
   */
  private static List<EventOutcome> makeEventOutcomes(EventData eventData) {
    List<EventOutcome> eventOutcomes = new ArrayList<>();
    for (OutcomeData outcome : eventData.outcomes()) {
      EventOutcome newOutcome = new EventOutcome(EventOutcome.OutcomeType.valueOf(outcome.name()),
          outcome.stringProperties(), outcome.doubleProperties());
      eventOutcomes.add(newOutcome);
    }
    return eventOutcomes;
  }

  /**
   * Extracts and converts the event type string from an {@link EventData} into an
   * {@link Event.EventType} enum.
   *
   * @param eventData the raw event data
   * @return the event type enum
   */
  private static Event.EventType makeEventType(EventData eventData) {
    return Event.EventType.valueOf(eventData.type().toUpperCase());
  }
}
