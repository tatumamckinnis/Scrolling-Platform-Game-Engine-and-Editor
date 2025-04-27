package oogasalad.editor.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import oogasalad.editor.controller.level.EditorDataAPI;
import oogasalad.editor.model.data.object.EditorObject;
import oogasalad.editor.model.data.object.DynamicVariable;
import oogasalad.editor.model.data.object.event.EditorEvent;
import oogasalad.editor.model.data.object.event.ExecutorData;
import oogasalad.editor.view.EditorViewListener;
import oogasalad.exceptions.EditorSaveException;
import oogasalad.fileparser.records.BlueprintData;

/**
 * Defines the contract for the main editor controller. It acts as a central mediator between the
 * view components and the data layer (represented by {@link EditorDataAPI}). The controller manages
 * application state like the currently selected object and active tool, handles user requests from
 * the view (e.g., placing objects, modifying properties, saving), and notifies registered view
 * listeners about changes in the model or state.
 *
 * @author Tatum McKinnis, Jacob You
 */
public interface EditorController {

  /**
   * Retrieves the EditorDataAPI instance used by this controller.
   *
   * @return The EditorDataAPI instance.
   */
  EditorDataAPI getEditorDataAPI();

  /**
   * Gets the UUID of the currently selected object in the editor.
   *
   * @return The UUID of the selected object, or null if no object is selected.
   */
  UUID getCurrentSelectedObjectId();

  /**
   * Registers a view listener to receive notifications about changes in the editor state or data.
   *
   * @param listener The EditorViewListener to register.
   */
  void registerViewListener(EditorViewListener listener);

  /**
   * Unregisters a previously registered view listener.
   *
   * @param listener The EditorViewListener to unregister.
   */
  void unregisterViewListener(EditorViewListener listener);

  /**
   * Sets the currently active tool in the editor (e.g., "selectionTool", "placementTool").
   *
   * @param toolName The identifier name of the tool to activate.
   */
  void setActiveTool(String toolName);

  /**
   * Notifies the controller and subsequently the listeners that an object has been selected.
   *
   * @param objectId The UUID of the selected object.
   */
  void notifyObjectSelected(UUID objectId);

  /**
   * Notifies the controller and listeners that the current selection should be cleared. Equivalent
   * to calling notifyObjectSelected(null).
   */
  void notifyObjectDeselected();

  /**
   * Notifies listeners that the list of available prefabs has changed.
   */
  void notifyPrefabsChanged();

  /**
   * Notifies listeners that an error has occurred, providing a message.
   *
   * @param errorMessage A description of the error.
   */
  void notifyErrorOccurred(String errorMessage);

  /**
   * Handles a request from the view to place a standard (non-prefab) object onto the canvas.
   *
   * @param objectGroup      The group/type of the object to place.
   * @param objectNamePrefix The prefix for the object's name.
   * @param worldX           The x-coordinate in world space where the object should be placed.
   * @param worldY           The y-coordinate in world space where the object should be placed.
   * @param cellSize         The current grid cell size (may be used for snapping or default sizing).
   */
  void requestObjectPlacement(String objectGroup, String objectNamePrefix, double worldX,
      double worldY, int cellSize);


  /**
   * Handles a request from the view to place an object based on a prefab blueprint onto the canvas.
   *
   * @param prefabData The BlueprintData representing the prefab to place.
   * @param worldX     The x-coordinate in world space where the object should be placed.
   * @param worldY     The y-coordinate in world space where the object should be placed.
   */
  void requestPrefabPlacement(BlueprintData prefabData, double worldX, double worldY);

  /**
   * Handles a request from the view to remove an object from the level.
   *
   * @param objectId The UUID of the object to remove.
   */
  void requestObjectRemoval(UUID objectId);

  /**
   * Handles a request from the view or internal logic to update an existing object's data.
   *
   * @param updatedObject The EditorObject containing the new data. Its ID identifies the object to
   * update.
   */
  void requestObjectUpdate(EditorObject updatedObject);

  /**
   * Handles a request to save the specified EditorObject as a reusable prefab.
   *
   * @param objectToSave The EditorObject to be saved as a prefab.
   */
  void requestSaveAsPrefab(EditorObject objectToSave);

  /**
   * Retrieves the full EditorObject data for a given object ID.
   *
   * @param objectId The UUID of the object to retrieve.
   * @return The EditorObject, or null if not found.
   */
  EditorObject getEditorObject(UUID objectId);

  /**
   * Finds the UUID of the object located at the specified grid coordinates.
   *
   * @param gridX The x-coordinate in grid space.
   * @param gridY The y-coordinate in grid space.
   * @return The UUID of the object at that location, or null if no object is found.
   */
  UUID getObjectIDAt(double gridX, double gridY);

  /**
   * Adds a specific event type (identified by eventId) to the specified object.
   *
   * @param objectId The UUID of the object to modify.
   * @param eventId  The identifier of the event type to add (e.g., "CollisionWithEnemy",
   * "KeyPressA").
   */
  void addEvent(UUID objectId, String eventId);

  /**
   * Removes a specific event type (identified by eventId) from the specified object.
   *
   * @param objectId The UUID of the object to modify.
   * @param eventId  The identifier of the event type to remove.
   */
  void removeEvent(UUID objectId, String eventId);

  /**
   * Retrieves a map of all events currently associated with the specified object.
   *
   * @param objectId The UUID of the object.
   * @return A map where keys are event IDs and values are the corresponding {@link EditorEvent}
   * data. Returns an empty map if the object is not found or has no events.
   */
  Map<String, EditorEvent> getEventsForObject(UUID objectId);

  /**
   * Adds a new, empty condition group (an 'OR' block) to a specific event on an object.
   *
   * @param objectId The UUID of the object.
   * @param eventId  The identifier of the event to modify.
   */
  void addConditionGroup(UUID objectId, String eventId);

  /**
   * Adds a new condition of a specific type to a condition group within an event.
   *
   * @param objectId      The UUID of the object.
   * @param eventId       The identifier of the event.
   * @param groupIndex    The index of the condition group (OR block) to add to.
   * @param conditionType The type identifier of the condition to add (e.g., "IsGrounded",
   * "VariableEquals").
   */
  void addEventCondition(UUID objectId, String eventId, int groupIndex, String conditionType);

  /**
   * Removes a specific condition from a condition group within an event.
   *
   * @param objectId       The UUID of the object.
   * @param eventId        The identifier of the event.
   * @param groupIndex     The index of the condition group.
   * @param conditionIndex The index of the condition within the group to remove.
   */
  void removeEventCondition(UUID objectId, String eventId, int groupIndex, int conditionIndex);

  /**
   * Removes an entire condition group (OR block) from an event.
   *
   * @param objectId   The UUID of the object.
   * @param eventId    The identifier of the event.
   * @param groupIndex The index of the condition group to remove.
   */
  void removeConditionGroup(UUID objectId, String eventId, int groupIndex);

  /**
   * Sets the value of a string parameter for a specific condition within an event.
   *
   * @param objectId       The UUID of the object.
   * @param eventId        The identifier of the event.
   * @param groupIndex     The index of the condition group.
   * @param conditionIndex The index of the condition within the group.
   * @param paramName      The name of the string parameter to set.
   * @param value          The new string value.
   */
  void setEventConditionStringParameter(UUID objectId, String eventId, int groupIndex,
      int conditionIndex, String paramName, String value);

  /**
   * Sets the value of a double parameter for a specific condition within an event.
   *
   * @param objectId       The UUID of the object.
   * @param eventId        The identifier of the event.
   * @param groupIndex     The index of the condition group.
   * @param conditionIndex The index of the condition within the group.
   * @param paramName      The name of the double parameter to set.
   * @param value          The new double value.
   */
  void setEventConditionDoubleParameter(UUID objectId, String eventId, int groupIndex,
      int conditionIndex, String paramName, Double value);

  /**
   * Retrieves the list of condition groups (each group being a list of conditions) for a specific
   * event.
   *
   * @param objectId The UUID of the object.
   * @param eventId  The identifier of the event.
   * @return A list of lists, where each inner list represents a condition group (OR block)
   * containing {@link ExecutorData} for each condition (ANDed within the group). Returns an empty
   * list if not found.
   */
  List<List<ExecutorData>> getEventConditions(UUID objectId, String eventId);

  /**
   * Retrieves a specific condition group from an event.
   *
   * @param objectId   The UUID of the object.
   * @param eventId    The identifier of the event.
   * @param groupIndex The index of the condition group to retrieve.
   * @return A list of {@link ExecutorData} representing the conditions in that group, or an empty
   * list if not found.
   */
  List<ExecutorData> getEventConditionGroup(UUID objectId, String eventId, int groupIndex);

  /**
   * Adds a new outcome of a specific type to an event.
   *
   * @param objectId    The UUID of the object.
   * @param eventId     The identifier of the event.
   * @param outcomeType The type identifier of the outcome to add (e.g., "MoveForward",
   * "DestroySelf").
   */
  void addEventOutcome(UUID objectId, String eventId, String outcomeType);

  /**
   * Removes a specific outcome from an event based on its index.
   *
   * @param objectId     The UUID of the object.
   * @param eventId      The identifier of the event.
   * @param outcomeIndex The index of the outcome to remove.
   */
  void removeEventOutcome(UUID objectId, String eventId, int outcomeIndex);

  /**
   * Sets the value of a string parameter for a specific outcome within an event.
   *
   * @param objectId     The UUID of the object.
   * @param eventId      The identifier of the event.
   * @param outcomeIndex The index of the outcome.
   * @param paramName    The name of the string parameter to set.
   * @param value        The new string value.
   */
  void setEventOutcomeStringParameter(UUID objectId, String eventId, int outcomeIndex,
      String paramName, String value);

  /**
   * Sets the value of a double parameter for a specific outcome within an event.
   *
   * @param objectId     The UUID of the object.
   * @param eventId      The identifier of the event.
   * @param outcomeIndex The index of the outcome.
   * @param paramName    The name of the double parameter to set.
   * @param value        The new double value.
   */
  void setEventOutcomeDoubleParameter(UUID objectId, String eventId, int outcomeIndex,
      String paramName, Double value);

  /**
   * Retrieves the list of all outcomes associated with a specific event.
   *
   * @param objectId The UUID of the object.
   * @param eventId  The identifier of the event.
   * @return A list of {@link ExecutorData} representing the outcomes, or an empty list if not
   * found.
   */
  List<ExecutorData> getEventOutcomes(UUID objectId, String eventId);

  /**
   * Retrieves the data for a specific outcome within an event.
   *
   * @param objectId     The UUID of the object.
   * @param eventId      The identifier of the event.
   * @param outcomeIndex The index of the outcome to retrieve.
   * @return The {@link ExecutorData} for the outcome, or null if not found.
   */
  ExecutorData getEventOutcomeData(UUID objectId, String eventId, int outcomeIndex);

  /**
   * Adds a global dynamic variable to the editor's context.
   * Note: This manages variables separate from per-object parameters.
   *
   * @param variable The {@link DynamicVariable} to add.
   */
  void addDynamicVariable(DynamicVariable variable);

  /**
   * Retrieves a list of all available global dynamic variables.
   * Note: This manages variables separate from per-object parameters.
   *
   * @param objectId The UUID of the currently selected object (context, may not be used for global
   * vars).
   * @return A list of {@link DynamicVariable} objects.
   */
  List<DynamicVariable> getAvailableDynamicVariables(UUID objectId);


  /**
   * Sets or updates a custom string parameter for a specific object.
   *
   * @param objectId The UUID of the object to modify.
   * @param key The non-empty key for the parameter.
   * @param value The string value to set.
   */
  void setObjectStringParameter(UUID objectId, String key, String value);

  /**
   * Sets or updates a custom double parameter for a specific object.
   *
   * @param objectId The UUID of the object to modify.
   * @param key The non-empty key for the parameter.
   * @param value The double value to set.
   */
  void setObjectDoubleParameter(UUID objectId, String key, Double value);

  /**
   * Removes a custom parameter (either string or double) with the specified key from an object.
   *
   * @param objectId The UUID of the object to modify.
   * @param key The non-empty key of the parameter to remove.
   */
  void removeObjectParameter(UUID objectId, String key);

  /**
   * Retrieves a map of all custom string parameters associated with the specified object.
   *
   * @param objectId The UUID of the object.
   * @return An unmodifiable map of string key-value pairs, or an empty map if none exist or the object is not found.
   */
  Map<String, String> getObjectStringParameters(UUID objectId);

  /**
   * Retrieves a map of all custom double parameters associated with the specified object.
   *
   * @param objectId The UUID of the object.
   * @return An unmodifiable map of double key-value pairs, or an empty map if none exist or the object is not found.
   */
  Map<String, Double> getObjectDoubleParameters(UUID objectId);


  /**
   * Saves the current level data to the specified file path.
   *
   * @param fileName The path and name of the file to save to.
   * @throws EditorSaveException If an error occurs during saving.
   */
  void saveLevelData(String fileName) throws EditorSaveException;

  /**
   * Sets the grid cell size used by the editor view.
   *
   * @param cellSize The size of grid cells in pixels (must be positive).
   */
  void setCellSize(int cellSize);

  /**
   * Gets the current grid cell size.
   *
   * @return The cell size in pixels.
   */
  int getCellSize();

  /**
   * Sets whether object placement and movement should snap to the grid.
   *
   * @param doSnap True to enable snapping, false otherwise.
   */
  void setSnapToGrid(boolean doSnap);

  /**
   * Checks if snap-to-grid is currently enabled.
   *
   * @return True if snapping is enabled, false otherwise.
   */
  boolean isSnapToGrid();
}