package oogasalad.editor.controller.object_data;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import oogasalad.editor.controller.EditorDataAPI;
import oogasalad.editor.controller.listeners.EditorListenerNotifier;
import oogasalad.editor.model.data.object.event.EditorEvent;
import oogasalad.editor.model.data.object.event.ExecutorData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles editing of events, conditions, and outcomes associated with editor objects.
 * Provides methods for adding, removing, modifying, and retrieving event-related data.
 */
public class EditorEventHandler {

  private static final Logger LOG = LogManager.getLogger(EditorEventHandler.class);
  private final EditorDataAPI editorDataAPI;
  private final EditorListenerNotifier notifier;

  /**
   * Constructs an EditorEventHandler.
   *
   * @param editorDataAPI The data API to access and modify event data.
   * @param notifier      The notifier to signal object updates and errors.
   */
  public EditorEventHandler(EditorDataAPI editorDataAPI, EditorListenerNotifier notifier) {
    this.editorDataAPI = editorDataAPI;
    this.notifier = notifier;
  }

  /**
   * Adds a new event definition to the specified object.
   *
   * @param objectId UUID of the target object. Must not be null.
   * @param eventId  String identifier for the new event. Must not be null or empty.
   */
  public void addEvent(UUID objectId, String eventId) {
    if (!validateEventInput(objectId, eventId, "addEvent")) return;
    if (eventId.trim().isEmpty()) {
      notifier.notifyErrorOccurred("Event ID cannot be empty.");
      return;
    }
    handleEventOperation(objectId,
        () -> editorDataAPI.getInputDataAPI().addEvent(objectId, eventId),
        String.format("add event '%s' to object %s", eventId, objectId)
    );
  }

  /**
   * Removes an event definition from the specified object.
   *
   * @param objectId UUID of the target object. Must not be null.
   * @param eventId  String identifier of the event to remove. Must not be null.
   */
  public void removeEvent(UUID objectId, String eventId) {
    if (!validateEventInput(objectId, eventId, "removeEvent")) return;
    handleEventOperation(objectId,
        () -> editorDataAPI.getInputDataAPI().removeEvent(objectId, eventId),
        String.format("remove event '%s' from object %s", eventId, objectId)
    );
  }

  /**
   * Gets all events associated with the specified object ID.
   *
   * @param objectId UUID of the target object.
   * @return A Map where keys are event IDs (String) and values are EditorEvent objects. Returns an empty map on failure or if objectId is null.
   */
  public Map<String, EditorEvent> getEventsForObject(UUID objectId) {
    if (objectId == null) return Collections.emptyMap();
    try {
      Map<String, EditorEvent> events = editorDataAPI.getInputDataAPI().getEvents(objectId);
      return (events != null) ? events : Collections.emptyMap();
    } catch (Exception e) {
      handleRetrievalError(e, "get events for object " + objectId);
      return Collections.emptyMap();
    }
  }

  /**
   * Adds an empty condition group (OR block) to the specified event.
   *
   * @param objectId UUID of the target object. Must not be null.
   * @param eventId  String identifier of the target event. Must not be null.
   */
  public void addConditionGroup(UUID objectId, String eventId) {
    if (!validateEventInput(objectId, eventId, "addConditionGroup")) return;
    handleEventOperation(objectId,
        () -> editorDataAPI.getInputDataAPI().addConditionGroup(objectId, eventId),
        String.format("add condition group to event '%s' on object %s", eventId, objectId)
    );
  }

  /**
   * Adds a condition of a specific type to a specified group within an event.
   *
   * @param objectId      UUID of the target object. Must not be null.
   * @param eventId       String identifier of the target event. Must not be null.
   * @param groupIndex    Index of the condition group to add to (non-negative).
   * @param conditionType String identifier of the condition type. Must not be null.
   */
  public void addEventCondition(UUID objectId, String eventId, int groupIndex, String conditionType) {
    if (!validateConditionInput(objectId, eventId, conditionType, groupIndex, "addEventCondition")) return;
    handleEventOperation(objectId,
        () -> editorDataAPI.getInputDataAPI().addEventCondition(objectId, eventId, groupIndex, conditionType),
        String.format("add condition '%s' to group %d of event '%s' on object %s", conditionType, groupIndex, eventId, objectId)
    );
  }

  /**
   * Removes a condition at a specific index within a specific group of an event.
   *
   * @param objectId       UUID of the target object. Must not be null.
   * @param eventId        String identifier of the target event. Must not be null.
   * @param groupIndex     Index of the condition group (non-negative).
   * @param conditionIndex Index of the condition within the group to remove (non-negative).
   */
  public void removeEventCondition(UUID objectId, String eventId, int groupIndex, int conditionIndex) {
    if (!validateEventInput(objectId, eventId, "removeEventCondition")) return;
    if (groupIndex < 0 || conditionIndex < 0) {
      notifier.notifyErrorOccurred("Indices cannot be negative for removeEventCondition.");
      return;
    }
    handleEventOperation(objectId,
        () -> editorDataAPI.getInputDataAPI().removeEventCondition(objectId, eventId, groupIndex, conditionIndex),
        String.format("remove condition [%d,%d] from event '%s' on object %s", groupIndex, conditionIndex, eventId, objectId)
    );
  }

  /**
   * Removes an entire condition group from an event.
   *
   * @param objectId   UUID of the target object. Must not be null.
   * @param eventId    String identifier of the target event. Must not be null.
   * @param groupIndex Index of the condition group to remove (non-negative).
   */
  public void removeConditionGroup(UUID objectId, String eventId, int groupIndex) {
    if (!validateEventInput(objectId, eventId, "removeConditionGroup")) return;
    if (groupIndex < 0) {
      notifier.notifyErrorOccurred("Group index cannot be negative for removeConditionGroup.");
      return;
    }
    handleEventOperation(objectId,
        () -> editorDataAPI.getInputDataAPI().removeConditionGroup(objectId, eventId, groupIndex),
        String.format("remove condition group %d from event '%s' on object %s", groupIndex, eventId, objectId)
    );
  }

  /**
   * Sets a String parameter for a specific condition within an event.
   *
   * @param objectId       UUID of the target object. Must not be null.
   * @param eventId        String identifier of the target event. Must not be null.
   * @param groupIndex     Index of the condition group (non-negative).
   * @param conditionIndex Index of the condition within the group (non-negative).
   * @param paramName      Name of the parameter to set. Must not be null.
   * @param value          The String value (can be null).
   */
  public void setEventConditionStringParameter(UUID objectId, String eventId, int groupIndex, int conditionIndex, String paramName, String value) {
    if (!validateParamInput(objectId, eventId, paramName, "setEventConditionStringParameter")) return;
    if (groupIndex < 0 || conditionIndex < 0) {
      notifier.notifyErrorOccurred("Indices cannot be negative for setEventConditionStringParameter.");
      return;
    }
    handleParameterSetOperation(
        () -> editorDataAPI.getInputDataAPI().setEventConditionStringParameter(objectId, eventId, groupIndex, conditionIndex, paramName, value),
        String.format("set condition String param '%s' at [%d,%d] on event '%s', object %s", paramName, groupIndex, conditionIndex, eventId, objectId)
    );
  }

  /**
   * Sets a Double parameter for a specific condition within an event.
   *
   * @param objectId       UUID of the target object. Must not be null.
   * @param eventId        String identifier of the target event. Must not be null.
   * @param groupIndex     Index of the condition group (non-negative).
   * @param conditionIndex Index of the condition within the group (non-negative).
   * @param paramName      Name of the parameter to set. Must not be null.
   * @param value          The Double value (can be null).
   */
  public void setEventConditionDoubleParameter(UUID objectId, String eventId, int groupIndex, int conditionIndex, String paramName, Double value) {
    if (!validateParamInput(objectId, eventId, paramName, "setEventConditionDoubleParameter")) return;
    if (groupIndex < 0 || conditionIndex < 0) {
      notifier.notifyErrorOccurred("Indices cannot be negative for setEventConditionDoubleParameter.");
      return;
    }
    handleParameterSetOperation(
        () -> editorDataAPI.getInputDataAPI().setEventConditionDoubleParameter(objectId, eventId, groupIndex, conditionIndex, paramName, value),
        String.format("set condition Double param '%s' at [%d,%d] on event '%s', object %s", paramName, groupIndex, conditionIndex, eventId, objectId)
    );
  }

  /**
   * Gets all condition groups and their conditions for a specific event.
   *
   * @param objectId UUID of the target object. Must not be null.
   * @param eventId  String identifier of the target event. Must not be null.
   * @return A List of condition groups (List<List<ExecutorData>>). Returns an empty list on failure or if object/event ID is null.
   */
  public List<List<ExecutorData>> getEventConditions(UUID objectId, String eventId) {
    if (objectId == null || eventId == null) return Collections.emptyList();
    try {
      List<List<ExecutorData>> conditions = editorDataAPI.getInputDataAPI().getEventConditions(objectId, eventId);
      return (conditions != null) ? conditions : Collections.emptyList();
    } catch (Exception e) {
      handleRetrievalError(e, String.format("get conditions for event '%s' on object %s", eventId, objectId));
      return Collections.emptyList();
    }
  }

  /**
   * Gets a specific condition group from an event.
   *
   * @param objectId   UUID of the target object. Must not be null.
   * @param eventId    String identifier of the target event. Must not be null.
   * @param groupIndex Index of the condition group (non-negative).
   * @return A List of ExecutorData for the specified group, or null if not found or an error occurs.
   */
  public List<ExecutorData> getEventConditionGroup(UUID objectId, String eventId, int groupIndex) {
    if (objectId == null || eventId == null) return null;
    if (groupIndex < 0) {
      notifier.notifyErrorOccurred("Group index cannot be negative for getEventConditionGroup.");
      return null;
    }
    try {
      return editorDataAPI.getInputDataAPI().getEventConditionGroup(objectId, eventId, groupIndex);
    } catch (Exception e) {
      handleRetrievalError(e, String.format("get condition group %d for event '%s' on object %s", groupIndex, eventId, objectId));
      return null;
    }
  }

  /**
   * Adds an outcome of a specific type to an event.
   *
   * @param objectId    UUID of the target object. Must not be null.
   * @param eventId     String identifier of the target event. Must not be null.
   * @param outcomeType String identifier of the outcome type. Must not be null.
   */
  public void addEventOutcome(UUID objectId, String eventId, String outcomeType) {
    if (!validateOutcomeInput(objectId, eventId, outcomeType, "addEventOutcome")) return;
    handleEventOperation(objectId,
        () -> editorDataAPI.getInputDataAPI().addEventOutcome(objectId, eventId, outcomeType),
        String.format("add outcome '%s' to event '%s' on object %s", outcomeType, eventId, objectId)
    );
  }

  /**
   * Removes an outcome at a specific index from an event.
   *
   * @param objectId     UUID of the target object. Must not be null.
   * @param eventId      String identifier of the target event. Must not be null.
   * @param outcomeIndex Index of the outcome to remove (non-negative).
   */
  public void removeEventOutcome(UUID objectId, String eventId, int outcomeIndex) {
    if (!validateEventInput(objectId, eventId, "removeEventOutcome")) return;
    if (outcomeIndex < 0) {
      notifier.notifyErrorOccurred("Outcome index cannot be negative for removeEventOutcome.");
      return;
    }
    handleEventOperation(objectId,
        () -> editorDataAPI.getInputDataAPI().removeEventOutcome(objectId, eventId, outcomeIndex),
        String.format("remove outcome at index %d from event '%s' on object %s", outcomeIndex, eventId, objectId)
    );
  }

  /**
   * Sets a String parameter for a specific outcome within an event.
   *
   * @param objectId     UUID of the target object. Must not be null.
   * @param eventId      String identifier of the target event. Must not be null.
   * @param outcomeIndex Index of the outcome (non-negative).
   * @param paramName    Name of the parameter to set. Must not be null.
   * @param value        The String value (can be null).
   */
  public void setEventOutcomeStringParameter(UUID objectId, String eventId, int outcomeIndex, String paramName, String value) {
    if (!validateParamInput(objectId, eventId, paramName, "setEventOutcomeStringParameter")) return;
    if (outcomeIndex < 0) {
      notifier.notifyErrorOccurred("Outcome index cannot be negative for setEventOutcomeStringParameter.");
      return;
    }
    handleParameterSetOperation(
        () -> editorDataAPI.getInputDataAPI().setEventOutcomeStringParameter(objectId, eventId, outcomeIndex, paramName, value),
        String.format("set outcome String param '%s' at index %d on event '%s', object %s", paramName, outcomeIndex, eventId, objectId)
    );
  }

  /**
   * Sets a Double parameter for a specific outcome within an event.
   *
   * @param objectId     UUID of the target object. Must not be null.
   * @param eventId      String identifier of the target event. Must not be null.
   * @param outcomeIndex Index of the outcome (non-negative).
   * @param paramName    Name of the parameter to set. Must not be null.
   * @param value        The Double value (can be null).
   */
  public void setEventOutcomeDoubleParameter(UUID objectId, String eventId, int outcomeIndex, String paramName, Double value) {
    if (!validateParamInput(objectId, eventId, paramName, "setEventOutcomeDoubleParameter")) return;
    if (outcomeIndex < 0) {
      notifier.notifyErrorOccurred("Outcome index cannot be negative for setEventOutcomeDoubleParameter.");
      return;
    }
    handleParameterSetOperation(
        () -> editorDataAPI.getInputDataAPI().setEventOutcomeDoubleParameter(objectId, eventId, outcomeIndex, paramName, value),
        String.format("set outcome Double param '%s' at index %d on event '%s', object %s", paramName, outcomeIndex, eventId, objectId)
    );
  }

  /**
   * Gets all outcomes for a specific event.
   *
   * @param objectId UUID of the target object. Must not be null.
   * @param eventId  String identifier of the target event. Must not be null.
   * @return A List of ExecutorData representing the outcomes. Returns an empty list on failure or if object/event ID is null.
   */
  public List<ExecutorData> getEventOutcomes(UUID objectId, String eventId) {
    if (objectId == null || eventId == null) return Collections.emptyList();
    try {
      List<ExecutorData> outcomes = editorDataAPI.getInputDataAPI().getEventOutcomes(objectId, eventId);
      return (outcomes != null) ? outcomes : Collections.emptyList();
    } catch (Exception e) {
      handleRetrievalError(e, String.format("get outcomes for event '%s' on object %s", eventId, objectId));
      return Collections.emptyList();
    }
  }

  /**
   * Gets the data for a specific outcome at a given index within an event.
   *
   * @param objectId     UUID of the target object. Must not be null.
   * @param eventId      String identifier of the target event. Must not be null.
   * @param outcomeIndex Index of the desired outcome (non-negative).
   * @return The ExecutorData for the outcome, or null if not found or an error occurs.
   */
  public ExecutorData getEventOutcomeData(UUID objectId, String eventId, int outcomeIndex) {
    if (objectId == null || eventId == null) return null;
    if (outcomeIndex < 0) {
      notifier.notifyErrorOccurred("Outcome index cannot be negative for getEventOutcomeData.");
      return null;
    }
    try {
      return editorDataAPI.getInputDataAPI().getEventOutcomeData(objectId, eventId, outcomeIndex);
    } catch (Exception e) {
      handleRetrievalError(e, String.format("get outcome data at index %d for event '%s' on object %s", outcomeIndex, eventId, objectId));
      return null;
    }
  }


  private void handleEventOperation(UUID objectId, Runnable operation, String logDescription) {
    LOG.debug("Controller delegating {}", logDescription);
    try {
      operation.run();
      notifier.notifyObjectUpdated(objectId);
    } catch (Exception e) {
      LOG.error("Failed to {}: {}", logDescription, e.getMessage(), e);
      notifier.notifyErrorOccurred("Failed to " + logDescription.split(" ")[0] + ": " + e.getMessage());
    }
  }

  private void handleParameterSetOperation(Runnable operation, String logDescription) {
    LOG.trace("Controller delegating {}", logDescription);
    try {
      operation.run();
    } catch (Exception e) {
      LOG.error("Failed to {}: {}", logDescription, e.getMessage(), e);
      notifier.notifyErrorOccurred("Failed to set parameter: " + e.getMessage());
    }
  }

  private void handleRetrievalError(Exception e, String logDescription) {
    LOG.error("Failed to {}: {}", logDescription, e.getMessage(), e);
    notifier.notifyErrorOccurred("Failed to get data (" + logDescription.split(" ")[1] + "): " + e.getMessage());
  }

  private boolean validateEventInput(UUID objectId, String eventId, String operationName) {
    try {
      Objects.requireNonNull(objectId, String.format("Object ID cannot be null for %s", operationName));
      Objects.requireNonNull(eventId, String.format("Event ID cannot be null for %s", operationName));
      return true;
    } catch (NullPointerException e) {
      LOG.warn("Validation failed for {}: {}", operationName, e.getMessage());
      notifier.notifyErrorOccurred("Invalid input for " + operationName + ": " + e.getMessage());
      return false;
    }
  }

  private boolean validateConditionInput(UUID objectId, String eventId, String conditionType, int groupIndex, String operationName) {
    if (!validateEventInput(objectId, eventId, operationName)) return false;
    try {
      Objects.requireNonNull(conditionType, String.format("Condition type cannot be null for %s", operationName));
      if (groupIndex < 0) throw new IllegalArgumentException("Group index cannot be negative");
      return true;
    } catch (NullPointerException | IllegalArgumentException e) {
      LOG.warn("Validation failed for {}: {}", operationName, e.getMessage());
      notifier.notifyErrorOccurred("Invalid input for " + operationName + ": " + e.getMessage());
      return false;
    }
  }

  private boolean validateOutcomeInput(UUID objectId, String eventId, String outcomeType, String operationName) {
    if (!validateEventInput(objectId, eventId, operationName)) return false;
    try {
      Objects.requireNonNull(outcomeType, String.format("Outcome type cannot be null for %s", operationName));
      return true;
    } catch (NullPointerException e) {
      LOG.warn("Validation failed for {}: {}", operationName, e.getMessage());
      notifier.notifyErrorOccurred("Invalid input for " + operationName + ": " + e.getMessage());
      return false;
    }
  }

  private boolean validateParamInput(UUID objectId, String eventId, String paramName, String operationName) {
    if (!validateEventInput(objectId, eventId, operationName)) return false;
    try {
      Objects.requireNonNull(paramName, String.format("Parameter name cannot be null for %s", operationName));
      return true;
    } catch (NullPointerException e) {
      LOG.warn("Validation failed for {}: {}", operationName, e.getMessage());
      notifier.notifyErrorOccurred("Invalid input for " + operationName + ": " + e.getMessage());
      return false;
    }
  }
}
