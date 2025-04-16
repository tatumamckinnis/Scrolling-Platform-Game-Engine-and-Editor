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
 * Provides methods for managing editor objects, selection, events, and listeners.
 * Includes methods for handling prefab (BlueprintData) placement and saving.
 *
 * @author Tatum McKinnis, Jacob You
 */
public interface EditorController {

  void registerViewListener(EditorViewListener listener);
  void unregisterViewListener(EditorViewListener listener);
  EditorDataAPI getEditorDataAPI();

  // --- Object Lifecycle ---
  void requestObjectPlacement(String objectGroup, String objectNamePrefix, double worldX, double worldY, int cellSize);
  void requestObjectRemoval(UUID objectId);
  void requestObjectUpdate(EditorObject updatedObject);
  void requestPrefabPlacement(BlueprintData prefabData, double worldX, double worldY);
  void requestSaveAsPrefab(EditorObject objectToSave);


  // --- Selection ---
  void notifyObjectSelected(UUID objectId);
  void notifyObjectDeselected();
  EditorObject getEditorObject(UUID objectId);
  UUID getObjectIDAt(double gridX, double gridY);
  UUID getCurrentSelectedObjectId(); // Added getter for selected ID

  // --- Tool Management ---
  void setActiveTool(String toolName); // Added method to change active tool

  // --- Event Handling ---
  void addEvent(UUID objectId, String eventId);
  void removeEvent(UUID objectId, String eventId);
  Map<String, EditorEvent> getEventsForObject(UUID objectId);
  void addConditionGroup(UUID objectId, String eventId);
  void addEventCondition(UUID objectId, String eventId, int groupIndex, String conditionType);
  void removeEventCondition(UUID objectId, String eventId, int groupIndex, int conditionIndex);
  void removeConditionGroup(UUID objectId, String eventId, int groupIndex);
  void setEventConditionStringParameter(UUID objectId, String eventId, int groupIndex, int conditionIndex, String paramName, String value);
  void setEventConditionDoubleParameter(UUID objectId, String eventId, int groupIndex, int conditionIndex, String paramName, Double value);
  List<List<ExecutorData>> getEventConditions(UUID objectId, String eventId);
  List<ExecutorData> getEventConditionGroup(UUID objectId, String eventId, int groupIndex);
  void addEventOutcome(UUID objectId, String eventId, String outcomeType);
  void removeEventOutcome(UUID objectId, String eventId, int outcomeIndex);
  void setEventOutcomeStringParameter(UUID objectId, String eventId, int outcomeIndex, String paramName, String value);
  void setEventOutcomeDoubleParameter(UUID objectId, String eventId, int outcomeIndex, String paramName, Double value);
  List<ExecutorData> getEventOutcomes(UUID objectId, String eventId);
  ExecutorData getEventOutcomeData(UUID objectId, String eventId, int outcomeIndex);

  // --- Dynamic Variables ---
  void addDynamicVariable(DynamicVariable variable);
  List<DynamicVariable> getAvailableDynamicVariables(UUID objectId);

  // --- Notifications ---
  void notifyErrorOccurred(String errorMessage);
  void notifyPrefabsChanged(); // Added method to signal prefab list update
}