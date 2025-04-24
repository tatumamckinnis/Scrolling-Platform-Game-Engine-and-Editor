package oogasalad.editor.controller.object_data;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.EditorObject;
import oogasalad.editor.model.data.object.event.EditorEvent;
import oogasalad.editor.model.data.object.event.AbstractEventMapData;
import oogasalad.editor.model.data.object.event.ExecutorData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Abstract class for managing event data (conditions, outcomes, and their parameters) associated
 * with {@link EditorObject}s. Sub‑classes decide which concrete {@link AbstractEventMapData} container
 * (e.g. input, collision, timer, etc.) is used to hold the event data for an editor object.
 *
 * @author Jacob You
 */
public abstract class EditorEventDataManager {

  private static final Logger LOG = LogManager.getLogger(EditorEventDataManager.class);

  private final EditorLevelData level;

  /**
   * Constructs a manager for the supplied level.
   *
   * @param level the level whose objects' events are being managed
   */
  protected EditorEventDataManager(EditorLevelData level) {
    this.level = level;
  }

  /**
   * Creates (if necessary) and returns the concrete event‑data container for the given editor
   * object.
   * <p>
   * Implemented by sub‑classes so that each manager knows where its events reside.
   * </p>
   *
   * @param object the {@link EditorObject} for which to obtain the event data container
   * @return the corresponding {@link AbstractEventMapData}
   */
  protected abstract AbstractEventMapData createDataIfAbsent(EditorObject object);

  /**
   * Retrieves the {@link EditorObject} corresponding to the supplied UUID.
   *
   * @param objectId the unique identifier of the editor object
   * @return the corresponding {@link EditorObject}
   * @throws NullPointerException     if the provided objectId is null
   * @throws IllegalArgumentException if no object with the given ID is found
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
   * Public access point to retrieve an EditorObject by its ID.
   * Delegates to the internal getObject method.
   *
   * @param objectId The unique identifier of the editor object.
   * @return The corresponding EditorObject.
   * @throws NullPointerException     if objectId is null.
   * @throws IllegalArgumentException if no object with the given ID is found.
   */
  public EditorObject getObjectById(UUID objectId) {
    return getObject(objectId);
  }

  /**
   * Retrieves the {@link EditorEvent} for the given object and event identifier.
   *
   * @param objectId the unique identifier of the editor object
   * @param eventId  the identifier of the event
   * @return the corresponding {@link EditorEvent}
   * @throws NullPointerException     if the provided eventId is null
   * @throws IllegalArgumentException if no event with the specified ID is found for the object
   */
  protected EditorEvent getEvent(UUID objectId, String eventId) {
    Objects.requireNonNull(eventId, "Event ID cannot be null.");
    EditorObject object = getObject(objectId);
    AbstractEventMapData data = createDataIfAbsent(object);
    EditorEvent event = data.getEvent(eventId);
    if (event == null) {
      LOG.error("Event '{}' not found for object {}.", eventId, objectId);
      throw new IllegalArgumentException("Event not found: " + eventId + " for object " + objectId);
    }
    return event;
  }

  /**
   * Adds a new event to the editor object identified by the given UUID.
   *
   * @param objectId the unique identifier of the editor object
   * @param eventId  the identifier for the new event
   */
  public void addEvent(UUID objectId, String eventId) {
    EditorObject object = getObject(objectId);
    createDataIfAbsent(object).addEvent(eventId, new EditorEvent());
    LOG.debug("Added event '{}' for object {}", eventId, objectId);
  }

  /**
   * Removes an event identified by the given eventId from the editor object.
   *
   * @param objectId the unique identifier of the editor object
   * @param eventId  the identifier of the event to be removed
   */
  public void removeEvent(UUID objectId, String eventId) {
    EditorObject object = getObject(objectId);
    boolean removed = createDataIfAbsent(object).removeEvent(eventId);
    if (removed) {
      LOG.debug("Removed event '{}' for object {}", eventId, objectId);
    } else {
      LOG.warn("Attempted to remove non‑existent event '{}' for object {}", eventId, objectId);
    }
  }

  /**
   * Retrieves a map of all events for the editor object.
   *
   * @param objectId the unique identifier of the editor object
   * @return a map with event IDs as keys and corresponding {@link EditorEvent} objects as values
   */
  public Map<String, EditorEvent> getEvents(UUID objectId) {
    return createDataIfAbsent(getObject(objectId)).getEvents();
  }

  /**
   * Adds an empty condition group to the specified event for the editor object.
   *
   * @param objectId the unique identifier of the editor object
   * @param eventId  the identifier of the event to update
   */
  public void addConditionGroup(UUID objectId, String eventId) {
    getEvent(objectId, eventId).addConditionGroup();
    LOG.debug("Added empty condition group to event '{}' for object {}", eventId, objectId);
  }

  /**
   * Adds a condition of the specified type to a particular group within an event.
   *
   * @param objectId   the unique identifier of the editor object
   * @param eventId    the identifier of the event to update
   * @param groupIndex the index of the condition group
   * @param type       the condition type to add (as a String)
   */
  public void addEventCondition(UUID objectId, String eventId, int groupIndex, String type) {
    EditorEvent event = getEvent(objectId, eventId);
    ensureGroupExists(event, groupIndex);
    event.addCondition(groupIndex, type);
    LOG.debug("Added condition '{}' in group '{}' to event '{}' for object {}", type, groupIndex,
        eventId, objectId);
  }

  /**
   * Removes a condition from a specified group within an event.
   *
   * @param objectId   the unique identifier of the editor object
   * @param eventId    the identifier of the event to update
   * @param groupIndex the index of the condition group
   * @param index      the index of the condition within the group to remove
   */
  public void removeEventCondition(UUID objectId, String eventId, int groupIndex, int index) {
    EditorEvent event = getEvent(objectId, eventId);
    event.removeCondition(groupIndex, index);
    LOG.debug("Removed condition at [{},{}] from event '{}' for object {}", groupIndex, index,
        eventId, objectId);
  }

  /**
   * Removes an entire condition group from an event.
   *
   * @param objectId   the unique identifier of the editor object
   * @param eventId    the identifier of the event to update
   * @param groupIndex the index of the condition group to remove
   */
  public void removeConditionGroup(UUID objectId, String eventId, int groupIndex) {
    EditorEvent event = getEvent(objectId, eventId);
    event.removeConditionGroup(groupIndex);
    LOG.debug("Removed condition group '{}' from event '{}' for object {}", groupIndex, eventId,
        objectId);
  }

  /**
   * Sets a String parameter for a condition within an event.
   *
   * @param objectId   the unique identifier of the editor object
   * @param eventId    the identifier of the event containing the condition
   * @param groupIndex the index of the condition group
   * @param index      the index of the condition within the group
   * @param paramName  the name of the parameter to set
   * @param value      the String value to set for the parameter
   */
  public void setEventConditionStringParameter(UUID objectId, String eventId, int groupIndex,
      int index,
      String paramName, String value) {
    getEvent(objectId, eventId).setConditionStringParameter(groupIndex, index, paramName, value);
    LOG.trace("Set String param '{}'='{}' on condition [{},{}] of event '{}' for object {}",
        paramName, value, groupIndex, index, eventId, objectId);
  }

  /**
   * Sets a Double parameter for a condition within an event.
   *
   * @param objectId   the unique identifier of the editor object
   * @param eventId    the identifier of the event containing the condition
   * @param groupIndex the index of the condition group
   * @param index      the index of the condition within the group
   * @param paramName  the name of the parameter to set
   * @param value      the Double value to set for the parameter
   */
  public void setEventConditionDoubleParameter(UUID objectId, String eventId, int groupIndex,
      int index,
      String paramName, Double value) {
    getEvent(objectId, eventId).setConditionDoubleParameter(groupIndex, index, paramName, value);
    LOG.trace("Set Double param '{}'={} on condition [{},{}] of event '{}' for object {}",
        paramName, value, groupIndex, index, eventId, objectId);
  }

  /**
   * Retrieves all condition groups for a specified event of the editor object.
   *
   * @param objectId the unique identifier of the editor object
   * @param eventId  the identifier of the event to query
   * @return a list of condition groups, where each group is a list of {@link ExecutorData}
   * representing conditions
   */
  public List<List<ExecutorData>> getEventConditions(UUID objectId, String eventId) {
    return getEvent(objectId, eventId).getConditions();
  }

  /**
   * Retrieves a specific condition group from an event.
   *
   * @param objectId   the unique identifier of the editor object
   * @param eventId    the identifier of the event to query
   * @param groupIndex the index of the condition group
   * @return a list of {@link ExecutorData} representing the conditions in the specified group
   */
  public List<ExecutorData> getEventConditionGroup(UUID objectId, String eventId, int groupIndex) {
    return getEvent(objectId, eventId).getConditionGroup(groupIndex);
  }

  /**
   * Adds an outcome of a specified type to an event.
   *
   * @param objectId    the unique identifier of the editor object
   * @param eventId     the identifier of the event to update
   * @param outcomeType the type of outcome to add (as a String)
   */
  public void addEventOutcome(UUID objectId, String eventId, String outcomeType) {
    getEvent(objectId, eventId).addOutcome(outcomeType);
    LOG.debug("Added outcome '{}' to event '{}' for object {}", outcomeType, eventId, objectId);
  }

  /**
   * Removes an outcome at the specified index from an event.
   *
   * @param objectId the unique identifier of the editor object
   * @param eventId  the identifier of the event to update
   * @param index    the index of the outcome to remove
   */
  public void removeEventOutcome(UUID objectId, String eventId, int index) {
    getEvent(objectId, eventId).removeOutcome(index);
    LOG.debug("Removed outcome index '{}' from event '{}' for object {}", index, eventId, objectId);
  }

  /**
   * Sets a String parameter for an outcome within an event.
   *
   * @param objectId  the unique identifier of the editor object
   * @param eventId   the identifier of the event containing the outcome
   * @param index     the index of the outcome to update
   * @param paramName the name of the parameter to set
   * @param value     the String value to set for the parameter
   */
  public void setEventOutcomeStringParameter(UUID objectId, String eventId, int index,
      String paramName, String value) {
    getEvent(objectId, eventId).setOutcomeStringParameter(index, paramName, value);
    LOG.trace("Set String param '{}'='{}' on outcome[{}] of event '{}' for object {}", paramName,
        value, index, eventId, objectId);
  }

  /**
   * Sets a Double parameter for an outcome within an event.
   *
   * @param objectId  the unique identifier of the editor object
   * @param eventId   the identifier of the event containing the outcome
   * @param index     the index of the outcome to update
   * @param paramName the name of the parameter to set
   * @param value     the Double value to set for the parameter
   */
  public void setEventOutcomeDoubleParameter(UUID objectId, String eventId, int index,
      String paramName, Double value) {
    getEvent(objectId, eventId).setOutcomeDoubleParameter(index, paramName, value);
    LOG.trace("Set Double param '{}'={} on outcome[{}] of event '{}' for object {}", paramName,
        value, index, eventId, objectId);
  }

  /**
   * Retrieves all outcomes for a specified event of the editor object.
   *
   * @param objectId the unique identifier of the editor object
   * @param eventId  the identifier of the event to query
   * @return a list of {@link ExecutorData} representing the outcomes of the event
   */
  public List<ExecutorData> getEventOutcomes(UUID objectId, String eventId) {
    return getEvent(objectId, eventId).getOutcomes();
  }

  /**
   * Retrieves outcome data at the specified index from an event.
   *
   * @param objectId the unique identifier of the editor object
   * @param eventId  the identifier of the event to query
   * @param index    the index of the outcome to retrieve
   * @return the {@link ExecutorData} for the specified outcome
   */
  public ExecutorData getEventOutcomeData(UUID objectId, String eventId, int index) {
    return getEvent(objectId, eventId).getOutcomeData(index);
  }

  private void ensureGroupExists(EditorEvent event, int groupIndex) {
    while (event.getConditions().size() <= groupIndex) {
      event.addConditionGroup();
    }
  }
}