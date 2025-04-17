package oogasalad.editor.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import oogasalad.editor.model.data.EditorObject;
import oogasalad.editor.model.data.object.DynamicVariable;
import oogasalad.editor.model.data.object.event.EditorEvent;
import oogasalad.editor.model.data.object.event.ExecutorData;
import oogasalad.editor.view.EditorViewListener;
import oogasalad.fileparser.records.BlueprintData;

/**
 * Defines the primary interface for interaction between the Editor View and the Editor Model/Data.
 * Provides methods for managing editor objects, selection, events, and listeners. Includes methods
 * for handling prefab (BlueprintData) placement and saving.
 *
 * @author Tatum McKinnis, Jacob You
 */
public interface EditorController {

  /**
   * Registers a view listener to receive updates from the controller.
   */
  void registerViewListener(EditorViewListener listener);

  /**
   * Unregisters a previously registered view listener.
   */
  void unregisterViewListener(EditorViewListener listener);

  /**
   * Returns the data API that provides access to the current state of the editor data.
   */
  EditorDataAPI getEditorDataAPI();

  // --- Object Lifecycle ---

  /**
   * Requests placement of a new object in the world at the specified coordinates.
   */
  void requestObjectPlacement(String objectGroup, String objectNamePrefix, double worldX,
      double worldY, int cellSize);

  /**
   * Requests removal of the object with the given UUID.
   */
  void requestObjectRemoval(UUID objectId);

  /**
   * Requests an update to an existing object’s properties.
   */
  void requestObjectUpdate(EditorObject updatedObject);

  /**
   * Requests placement of a prefab (group of objects) at the specified world coordinates.
   */
  void requestPrefabPlacement(BlueprintData prefabData, double worldX, double worldY);

  /**
   * Saves the specified object as a new prefab.
   */
  void requestSaveAsPrefab(EditorObject objectToSave);

  // --- Selection ---

  /**
   * Notifies the controller that a specific object was selected by the user.
   */
  void notifyObjectSelected(UUID objectId);

  /**
   * Notifies the controller that the user deselected the currently selected object.
   */
  void notifyObjectDeselected();

  /**
   * Retrieves the object corresponding to the given UUID.
   */
  EditorObject getEditorObject(UUID objectId);

  /**
   * Returns the ID of the object located at the specified grid coordinates, if any.
   */
  UUID getObjectIDAt(double gridX, double gridY);

  /**
   * Returns the UUID of the currently selected object, if any.
   */
  UUID getCurrentSelectedObjectId();

  // --- Tool Management ---

  /**
   * Sets the currently active tool by name (e.g., "select", "place", etc.).
   */
  void setActiveTool(String toolName);

  // --- Event Handling ---

  /**
   * Adds an event with the given ID to the specified object.
   */
  void addEvent(UUID objectId, String eventId);

  /**
   * Removes the specified event from the object.
   */
  void removeEvent(UUID objectId, String eventId);

  /**
   * Returns the full map of event IDs to event data for the specified object.
   */
  Map<String, EditorEvent> getEventsForObject(UUID objectId);

  /**
   * Adds a new condition group to an event (supports multiple AND/OR condition sets).
   */
  void addConditionGroup(UUID objectId, String eventId);

  /**
   * Adds a condition to a specific group in an event's condition structure.
   */
  void addEventCondition(UUID objectId, String eventId, int groupIndex, String conditionType);

  /**
   * Removes a condition from a specific group in an event.
   */
  void removeEventCondition(UUID objectId, String eventId, int groupIndex, int conditionIndex);

  /**
   * Removes a full group of conditions from an event.
   */
  void removeConditionGroup(UUID objectId, String eventId, int groupIndex);

  /**
   * Sets a string parameter for a specific condition within an event.
   */
  void setEventConditionStringParameter(UUID objectId, String eventId, int groupIndex,
      int conditionIndex, String paramName, String value);

  /**
   * Sets a numeric parameter for a specific condition within an event.
   */
  void setEventConditionDoubleParameter(UUID objectId, String eventId, int groupIndex,
      int conditionIndex, String paramName, Double value);

  /**
   * Returns all condition groups and their associated conditions for the given event.
   */
  List<List<ExecutorData>> getEventConditions(UUID objectId, String eventId);

  /**
   * Returns the condition group at the specified index for the given event.
   */
  List<ExecutorData> getEventConditionGroup(UUID objectId, String eventId, int groupIndex);

  /**
   * Adds an outcome (response/action) to the specified event.
   */
  void addEventOutcome(UUID objectId, String eventId, String outcomeType);

  /**
   * Removes an outcome at the given index from the event.
   */
  void removeEventOutcome(UUID objectId, String eventId, int outcomeIndex);

  /**
   * Sets a string parameter for a specific outcome in an event.
   */
  void setEventOutcomeStringParameter(UUID objectId, String eventId, int outcomeIndex,
      String paramName, String value);

  /**
   * Sets a numeric parameter for a specific outcome in an event.
   */
  void setEventOutcomeDoubleParameter(UUID objectId, String eventId, int outcomeIndex,
      String paramName, Double value);

  /**
   * Returns a list of all outcomes associated with the specified event.
   */
  List<ExecutorData> getEventOutcomes(UUID objectId, String eventId);

  /**
   * Retrieves data for a specific outcome by index from the event.
   */
  ExecutorData getEventOutcomeData(UUID objectId, String eventId, int outcomeIndex);

  // --- Dynamic Variables ---

  /**
   * Adds a new dynamic variable to the editor’s shared variable list.
   */
  void addDynamicVariable(DynamicVariable variable);

  /**
   * Returns a list of dynamic variables available to the specified object.
   */
  List<DynamicVariable> getAvailableDynamicVariables(UUID objectId);

  // --- Notifications ---

  /**
   * Notifies the view that an error occurred and provides a message for display.
   */
  void notifyErrorOccurred(String errorMessage);

  /**
   * Notifies the view that the available prefab list has changed and should be refreshed.
   */
  void notifyPrefabsChanged();
}
