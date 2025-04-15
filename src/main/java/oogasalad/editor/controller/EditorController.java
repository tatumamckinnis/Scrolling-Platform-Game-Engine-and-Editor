package oogasalad.editor.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import oogasalad.editor.model.data.EditorObject;
import oogasalad.editor.model.data.object.DynamicVariable;
import oogasalad.editor.model.data.object.event.EditorEvent;
import oogasalad.editor.model.data.object.event.ExecutorData;
import oogasalad.editor.view.EditorViewListener;

/**
 * Defines the contract between the Editor View components and the underlying Controller logic.
 * Includes methods for actions, data fetching, and listener management for view updates (Observer Pattern).
 * Methods related to event management now use indices and String types consistent with ExecutorData.
 *
 * @author Tatum McKinnis, Jacob You
 */
public interface EditorController {

  /**
   * Requests the placement of a new game object within the editor grid.
   * @param objectGroup Group name for the new object.
   * @param objectNamePrefix Prefix for the new object's name.
   * @param worldX X-coordinate for placement in world units.
   * @param worldY Y-coordinate for placement in world units.
   * @param cellSize Size reference for the object, typically the grid cell size.
   */
  void requestObjectPlacement(String objectGroup, String objectNamePrefix, double worldX,
      double worldY, int cellSize);

  /**
   * Notifies the controller that an object has been selected in the view.
   * This updates the controller's internal state and notifies listeners.
   * @param objectId UUID of the selected object, or null if selection is cleared.
   */
  void notifyObjectSelected(UUID objectId);

  /**
   * Gets the {@link EditorDataAPI} instance used by this controller, providing access to underlying data managers.
   * @return The EditorDataAPI instance.
   */
  EditorDataAPI getEditorDataAPI();

  /**
   * Notifies the controller that object selection has been cleared in the view.
   * Equivalent to calling {@code notifyObjectSelected(null)}.
   */
  void notifyObjectDeselected();


  /**
   * Requests the removal of the specified game object from the editor model.
   * @param objectId UUID of the object to remove.
   */
  void requestObjectRemoval(UUID objectId);

  /**
   * Requests an update to an existing game object using the provided data object.
   * The object must contain a valid UUID matching an existing object.
   * @param updatedObject The object data containing updates.
   */
  void requestObjectUpdate(EditorObject updatedObject);

  /**
   * Gets the full {@link EditorObject} data associated with the given UUID.
   * @param objectId UUID of the object to retrieve.
   * @return The {@link EditorObject}, or null if not found or an error occurs.
   */
  EditorObject getEditorObject(UUID objectId);

  /**
   * Retrieves the UUID of the topmost object located at the specified grid coordinates,
   * considering layer priority if objects overlap.
   * @param x X-coordinate in world units.
   * @param y Y-coordinate in world units.
   * @return The UUID of the object at the location, or null if no object exists there.
   */
  UUID getObjectIDAt(double x, double y);


  /**
   * Adds a new event definition (identified by eventId) to the specified object.
   * @param objectId UUID of the target object.
   * @param eventId String identifier for the new event.
   */
  void addEvent(UUID objectId, String eventId);

  /**
   * Removes an event definition (identified by eventId) from the specified object.
   * @param objectId UUID of the target object.
   * @param eventId String identifier of the event to remove.
   */
  void removeEvent(UUID objectId, String eventId);

  /**
   * Gets all events associated with the specified object ID.
   * @param objectId UUID of the target object.
   * @return A Map where keys are event IDs (String) and values are {@link EditorEvent} objects. Returns an empty map if none found or on error.
   */
  Map<String, EditorEvent> getEventsForObject(UUID objectId);

  /**
   * Adds an empty condition group (representing an OR block) to the specified event.
   * @param objectId UUID of the target object.
   * @param eventId String identifier of the target event.
   */
  void addConditionGroup(UUID objectId, String eventId);

  /**
   * Adds a condition of a specific type (identified by a string) to a specified group within an event.
   * Conditions within a group are typically evaluated with AND logic.
   * @param objectId UUID of the target object.
   * @param eventId String identifier of the target event.
   * @param groupIndex Index of the condition group (OR block) to add the condition to.
   * @param conditionType String identifier of the condition type (e.g., "KEY_PRESSED").
   */
  void addEventCondition(UUID objectId, String eventId, int groupIndex, String conditionType);

  /**
   * Removes a condition at a specific index within a specific group of an event.
   * @param objectId UUID of the target object.
   * @param eventId String identifier of the target event.
   * @param groupIndex Index of the condition group containing the condition.
   * @param conditionIndex Index of the condition within the group to remove.
   */
  void removeEventCondition(UUID objectId, String eventId, int groupIndex, int conditionIndex);

  /**
   * Removes an entire condition group (OR block) from an event at the specified index.
   * @param objectId UUID of the target object.
   * @param eventId String identifier of the target event.
   * @param groupIndex Index of the condition group to remove.
   */
  void removeConditionGroup(UUID objectId, String eventId, int groupIndex);

  /**
   * Sets a String-valued parameter for a specific condition within an event.
   * @param objectId UUID of the target object.
   * @param eventId String identifier of the target event.
   * @param groupIndex Index of the condition group.
   * @param conditionIndex Index of the condition within the group.
   * @param paramName Name of the parameter to set (e.g., "key").
   * @param value The String value for the parameter.
   */
  void setEventConditionStringParameter(UUID objectId, String eventId, int groupIndex, int conditionIndex, String paramName, String value);

  /**
   * Sets a Double-valued parameter for a specific condition within an event.
   * @param objectId UUID of the target object.
   * @param eventId String identifier of the target event.
   * @param groupIndex Index of the condition group.
   * @param conditionIndex Index of the condition within the group.
   * @param paramName Name of the parameter to set (e.g., "duration").
   * @param value The Double value for the parameter.
   */
  void setEventConditionDoubleParameter(UUID objectId, String eventId, int groupIndex, int conditionIndex, String paramName, Double value);

  /**
   * Gets all condition groups and their conditions for a specific event.
   * The outer list represents OR blocks, and the inner list represents AND conditions within a block.
   * @param objectId UUID of the target object.
   * @param eventId String identifier of the target event.
   * @return A List of condition groups, where each group is a List of {@link ExecutorData}. Returns an empty list on failure or if not found.
   */
  List<List<ExecutorData>> getEventConditions(UUID objectId, String eventId);

  /**
   * Gets a specific condition group (list of AND conditions) from an event.
   * @param objectId UUID of the target object.
   * @param eventId String identifier of the target event.
   * @param groupIndex Index of the desired condition group.
   * @return A List of {@link ExecutorData} for the specified group, or null if the group index is invalid or an error occurs.
   */
  List<ExecutorData> getEventConditionGroup(UUID objectId, String eventId, int groupIndex);

  /**
   * Adds an outcome of a specific type (identified by a string) to an event.
   * Outcomes are typically executed sequentially if all conditions pass.
   * @param objectId UUID of the target object.
   * @param eventId String identifier of the target event.
   * @param outcomeType String identifier of the outcome type (e.g., "MOVE_LEFT").
   */
  void addEventOutcome(UUID objectId, String eventId, String outcomeType);

  /**
   * Removes an outcome at a specific index from an event's outcome list.
   * @param objectId UUID of the target object.
   * @param eventId String identifier of the target event.
   * @param outcomeIndex Index of the outcome to remove.
   */
  void removeEventOutcome(UUID objectId, String eventId, int outcomeIndex);

  /**
   * Sets a String-valued parameter for a specific outcome within an event.
   * @param objectId UUID of the target object.
   * @param eventId String identifier of the target event.
   * @param outcomeIndex Index of the outcome within the event's list.
   * @param paramName Name of the parameter to set (e.g., "targetVariable").
   * @param value The String value for the parameter.
   */
  void setEventOutcomeStringParameter(UUID objectId, String eventId, int outcomeIndex, String paramName, String value);

  /**
   * Sets a Double-valued parameter for a specific outcome within an event.
   * @param objectId UUID of the target object.
   * @param eventId String identifier of the target event.
   * @param outcomeIndex Index of the outcome within the event's list.
   * @param paramName Name of the parameter to set (e.g., "speed").
   * @param value The Double value for the parameter.
   */
  void setEventOutcomeDoubleParameter(UUID objectId, String eventId, int outcomeIndex, String paramName, Double value);

  /**
   * Gets all outcomes for a specific event.
   * @param objectId UUID of the target object.
   * @param eventId String identifier of the target event.
   * @return A List of {@link ExecutorData} representing the outcomes. Returns an empty list on failure or if not found.
   */
  List<ExecutorData> getEventOutcomes(UUID objectId, String eventId);

  /**
   * Gets the data ({@link ExecutorData}) for a specific outcome at a given index within an event.
   * @param objectId UUID of the target object.
   * @param eventId String identifier of the target event.
   * @param outcomeIndex Index of the desired outcome.
   * @return The {@link ExecutorData} for the outcome, or null if the index is invalid or an error occurs.
   */
  ExecutorData getEventOutcomeData(UUID objectId, String eventId, int outcomeIndex);


  /**
   * Adds a new dynamic variable to the global scope (or potentially object scope later).
   * @param variable The {@link DynamicVariable} object to add.
   */
  void addDynamicVariable(DynamicVariable variable);

  /**
   * Gets all available dynamic variables, potentially filtered by context (e.g., global or object-specific).
   * @param objectId UUID of the context object (may be null for global scope, interpretation depends on implementation).
   * @return List of available {@link DynamicVariable} objects. Returns an empty list on failure.
   */
  List<DynamicVariable> getAvailableDynamicVariables(UUID objectId);


  /**
   * Registers a listener (typically a View component) to receive notifications about model/state changes.
   * @param listener The listener implementing {@link EditorViewListener} to register.
   */
  void registerViewListener(EditorViewListener listener);

  /**
   * Unregisters a previously registered listener.
   * @param listener The listener to unregister.
   */
  void unregisterViewListener(EditorViewListener listener);
}