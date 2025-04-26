package oogasalad.editor.controller;

import java.util.Collections;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import oogasalad.editor.controller.asset.EditorPrefabManager;
import oogasalad.editor.controller.level.EditorDataAPI;
import oogasalad.editor.controller.listeners.EditorListenerNotifier;
import oogasalad.editor.controller.object.EditorEventHandler;
import oogasalad.editor.model.data.object.EditorObject;
import oogasalad.editor.model.data.object.DynamicVariable;
import oogasalad.editor.model.data.object.DynamicVariableContainer;
import oogasalad.editor.model.data.object.event.EditorEvent;
import oogasalad.editor.model.data.object.event.ExecutorData;
import oogasalad.editor.view.EditorViewListener;
import oogasalad.exceptions.EditorSaveException;
import oogasalad.fileparser.records.BlueprintData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Concrete implementation of the {@link EditorController} interface. Acts as a mediator between the editor
 * view and the editor data backend ({@link EditorDataAPI}). Manages selection state, active tool,
 * listeners, and delegates specific actions (object placement, prefab management, event handling,
 * object queries, parameter management) to specialized handler classes or directly to the data API.
 *
 * @author Tatum McKinnis
 */
public class ConcreteEditorController implements EditorController {

  private static final Logger LOG = LogManager.getLogger(ConcreteEditorController.class);

  private final EditorDataAPI editorDataAPI;
  private UUID currentSelectedObjectId;
  private String activeToolName = "selectionTool";

  private final EditorListenerNotifier listenerNotifier;
  private final EditorObjectPlacementHandler objectPlacementHandler;
  private final EditorPrefabManager prefabManager;
  private final EditorEventHandler eventHandler;
  private final EditorObjectQueryHandler objectQueryHandler;

  private int cellSize = 32;
  private boolean snapToGrid = true;
  private int editorWidth = 1200;
  private int editorHeight = 800;

  /**
   * Constructs a ConcreteEditorController, initializing its data API, listener notifier,
   * and various helper handlers for specific editor functionalities.
   *
   * @param editorDataAPI The central API for accessing and modifying editor data. Cannot be null.
   * @param listenerNotifier The notifier responsible for broadcasting events to registered view listeners. Cannot be null.
   */
  public ConcreteEditorController(EditorDataAPI editorDataAPI, EditorListenerNotifier listenerNotifier) {
    this.editorDataAPI = Objects.requireNonNull(editorDataAPI, "EditorDataAPI cannot be null.");
    this.listenerNotifier = Objects.requireNonNull(listenerNotifier, "ListenerNotifier cannot be null.");
    this.objectPlacementHandler = new EditorObjectPlacementHandler(editorDataAPI, listenerNotifier);
    this.prefabManager = new EditorPrefabManager(editorDataAPI, listenerNotifier);
    this.eventHandler = new EditorEventHandler(editorDataAPI, listenerNotifier);
    this.objectQueryHandler = new EditorObjectQueryHandler(editorDataAPI);


    LOG.info("ConcreteEditorController initialized with helper classes.");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public EditorDataAPI getEditorDataAPI() {
    return editorDataAPI;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public UUID getCurrentSelectedObjectId() {
    return currentSelectedObjectId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void registerViewListener(EditorViewListener listener) {
    listenerNotifier.registerViewListener(listener);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void unregisterViewListener(EditorViewListener listener) {
    listenerNotifier.unregisterViewListener(listener);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setActiveTool(String toolName) {
    if (toolName != null && !toolName.equals(this.activeToolName)) {
      this.activeToolName = toolName;
      LOG.info("Controller: Active tool changed to {}", toolName);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void notifyObjectSelected(UUID objectId) {
    if (!Objects.equals(this.currentSelectedObjectId, objectId)) {
      this.currentSelectedObjectId = objectId;
      LOG.info("Controller state updated: Object selected: {}", objectId);
      listenerNotifier.notifySelectionChanged(this.currentSelectedObjectId);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void notifyObjectDeselected() {
    notifyObjectSelected(null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void notifyPrefabsChanged() {
    listenerNotifier.notifyPrefabsChanged();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void notifyErrorOccurred(String errorMessage) {
    listenerNotifier.notifyErrorOccurred(errorMessage);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void requestObjectPlacement(String objectGroup, String objectNamePrefix, double worldX,
      double worldY, int cellSize) {
    UUID newObjectId = objectPlacementHandler.placeStandardObject(objectGroup, objectNamePrefix, worldX, worldY, cellSize);
    if (newObjectId != null) {
      notifyObjectSelected(newObjectId);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void requestPrefabPlacement(BlueprintData prefabData, double worldX, double worldY) {
    UUID newObjectId = objectPlacementHandler.placePrefab(prefabData, worldX, worldY);
    if (newObjectId != null) {
      notifyObjectSelected(newObjectId);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void requestObjectRemoval(UUID objectId) {
    Objects.requireNonNull(objectId, "Object ID cannot be null for removal request");
    LOG.info("Processing object removal request: ID={}", objectId);
    try {
      boolean removed = editorDataAPI.removeEditorObject(objectId);
      if (removed) {
        listenerNotifier.notifyObjectRemoved(objectId);
        if (objectId.equals(currentSelectedObjectId)) {
          notifyObjectSelected(null);
        }
        LOG.debug("Successfully processed removal for object {}", objectId);
      } else {
        LOG.warn("Removal request processed but object {} was not found or not removed by backend.", objectId);
        listenerNotifier.notifyErrorOccurred("Object with ID " + objectId + " could not be removed (not found).");
      }
    } catch (Exception e) {
      LOG.error("Error during object removal for ID {}: {}", objectId, e.getMessage(), e);
      listenerNotifier.notifyErrorOccurred("Failed to remove object: " + e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void requestObjectUpdate(EditorObject updatedObject) {
    Objects.requireNonNull(updatedObject, "Updated object cannot be null");
    UUID objectId = updatedObject.getId();
    Objects.requireNonNull(objectId, "Object ID cannot be null for update request");
    LOG.info("Processing object update request: ID={}", objectId);
    try {
      boolean updated = editorDataAPI.updateEditorObject(updatedObject);
      if (updated) {
        LOG.debug("Successfully processed update for object {}", objectId);
        listenerNotifier.notifyObjectUpdated(objectId);
      } else {
        LOG.warn("Update request processed but object {} was not found or not updated by backend.", objectId);
        listenerNotifier.notifyErrorOccurred("Object with ID " + objectId + " could not be updated (not found).");
      }
    } catch (Exception e) {
      LOG.error("Error during object update for ID {}: {}", objectId, e.getMessage(), e);
      listenerNotifier.notifyErrorOccurred("Failed to update object: " + e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void requestSaveAsPrefab(EditorObject objectToSave) {
    prefabManager.saveAsPrefab(objectToSave);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public EditorObject getEditorObject(UUID objectId) {
    EditorObject obj = objectQueryHandler.getEditorObject(objectId);
    if (obj == null && objectId != null) {
      listenerNotifier.notifyErrorOccurred("Failed to get object data for ID: " + objectId);
    }
    return obj;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public UUID getObjectIDAt(double gridX, double gridY) {
    UUID foundId = objectQueryHandler.getObjectIDAt(gridX, gridY);
    return foundId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addEvent(UUID objectId, String eventId) {
    eventHandler.addEvent(objectId, eventId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeEvent(UUID objectId, String eventId) {
    eventHandler.removeEvent(objectId, eventId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, EditorEvent> getEventsForObject(UUID objectId) {
    return eventHandler.getEventsForObject(objectId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addConditionGroup(UUID objectId, String eventId) {
    eventHandler.addConditionGroup(objectId, eventId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addEventCondition(UUID objectId, String eventId, int groupIndex, String conditionType) {
    eventHandler.addEventCondition(objectId, eventId, groupIndex, conditionType);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeEventCondition(UUID objectId, String eventId, int groupIndex, int conditionIndex) {
    eventHandler.removeEventCondition(objectId, eventId, groupIndex, conditionIndex);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeConditionGroup(UUID objectId, String eventId, int groupIndex) {
    eventHandler.removeConditionGroup(objectId, eventId, groupIndex);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setEventConditionStringParameter(UUID objectId, String eventId, int groupIndex, int conditionIndex, String paramName, String value) {
    eventHandler.setEventConditionStringParameter(objectId, eventId, groupIndex, conditionIndex, paramName, value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setEventConditionDoubleParameter(UUID objectId, String eventId, int groupIndex, int conditionIndex, String paramName, Double value) {
    eventHandler.setEventConditionDoubleParameter(objectId, eventId, groupIndex, conditionIndex, paramName, value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<List<ExecutorData>> getEventConditions(UUID objectId, String eventId) {
    return eventHandler.getEventConditions(objectId, eventId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExecutorData> getEventConditionGroup(UUID objectId, String eventId, int groupIndex) {
    return eventHandler.getEventConditionGroup(objectId, eventId, groupIndex);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addEventOutcome(UUID objectId, String eventId, String outcomeType) {
    eventHandler.addEventOutcome(objectId, eventId, outcomeType);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeEventOutcome(UUID objectId, String eventId, int outcomeIndex) {
    eventHandler.removeEventOutcome(objectId, eventId, outcomeIndex);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setEventOutcomeStringParameter(UUID objectId, String eventId, int outcomeIndex, String paramName, String value) {
    eventHandler.setEventOutcomeStringParameter(objectId, eventId, outcomeIndex, paramName, value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setEventOutcomeDoubleParameter(UUID objectId, String eventId, int outcomeIndex, String paramName, Double value) {
    eventHandler.setEventOutcomeDoubleParameter(objectId, eventId, outcomeIndex, paramName, value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExecutorData> getEventOutcomes(UUID objectId, String eventId) {
    return eventHandler.getEventOutcomes(objectId, eventId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ExecutorData getEventOutcomeData(UUID objectId, String eventId, int outcomeIndex) {
    return eventHandler.getEventOutcomeData(objectId, eventId, outcomeIndex);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addDynamicVariable(DynamicVariable variable) {
    Objects.requireNonNull(variable, "DynamicVariable cannot be null");
    LOG.debug("Controller adding global dynamic variable '{}'", variable.getName());
    try {
      DynamicVariableContainer container = editorDataAPI.getDynamicVariableContainer();
      if (container != null) {
        container.addVariable(variable);
        listenerNotifier.notifyDynamicVariablesChanged();
      } else {
        LOG.error("DynamicVariableContainer is null in EditorDataAPI. Cannot add variable.");
        listenerNotifier.notifyErrorOccurred("Internal error: Cannot access variable storage.");
      }
    } catch (IllegalArgumentException e) {
      LOG.warn("Failed to add dynamic variable '{}' due to invalid input or duplicate: {}",
          variable.getName(), e.getMessage());
      listenerNotifier.notifyErrorOccurred("Failed to add variable: " + e.getMessage());
    } catch (Exception e) {
      LOG.error("Unexpected error adding dynamic variable '{}': {}", variable.getName(), e.getMessage(), e);
      listenerNotifier.notifyErrorOccurred("Failed to add variable: " + e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<DynamicVariable> getAvailableDynamicVariables(UUID objectId) {
    LOG.trace("Retrieving available global dynamic variables (context objectId: {})", objectId);
    try {
      DynamicVariableContainer container = editorDataAPI.getDynamicVariableContainer();
      if (container != null) {
        Collection<DynamicVariable> varsCollection = container.getAllVariables();
        return (varsCollection != null) ? new ArrayList<>(varsCollection) : Collections.emptyList();
      } else {
        LOG.warn("DynamicVariableContainer is null in EditorDataAPI.");
        return Collections.emptyList();
      }
    } catch (Exception e) {
      LOG.error("Failed to retrieve dynamic variables: {}", e.getMessage(), e);
      listenerNotifier.notifyErrorOccurred("Failed to retrieve variable list: " + e.getMessage());
      return Collections.emptyList();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setObjectStringParameter(UUID objectId, String key, String value) {
    if (objectId == null || key == null || key.trim().isEmpty()) {
      LOG.warn("Cannot set string parameter with null objectId or null/empty key.");
      return;
    }
    try {
      editorDataAPI.getIdentityDataAPI().setStringParameter(objectId, key, value);
      listenerNotifier.notifyObjectUpdated(objectId); // Notify UI to refresh
    } catch (Exception e) {
      LOG.error("Error setting string parameter '{}' for object {}: {}", key, objectId, e.getMessage(), e);
      listenerNotifier.notifyErrorOccurred("Failed to set string parameter: " + e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setObjectDoubleParameter(UUID objectId, String key, Double value) {
    if (objectId == null || key == null || key.trim().isEmpty()) {
      LOG.warn("Cannot set double parameter with null objectId or null/empty key.");
      return;
    }
    try {
      editorDataAPI.getIdentityDataAPI().setDoubleParameter(objectId, key, value);
      listenerNotifier.notifyObjectUpdated(objectId); // Notify UI to refresh
    } catch (Exception e) {
      LOG.error("Error setting double parameter '{}' for object {}: {}", key, objectId, e.getMessage(), e);
      listenerNotifier.notifyErrorOccurred("Failed to set double parameter: " + e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeObjectParameter(UUID objectId, String key) {
    if (objectId == null || key == null || key.trim().isEmpty()) {
      LOG.warn("Cannot remove parameter with null objectId or null/empty key.");
      return;
    }
    try {
      editorDataAPI.getIdentityDataAPI().removeParameter(objectId, key);
      listenerNotifier.notifyObjectUpdated(objectId); // Notify UI to refresh
    } catch (Exception e) {
      LOG.error("Error removing parameter '{}' for object {}: {}", key, objectId, e.getMessage(), e);
      listenerNotifier.notifyErrorOccurred("Failed to remove parameter: " + e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, String> getObjectStringParameters(UUID objectId) {
    if (objectId == null) return Collections.emptyMap();
    try {
      return editorDataAPI.getIdentityDataAPI().getStringParameters(objectId);
    } catch (Exception e) {
      LOG.error("Error getting string parameters for object {}: {}", objectId, e.getMessage(), e);
      listenerNotifier.notifyErrorOccurred("Failed to get string parameters: " + e.getMessage());
      return Collections.emptyMap();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, Double> getObjectDoubleParameters(UUID objectId) {
    if (objectId == null) return Collections.emptyMap();
    try {
      return editorDataAPI.getIdentityDataAPI().getDoubleParameters(objectId);
    } catch (Exception e) {
      LOG.error("Error getting double parameters for object {}: {}", objectId, e.getMessage(), e);
      listenerNotifier.notifyErrorOccurred("Failed to get double parameters: " + e.getMessage());
      return Collections.emptyMap();
    }
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public void saveLevelData(String fileName) throws EditorSaveException {
    editorDataAPI.saveLevelData(fileName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setCellSize(int cellSize) {
    if (cellSize > 0) {
      this.cellSize = cellSize;
      listenerNotifier.notifyCellSizeChanged(cellSize);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getCellSize() {
    return cellSize;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setSnapToGrid(boolean doSnap) {
    this.snapToGrid = doSnap;
    listenerNotifier.notifySnapToGridChanged(doSnap);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isSnapToGrid() {
    return snapToGrid;
  }
}