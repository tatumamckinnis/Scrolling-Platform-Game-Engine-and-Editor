package oogasalad.editor.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import oogasalad.editor.controller.listeners.EditorListenerNotifier;
import oogasalad.editor.model.data.EditorObject;
import oogasalad.editor.model.data.object.DynamicVariable;
import oogasalad.editor.model.data.object.DynamicVariableContainer;
import oogasalad.editor.model.data.object.event.EditorEvent;
import oogasalad.editor.model.data.object.event.ExecutorData;
import oogasalad.editor.view.EditorViewListener;
import oogasalad.fileparser.records.BlueprintData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Concrete implementation of the EditorController interface. Acts as a mediator between the editor
 * view and the editor data backend (EditorDataAPI). Manages selection state, active tool,
 * listeners, and delegates specific actions to specialized handler classes.
 *
 * @author Tatum McKinnis, Jacob You (Original), Refactored by AI
 */
public class ConcreteEditorController implements EditorController {

  private static final Logger LOG = LogManager.getLogger(ConcreteEditorController.class);

  private final EditorDataAPI editorDataAPI;
  private UUID currentSelectedObjectId;
  private String activeToolName = "selectionTool"; // Default tool

  private final EditorListenerNotifier listenerNotifier;
  private final EditorObjectPlacementHandler objectPlacementHandler;
  private final EditorPrefabManager prefabManager;
  private final EditorEventHandler eventHandler;
  private final EditorObjectQueryHandler objectQueryHandler;

  /**
   * Constructs a ConcreteEditorController, initializing its data API and helper handlers.
   *
   * @param editorDataAPI The central API for accessing and modifying editor data. Cannot be null.
   */
  public ConcreteEditorController(EditorDataAPI editorDataAPI) {
    this.editorDataAPI = Objects.requireNonNull(editorDataAPI, "EditorDataAPI cannot be null.");

    this.listenerNotifier = new EditorListenerNotifier();
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
    // Consider if an error notification is needed here if the query handler logs errors internally
    // If not, add: if (foundId == null && someErrorOccurredInHandler) { notifyError(...) }
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
    LOG.debug("Controller adding dynamic variable '{}'", variable.getName());
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
    LOG.trace("Retrieving available dynamic variables (context objectId: {})", objectId);
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
}