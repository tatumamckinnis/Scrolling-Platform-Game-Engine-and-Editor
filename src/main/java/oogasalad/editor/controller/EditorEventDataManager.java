package oogasalad.editor.controller;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.EditorObject;
import oogasalad.editor.model.data.event_enum.ConditionType;
import oogasalad.editor.model.data.event_enum.OutcomeType;
import oogasalad.editor.model.data.object.event.EditorEvent;
import oogasalad.editor.model.data.object.event.EditorEventData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Abstract class for managing event data (Conditions, Outcomes) associated with EditorObjects.
 * Subclasses determine which specific event data container (e.g., InputData, CollisionData) is
 * used.
 */
public abstract class EditorEventDataManager {

  private static final Logger LOG = LogManager.getLogger(
      EditorEventDataManager.class); // Add logger

  private final EditorLevelData level;

  /**
   * Constructs an EditorEventDataManager with the provided EditorLevelData.
   *
   * @param level the EditorLevelData instance to be used for managing events
   */
  public EditorEventDataManager(EditorLevelData level) {
    this.level = level;
  }

  /**
   * Creates and returns the specific {@link EditorEventData} container for the given
   * {@link EditorObject} if it is absent.
   *
   * @param object the EditorObject to create event data for
   * @return the corresponding EditorEventData container
   */
  protected abstract EditorEventData createDataIfAbsent(EditorObject object);

  /**
   * Helper method to get the target EditorObject, handling null cases.
   *
   * @param objectId The UUID of the object to obtain
   * @return the {@link EditorObject} with the UUID
   */
  protected EditorObject getObject(UUID objectId) {
    Objects.requireNonNull(objectId, "Object ID cannot be null.");
    EditorObject object = level.getEditorObject(objectId);
    if (object == null) {
      LOG.error("Object with ID {} not found.", objectId);
      throw new IllegalArgumentException("Object not found: " + objectId);
    }
    return object;
  }

  /**
   * Helper method to get the specific EditorEvent, handling null/missing cases.
   *
   * @param objectId the object UUID to get the EditorEvent from
   * @param eventId  the event UUID of the EditorEvent to get
   * @return the {@link EditorEvent} of the eventId from the {@link EditorObject} of objectId
   */
  protected EditorEvent getEvent(UUID objectId, String eventId) {
    Objects.requireNonNull(eventId, "Event ID cannot be null.");
    EditorObject object = getObject(objectId);
    EditorEventData eventData = createDataIfAbsent(object);
    EditorEvent event = eventData.getEvent(eventId);
    if (event == null) {
      LOG.error("Event '{}' not found for object {}.", eventId, objectId);
      throw new IllegalArgumentException("Event not found: " + eventId + " for object " + objectId);
    }
    return event;
  }

  /**
   * Adds a new event with the specified event ID to the EditorObject identified by objectId.
   *
   * @param objectId the UUID of the EditorObject
   * @param eventId  the UUID for the event to be added
   */
  public void addEvent(UUID objectId, String eventId) {
    EditorObject object = getObject(objectId);
    EditorEventData eventData = createDataIfAbsent(object);
    eventData.addEvent(eventId, new EditorEvent());
    LOG.debug("Added event '{}' for object {}", eventId, objectId);
  }

  /**
   * Removes the event identified by the specified event ID from the EditorObject.
   *
   * @param objectId the UUID of the EditorObject
   * @param eventId  the UUID of the event to be removed
   */
  public void removeEvent(UUID objectId, String eventId) {
    EditorObject object = getObject(objectId);
    EditorEventData eventData = createDataIfAbsent(object);
    boolean removed = eventData.removeEvent(eventId);
    if (removed) {
      LOG.debug("Removed event '{}' for object {}", eventId, objectId);
    } else {
      LOG.warn("Attempted to remove non-existent event '{}' for object {}", eventId, objectId);
    }
  }

  /**
   * Adds a condition of the specified type to the event identified by the event ID for the given
   * EditorObject.
   *
   * @param objectId  the UUID of the EditorObject
   * @param eventId   the UUID of the event to which the condition is to be added
   * @param condition the ConditionType to add
   */
  public void addEventCondition(UUID objectId, String eventId, ConditionType condition) {
    EditorEvent event = getEvent(objectId, eventId);
    event.addCondition(condition);
    LOG.debug("Added condition '{}' to event '{}' for object {}", condition, eventId, objectId);
  }

  /**
   * Removes the condition of the specified type from the event identified by the event ID for the
   * given EditorObject.
   *
   * @param objectId  the UUID of the EditorObject
   * @param eventId   the UUID of the event from which the condition is to be removed
   * @param condition the ConditionType to remove
   */
  public void removeEventCondition(UUID objectId, String eventId, ConditionType condition) {
    EditorEvent event = getEvent(objectId, eventId);
    event.removeCondition(condition);
    // TODO: Instead of removing an entire condition, delete an index instead
    LOG.debug("Attempted removal of condition '{}' from event '{}' for object {}", condition,
        eventId, objectId);
  }

  /**
   * Adds a outcome of the specified type to the event identified by the event ID for the given
   * EditorObject.
   *
   * @param objectId  the UUID of the EditorObject
   * @param eventId   the UUID of the event to which the condition is to be added
   * @param outcome   the OutcomeType to add
   */
  public void addEventOutcome(UUID objectId, String eventId, OutcomeType outcome) {
    EditorEvent event = getEvent(objectId, eventId);
    event.addOutcome(outcome);
    if (!event.getOutcomes().contains(outcome)) {
      LOG.warn("Outcome '{}' might already exist in event '{}' for object {}", outcome, eventId,
          objectId);
    }
    event.setOutcomeParameter(outcome, event.getOutcomeParameter(outcome));
    LOG.debug("Added outcome '{}' to event '{}' for object {}", outcome, eventId, objectId);
  }

  /**
   * Removes the outcome of the specified type from the event identified by the event ID for the
   * given EditorObject.
   *
   * @param objectId  the UUID of the EditorObject
   * @param eventId   the UUID of the event from which the condition is to be removed
   * @param outcome   the OutcomeType to remove
   */
  public void removeEventOutcome(UUID objectId, String eventId, OutcomeType outcome) {
    EditorEvent event = getEvent(objectId, eventId);
    event.removeOutcome(outcome);
    // TODO: Instead of removing an entire condition, delete an index instead
    LOG.debug("Attempted removal of outcome '{}' from event '{}' for object {}", outcome, eventId,
        objectId);
  }


  /**
   * Sets the parameter associated with a specific outcome within an event.
   *
   * @param objectId  The ID of the object containing the event.
   * @param eventId   The ID of the event.
   * @param outcome   The outcome whose parameter should be set.
   * @param parameter The parameter value (String), can be null to clear.
   */
  public void setEventOutcomeParameter(UUID objectId, String eventId, OutcomeType outcome,
      String parameter) {
    Objects.requireNonNull(outcome, "OutcomeType cannot be null.");
    EditorEvent event = getEvent(objectId, eventId);
    event.setOutcomeParameter(outcome, parameter);
    LOG.debug("Set parameter for outcome '{}' in event '{}' for object {} to '{}'", outcome,
        eventId, objectId, parameter);
  }


  /**
   * Gets the parameter associated with a specific outcome within an event.
   *
   * @param objectId The ID of the object containing the event.
   * @param eventId  The ID of the event.
   * @param outcome  The outcome whose parameter should be retrieved.
   * @return The parameter value (String), or null if not set or not found.
   */
  public String getEventOutcomeParameter(UUID objectId, String eventId, OutcomeType outcome) {
    Objects.requireNonNull(outcome, "OutcomeType cannot be null.");
    try {
      EditorEvent event = getEvent(objectId, eventId); // Handles object/event not found
      String parameter = event.getOutcomeParameter(outcome);
      LOG.trace("Retrieved parameter for outcome '{}' in event '{}' for object {}: '{}'", outcome,
          eventId, objectId, parameter);
      return parameter;
    } catch (IllegalArgumentException e) {
      LOG.warn("Could not get parameter for outcome '{}', event '{}', object {}: {}", outcome,
          eventId, objectId, e.getMessage());
      return null;
    } catch (Exception e) {
      LOG.error("Error getting parameter for outcome '{}', event '{}', object {}: {}", outcome,
          eventId, objectId, e.getMessage(), e);
      return null;
    }
  }


  /**
   * Gets the map of all events for the object.
   * @param objectId The UUID of the object to get the events of
   * @return The mapping of String to {@link EditorEvent}
   */
  public Map<String, EditorEvent> getEvents(UUID objectId) {
    EditorObject object = getObject(objectId);
    EditorEventData eventData = createDataIfAbsent(object);
    return eventData.getEvents();
  }

  /**
   * Gets the list of conditions for a specific event.
   * @param objectId the UUID of the object to get the event conditions of
   * @param eventId the ID of the event to get the conditions of
   * @return The list of {@link ConditionType} of the event
   */
  public List<ConditionType> getEventConditions(UUID objectId, String eventId) {
    EditorEvent event = getEvent(objectId, eventId);
    return event.getConditions();
  }

  /**
   * Gets the list of outcomes for a specific event.
   * @param objectId the UUID of the object to get the event conditions of
   * @param eventId the ID of the event to get the outcomes of
   * @return The list of {@link OutcomeType} of the event
   */
  public List<OutcomeType> getEventOutcomes(UUID objectId, String eventId) {
    EditorEvent event = getEvent(objectId, eventId);
    return event.getOutcomes();
  }
}