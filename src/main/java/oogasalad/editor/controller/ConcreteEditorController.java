package oogasalad.editor.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import oogasalad.editor.model.data.EditorObject;
import oogasalad.editor.model.data.event_enum.ConditionType;
import oogasalad.editor.model.data.event_enum.OutcomeType;
import oogasalad.editor.model.data.object.DynamicVariable;
import oogasalad.editor.model.data.object.DynamicVariableContainer;
import oogasalad.editor.model.data.object.event.EditorEvent;
import oogasalad.editor.view.EditorViewListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Concrete implementation of the EditorController interface. Acts as a mediator between the editor
 * view and the editor data backend (EditorDataAPI). Manages listeners and notifies them of changes
 * using the Observer pattern. (DESIGN-01, DESIGN-09: Controller Role, DESIGN-20: Observer Pattern,
 * DESIGN-21: Logging)
 */
public class ConcreteEditorController implements EditorController {

  private static final Logger LOG = LogManager.getLogger(ConcreteEditorController.class);

  private final EditorDataAPI editorDataAPI;
  private UUID currentSelectedObjectId;

  private final List<EditorViewListener> viewListeners = new CopyOnWriteArrayList<>();

  /**
   * Constructor for ConcreteEditorController.
   *
   * @param editorDataAPI The data access API providing methods to interact with editor data.
   */
  public ConcreteEditorController(EditorDataAPI editorDataAPI) {
    this.editorDataAPI = Objects.requireNonNull(editorDataAPI, "EditorDataAPI cannot be null.");
    LOG.info("ConcreteEditorController initialized.");
  }


  /**
   * Registers a new view listener if it is not already registered and is not null. This listener
   * will be notified of changes in the editor view.
   *
   * @param listener the {@link EditorViewListener} to register.
   */
  @Override
  public void registerViewListener(EditorViewListener listener) {
    if (listener != null && !viewListeners.contains(listener)) {
      viewListeners.add(listener);
      LOG.debug("Registered view listener: {}", listener.getClass().getSimpleName());
    }
  }

  /**
   * Unregisters a previously registered view listener if it is not null. The listener will no
   * longer receive editor view change notifications.
   *
   * @param listener the {@link EditorViewListener} to unregister.
   */
  @Override
  public void unregisterViewListener(EditorViewListener listener) {
    if (listener != null) {
      boolean removed = viewListeners.remove(listener);
      if (removed) {
        LOG.debug("Unregistered view listener: {}", listener.getClass().getSimpleName());
      }
    }
  }

  public EditorDataAPI getEditorDataAPI() {
    return editorDataAPI;
  }

  /**
   * Notifies all registered view listeners that a new object has been added.
   *
   * @param objectId the UUID of the object that was added.
   */
  private void notifyObjectAdded(UUID objectId) {
    LOG.debug("Notifying listeners: Object added {}", objectId);
    for (EditorViewListener listener : viewListeners) {
      try {
        listener.onObjectAdded(objectId);
      } catch (Exception e) {
        LOG.error("Error notifying listener {} about object addition: {}",
            listener.getClass().getSimpleName(), e.getMessage(), e);
      }
    }
  }

  /**
   * Notifies all registered view listeners that an object has been removed.
   *
   * @param objectId the UUID of the object that was removed.
   */
  private void notifyObjectRemoved(UUID objectId) {
    LOG.debug("Notifying listeners: Object removed {}", objectId);
    for (EditorViewListener listener : viewListeners) {
      try {
        listener.onObjectRemoved(objectId);
      } catch (Exception e) {
        LOG.error("Error notifying listener {} about object removal: {}",
            listener.getClass().getSimpleName(), e.getMessage(), e);
      }
    }
  }


  /**
   * Notifies all registered view listeners that an object has been updated.
   *
   * @param objectId the UUID of the object that was updated.
   */
  private void notifyObjectUpdated(UUID objectId) {
    LOG.debug("Notifying listeners: Object updated {}", objectId);
    for (EditorViewListener listener : viewListeners) {
      try {
        listener.onObjectUpdated(objectId);
      } catch (Exception e) {
        LOG.error("Error notifying listener {} about object update: {}",
            listener.getClass().getSimpleName(), e.getMessage(), e);
      }
    }
  }

  /**
   * Notifies all registered view listeners that the selected object has changed.
   *
   * @param selectedObjectId the UUID of the newly selected object.
   */
  private void notifySelectionChanged(UUID selectedObjectId) {
    LOG.debug("Notifying listeners: Selection changed {}", selectedObjectId);
    for (EditorViewListener listener : viewListeners) {
      try {
        listener.onSelectionChanged(selectedObjectId);
      } catch (Exception e) {
        LOG.error("Error notifying listener {} about selection change: {}",
            listener.getClass().getSimpleName(), e.getMessage(), e);
      }
    }
  }

  /**
   * Notifies all registered view listeners that the set of dynamic variables has changed.
   */
  private void notifyDynamicVariablesChanged() {
    LOG.debug("Notifying listeners: Dynamic variables changed");
    for (EditorViewListener listener : viewListeners) {
      try {
        listener.onDynamicVariablesChanged();
      } catch (Exception e) {
        LOG.error("Error notifying listener {} about dynamic variable change: {}",
            listener.getClass().getSimpleName(), e.getMessage(), e);
      }
    }
  }

  /**
   * Notifies all registered view listeners that an error has occurred.
   *
   * @param errorMessage the error message to be reported to the listeners.
   */
  private void notifyErrorOccurred(String errorMessage) {
    LOG.debug("Notifying listeners: Error occurred - {}", errorMessage);
    for (EditorViewListener listener : viewListeners) {
      try {
        listener.onErrorOccurred(errorMessage);
      } catch (Exception e) {
        LOG.error("Error notifying listener {} about error occurrence: {}",
            listener.getClass().getSimpleName(), e.getMessage(), e);
      }
    }
  }

  /**
   * Requests the placement of a new object within the editor at the specified world coordinates.
   * This method creates a new object, sets its properties (including name, group, position, and
   * size), and notifies listeners of the object's addition and selection. If any errors occur, they
   * are logged and reported to listeners.
   *
   * @param objectGroup      the group name to assign to the new object.
   * @param objectNamePrefix the prefix used to generate the object’s name.
   * @param worldX           the x-coordinate (in world units) where the object should be placed.
   * @param worldY           the y-coordinate (in world units) where the object should be placed.
   * @param cellSize         the size of a grid cell used to compute object placement and sizing.
   */
  @Override
  public void requestObjectPlacement(String objectGroup, String objectNamePrefix, int worldX,
      int worldY, int cellSize) {
    LOG.info("Processing object placement request: Group='{}', Prefix='{}', Pos=({},{})",
        objectGroup, objectNamePrefix, worldX, worldY);
    UUID newObjectId = null;
    try {
      newObjectId = editorDataAPI.createEditorObject();
      String objectName = String.format("%s%d_%d", objectNamePrefix, worldX / cellSize,
          worldY / cellSize);

      editorDataAPI.getIdentityDataAPI().setName(newObjectId, objectName);
      editorDataAPI.getIdentityDataAPI().setGroup(newObjectId, objectGroup);
      editorDataAPI.getSpriteDataAPI().setX(newObjectId, worldX);
      editorDataAPI.getSpriteDataAPI().setY(newObjectId, worldY);
      editorDataAPI.getHitboxDataAPI().setX(newObjectId, worldX);
      editorDataAPI.getHitboxDataAPI().setY(newObjectId, worldY);
      editorDataAPI.getHitboxDataAPI().setWidth(newObjectId, cellSize);
      editorDataAPI.getHitboxDataAPI().setHeight(newObjectId, cellSize);
      editorDataAPI.getHitboxDataAPI()
          .setShape(newObjectId, "RECTANGLE"); // TODO: Make shape configurable?

      LOG.debug("Object {} data set via EditorDataAPI.", newObjectId);

      notifyObjectAdded(newObjectId);
      notifyObjectSelected(newObjectId);

    } catch (Exception e) {
      LOG.error("Failed during object placement processing: {}", e.getMessage(), e);
      notifyErrorOccurred("Failed to place object: " + e.getMessage());
    }
  }


  /**
   * Processes a request to remove an object by its ID. If the object is successfully removed,
   * listeners are notified, and if the removed object was selected, the selection is cleared. In
   * case of failure, an error message is reported to listeners.
   *
   * @param objectId the UUID of the object to be removed.
   * @throws NullPointerException if the objectId is null.
   */
  @Override
  public void requestObjectRemoval(UUID objectId) {
    Objects.requireNonNull(objectId, "Object ID cannot be null for removal request");
    LOG.info("Processing object removal request: ID={}", objectId);
    try {
      boolean removed = editorDataAPI.removeEditorObject(objectId);

      if (removed) {
        LOG.debug("Successfully processed removal for object {}", objectId);
        notifyObjectRemoved(objectId);

        if (objectId.equals(currentSelectedObjectId)) {
          notifyObjectSelected(null);
        }
      } else {
        LOG.warn("Removal request processed but object {} was not found or not removed by backend.",
            objectId);
        notifyErrorOccurred("Object with ID " + objectId + " could not be removed (not found).");
      }

    } catch (Exception e) {
      LOG.error("Failed during object removal processing for ID {}: {}", objectId, e.getMessage(),
          e);
      notifyErrorOccurred("Failed to remove object: " + e.getMessage());
    }
  }

  /**
   * Processes a request to update an existing object. If the object is successfully updated,
   * listeners are notified of the update. In case of failure, an error message is reported to
   * listeners.
   *
   * @param updatedObject the object with updated data.
   * @throws NullPointerException if the updatedObject or its ID is null.
   */
  @Override
  public void requestObjectUpdate(EditorObject updatedObject) {
    Objects.requireNonNull(updatedObject, "Updated object cannot be null");
    UUID objectId = updatedObject.getId(); // Use the getId() method from EditorObject
    Objects.requireNonNull(objectId, "Object ID cannot be null for update request");
    LOG.info("Processing object update request: ID={}", objectId);
    try {
      boolean updated = editorDataAPI.updateEditorObject(updatedObject);

      if (updated) {
        LOG.debug("Successfully processed update for object {}", objectId);
        notifyObjectUpdated(objectId);
      } else {
        LOG.warn("Update request processed but object {} was not found or not updated by backend.",
            objectId);
        notifyErrorOccurred("Object with ID " + objectId + " could not be updated (not found).");
      }
    } catch (Exception e) {
      LOG.error("Failed during object update processing for ID {}: {}", objectId, e.getMessage(),
          e);
      notifyErrorOccurred("Failed to update object: " + e.getMessage());
    }
  }

  /**
   * Notifies listeners that a specific object has been selected. If the selected object is
   * different from the current selection, the controller updates the selection and notifies
   * listeners of the change.
   *
   * @param objectId the UUID of the selected object, or null to clear the selection.
   */
  @Override
  public void notifyObjectSelected(UUID objectId) {
    if (!Objects.equals(this.currentSelectedObjectId, objectId)) {
      this.currentSelectedObjectId = objectId;
      LOG.info("Controller state updated: Object selected: {}", objectId);
      notifySelectionChanged(this.currentSelectedObjectId);
    }
  }

  @Override
  public void notifyObjectDeselected() {
    this.currentSelectedObjectId = null;
    LOG.info("Controller state updated: Object deselected");
  }

  /**
   * Adds an event to an object. The event ID cannot be null or empty. If successful, listeners are
   * notified of the object's update. Otherwise, an error message is reported to listeners.
   *
   * @param objectId the UUID of the object to add the event to.
   * @param eventId  the ID of the event to add.
   * @throws NullPointerException     if the objectId or eventId is null.
   * @throws IllegalArgumentException if the eventId is empty.
   */
  @Override
  public void addEvent(UUID objectId, String eventId) {
    Objects.requireNonNull(objectId, "Object ID cannot be null for addEvent");
    Objects.requireNonNull(eventId, "Event ID cannot be null for addEvent");
    if (eventId.trim().isEmpty()) {
      String errorMsg = "Event ID cannot be empty.";
      LOG.warn(errorMsg);
      notifyErrorOccurred(errorMsg);
      return;
    }
    LOG.debug("Controller adding event '{}' to object {}", eventId, objectId);
    try {
      editorDataAPI.getInputDataAPI().addEvent(objectId, eventId);
      notifyObjectUpdated(objectId);
    } catch (Exception e) {
      LOG.error("Failed to add event '{}' to object {}: {}", eventId, objectId, e.getMessage(), e);
      notifyErrorOccurred("Failed to add event: " + e.getMessage());
    }
  }

  /**
   * Removes an event from an object. If successful, listeners are notified of the object's update.
   * In case of failure, an error message is reported to listeners.
   *
   * @param objectId the UUID of the object to remove the event from.
   * @param eventId  the ID of the event to remove.
   * @throws NullPointerException if the objectId or eventId is null.
   */
  @Override
  public void removeEvent(UUID objectId, String eventId) {
    Objects.requireNonNull(objectId, "Object ID cannot be null for removeEvent");
    Objects.requireNonNull(eventId, "Event ID cannot be null for removeEvent");
    LOG.debug("Controller removing event '{}' from object {}", eventId, objectId);
    try {
      editorDataAPI.getInputDataAPI().removeEvent(objectId, eventId);
      notifyObjectUpdated(objectId);
    } catch (Exception e) {
      LOG.error("Failed to remove event '{}' from object {}: {}", eventId, objectId, e.getMessage(),
          e);
      notifyErrorOccurred("Failed to remove event: " + e.getMessage());
    }
  }

  /**
   * Adds a condition to an event on an object. If successful, listeners are notified of the
   * object's update. In case of failure, an error message is reported to listeners.
   *
   * @param objectId  the UUID of the object to add the condition to.
   * @param eventId   the ID of the event to add the condition to.
   * @param condition the condition to add to the event.
   * @throws NullPointerException if the objectId, eventId, or condition is null.
   */
  @Override
  public void addCondition(UUID objectId, String eventId, ConditionType condition) {
    Objects.requireNonNull(objectId, "Object ID cannot be null for addCondition");
    Objects.requireNonNull(eventId, "Event ID cannot be null for addCondition");
    Objects.requireNonNull(condition, "ConditionType cannot be null for addCondition");
    LOG.debug("Controller adding condition '{}' to event '{}' on object {}", condition, eventId,
        objectId);
    try {
      editorDataAPI.getInputDataAPI().addEventCondition(objectId, eventId, condition);
      notifyObjectUpdated(objectId);
    } catch (Exception e) {
      LOG.error("Failed to add condition '{}' to event '{}' on object {}: {}", condition, eventId,
          objectId, e.getMessage(), e);
      notifyErrorOccurred("Failed to add condition: " + e.getMessage());
    }
  }

  /**
   * Removes a condition from an event on an object. If successful, listeners are notified of the
   * object's update. In case of failure, an error message is reported to listeners.
   *
   * @param objectId  the UUID of the object to remove the condition from.
   * @param eventId   the ID of the event to remove the condition from.
   * @param condition the condition to remove from the event.
   * @throws NullPointerException if the objectId, eventId, or condition is null.
   */
  @Override
  public void removeCondition(UUID objectId, String eventId, ConditionType condition) {
    Objects.requireNonNull(objectId, "Object ID cannot be null for removeCondition");
    Objects.requireNonNull(eventId, "Event ID cannot be null for removeCondition");
    Objects.requireNonNull(condition, "ConditionType cannot be null for removeCondition");
    LOG.debug("Controller removing condition '{}' from event '{}' on object {}", condition, eventId,
        objectId);
    try {
      editorDataAPI.getInputDataAPI().removeEventCondition(objectId, eventId, condition);
      notifyObjectUpdated(objectId);
    } catch (Exception e) {
      LOG.error("Failed to remove condition '{}' from event '{}' on object {}: {}", condition,
          eventId, objectId, e.getMessage(), e);
      notifyErrorOccurred("Failed to remove condition: " + e.getMessage());
    }
  }


  /**
   * Adds an outcome to the specified event of an object.
   *
   * @param objectId  The ID of the object to which the outcome is to be added. Must not be null.
   * @param eventId   The ID of the event to which the outcome is to be added. Must not be null.
   * @param outcome   The outcome type to be added. Must not be null.
   * @param parameter The parameter associated with the outcome, may be null or empty.
   * @throws NullPointerException If any of the provided parameters are null.
   */
  @Override
  public void addOutcome(UUID objectId, String eventId, OutcomeType outcome, String parameter) {
    Objects.requireNonNull(objectId, "Object ID cannot be null for addOutcome");
    Objects.requireNonNull(eventId, "Event ID cannot be null for addOutcome");
    Objects.requireNonNull(outcome, "OutcomeType cannot be null for addOutcome");
    LOG.debug("Controller adding outcome '{}' (Param: '{}') to event '{}' on object {}", outcome,
        parameter, eventId, objectId);
    try {
      editorDataAPI.getInputDataAPI().addEventOutcome(objectId, eventId, outcome);
      String paramToSet =
          (parameter != null && !parameter.trim().isEmpty()) ? parameter.trim() : null;
      editorDataAPI.getInputDataAPI()
          .setEventOutcomeParameter(objectId, eventId, outcome, paramToSet);
      notifyObjectUpdated(objectId);
    } catch (Exception e) {
      LOG.error("Failed to add outcome '{}' to event '{}' on object {}: {}", outcome, eventId,
          objectId, e.getMessage(), e);
      notifyErrorOccurred("Failed to add outcome: " + e.getMessage());
    }
  }

  /**
   * Removes an outcome from the specified event of an object.
   *
   * @param objectId The ID of the object from which the outcome is to be removed. Must not be
   *                 null.
   * @param eventId  The ID of the event from which the outcome is to be removed. Must not be null.
   * @param outcome  The outcome type to be removed. Must not be null.
   * @throws NullPointerException If any of the provided parameters are null.
   */
  @Override
  public void removeOutcome(UUID objectId, String eventId, OutcomeType outcome) {
    Objects.requireNonNull(objectId, "Object ID cannot be null for removeOutcome");
    Objects.requireNonNull(eventId, "Event ID cannot be null for removeOutcome");
    Objects.requireNonNull(outcome, "OutcomeType cannot be null for removeOutcome");
    LOG.debug("Controller removing outcome '{}' from event '{}' on object {}", outcome, eventId,
        objectId);
    try {
      editorDataAPI.getInputDataAPI().removeEventOutcome(objectId, eventId, outcome);
      notifyObjectUpdated(objectId);
    } catch (Exception e) {
      LOG.error("Failed to remove outcome '{}' from event '{}' on object {}: {}", outcome, eventId,
          objectId, e.getMessage(), e);
      notifyErrorOccurred("Failed to remove outcome: " + e.getMessage());
    }
  }

  /**
   * Adds a dynamic variable to the system.
   *
   * @param variable The dynamic variable to be added. Must not be null.
   * @throws NullPointerException     If the provided variable is null.
   * @throws IllegalArgumentException If the variable is invalid or already exists in the system.
   */
  @Override
  public void addDynamicVariable(DynamicVariable variable) {
    Objects.requireNonNull(variable, "DynamicVariable cannot be null");
    LOG.debug("Controller adding dynamic variable '{}'", variable.getName());
    try {
      DynamicVariableContainer container = editorDataAPI.getDynamicVariableContainer();
      if (container != null) {
        container.addVariable(variable);
        notifyDynamicVariablesChanged();
      } else {
        LOG.error("DynamicVariableContainer is null in EditorDataAPI. Cannot add variable.");
        notifyErrorOccurred("Internal error: Cannot access variable storage.");
      }
    } catch (IllegalArgumentException e) {
      LOG.warn("Failed to add dynamic variable '{}' due to invalid input or duplicate: {}",
          variable.getName(), e.getMessage());
      notifyErrorOccurred("Failed to add variable: " + e.getMessage());
    } catch (Exception e) {
      LOG.error("Failed to add dynamic variable '{}': {}", variable.getName(), e.getMessage(), e);
      notifyErrorOccurred("Failed to add dynamic variable: " + e.getMessage());
    }
  }

  /**
   * Retrieves the editor object associated with the specified ID.
   *
   * @param objectId The ID of the editor object to be retrieved. If null, the method returns null.
   * @return The editor object associated with the specified ID, or null if an error occurs or the
   * ID is invalid.
   */
  @Override
  public EditorObject getEditorObject(UUID objectId) {
    if (objectId == null) {
      LOG.trace("getEditorObject called with null ID.");
      return null;
    }
    try {
      EditorObject obj = editorDataAPI.getEditorObject(objectId);
      return obj;
    } catch (Exception e) {
      LOG.error("Failed to get EditorObject for ID {}: {}", objectId, e.getMessage(), e);
      notifyErrorOccurred("Error retrieving object data for ID: " + objectId);
      return null;
    }
  }


  /**
   * Retrieves the events associated with the specified object ID.
   *
   * @param objectId The ID of the object for which events are to be retrieved. If null, returns an
   *                 empty map.
   * @return A map of event IDs to corresponding {@link EditorEvent} objects, or an empty map if no
   * events are found or an error occurs.
   */
  @Override
  public Map<String, EditorEvent> getEventsForObject(UUID objectId) {
    if (objectId == null) {
      return Collections.emptyMap();
    }
    try {
      Map<String, EditorEvent> events = editorDataAPI.getInputDataAPI().getEvents(objectId);
      return (events != null) ? events : Collections.emptyMap();
    } catch (Exception e) {
      LOG.error("Failed to get events for object {}: {}", objectId, e.getMessage(), e);
      notifyErrorOccurred("Error retrieving events for object ID: " + objectId);
      return Collections.emptyMap();
    }
  }

  /**
   * Retrieves the conditions associated with the specified event of an object.
   *
   * @param objectId The ID of the object for which event conditions are to be retrieved. Must not
   *                 be null.
   * @param eventId  The ID of the event for which conditions are to be retrieved. Must not be
   *                 null.
   * @return A list of {@link ConditionType} objects associated with the event, or an empty list if
   * no conditions are found or an error occurs.
   */
  @Override
  public List<ConditionType> getConditionsForEvent(UUID objectId, String eventId) {
    if (objectId == null || eventId == null) {
      return Collections.emptyList();
    }
    try {
      List<ConditionType> conditions = editorDataAPI.getInputDataAPI()
          .getEventConditions(objectId, eventId);
      return (conditions != null) ? conditions : Collections.emptyList();
    } catch (Exception e) {
      LOG.error("Failed to get conditions for event '{}' on object {}: {}", eventId, objectId,
          e.getMessage(), e);
      notifyErrorOccurred("Error retrieving conditions for event: " + eventId);
      return Collections.emptyList();
    }
  }

  /**
   * Retrieves the outcomes associated with the specified event of an object.
   *
   * @param objectId The ID of the object for which event outcomes are to be retrieved. Must not be
   *                 null.
   * @param eventId  The ID of the event for which outcomes are to be retrieved. Must not be null.
   * @return A list of {@link OutcomeType} objects associated with the event, or an empty list if no
   * outcomes are found or an error occurs.
   */
  @Override
  public List<OutcomeType> getOutcomesForEvent(UUID objectId, String eventId) {
    if (objectId == null || eventId == null) {
      return Collections.emptyList();
    }
    try {
      List<OutcomeType> outcomes = editorDataAPI.getInputDataAPI()
          .getEventOutcomes(objectId, eventId);
      return (outcomes != null) ? outcomes : Collections.emptyList();
    } catch (Exception e) {
      LOG.error("Failed to get outcomes for event '{}' on object {}: {}", eventId, objectId,
          e.getMessage(), e);
      notifyErrorOccurred("Error retrieving outcomes for event: " + eventId);
      return Collections.emptyList();
    }
  }

  /**
   * Retrieves the parameter associated with the specified outcome of an event for an object.
   *
   * @param objectId The ID of the object for which the outcome parameter is to be retrieved. Must
   *                 not be null.
   * @param eventId  The ID of the event for which the outcome parameter is to be retrieved. Must
   *                 not be null.
   * @param outcome  The outcome type for which the parameter is to be retrieved. Must not be null.
   * @return The parameter associated with the outcome, or null if not found or an error occurs.
   */
  @Override
  public String getOutcomeParameter(UUID objectId, String eventId, OutcomeType outcome) {
    if (objectId == null || eventId == null || outcome == null) {
      return null;
    }
    try {
      return editorDataAPI.getInputDataAPI().getEventOutcomeParameter(objectId, eventId, outcome);
    } catch (Exception e) {
      LOG.error("Failed to get parameter for outcome '{}' in event '{}' on object {}: {}", outcome,
          eventId, objectId, e.getMessage(), e);
      notifyErrorOccurred("Error retrieving outcome parameter for event: " + eventId);
      return null;
    }
  }

  /**
   * Retrieves the dynamic variables available for the specified object ID.
   *
   * @param objectId The ID of the object (currently ignored). Must not be null.
   * @return A list of {@link DynamicVariable} objects available, or an empty list if none are found
   * or an error occurs.
   */
  @Override
  public List<DynamicVariable> getAvailableDynamicVariables(UUID objectId) {
    try {
      DynamicVariableContainer container = editorDataAPI.getDynamicVariableContainer();
      if (container != null) {
        Collection<DynamicVariable> varsCollection = container.getAllVariables();
        if (varsCollection != null) {
          return new ArrayList<>(varsCollection);
        } else {
          LOG.error("container.getAllVariables() returned null unexpectedly!");
          return Collections.emptyList();
        }
      } else {
        LOG.warn("DynamicVariableContainer is null in EditorDataAPI.");
        return Collections.emptyList();
      }
    } catch (Exception e) {
      LOG.error("Failed to get available dynamic variables: {}", e.getMessage(), e);
      notifyErrorOccurred("Error retrieving dynamic variables.");
      return Collections.emptyList();
    }
  }

  public UUID getObjectIDAt(double gridX, double gridY) {
    List<UUID> hitCandidates = new ArrayList<>();
    for (Map.Entry<UUID, EditorObject> entry : editorDataAPI.getLevel().getObjectDataMap()
        .entrySet()) {
      UUID id = entry.getKey();
      EditorObject obj = entry.getValue();
      if (obj != null && ifCollidesObject(obj, gridX, gridY)) {
        hitCandidates.add(id);
      }
    }
    Collections.sort(hitCandidates, (a, b) ->
        Integer.compare(editorDataAPI.getIdentityDataAPI().getLayerPriority(b),
            editorDataAPI.getIdentityDataAPI().getLayerPriority(a))
    );
    return hitCandidates.isEmpty() ? null : hitCandidates.get(0);
  }

  private boolean ifCollidesObject(EditorObject obj, double worldX, double worldY) {
    if (obj == null) {
      return false;
    }

    double x = obj.getHitboxData().getX();
    double y = obj.getHitboxData().getY();
    int width = obj.getHitboxData().getWidth();
    int height = obj.getHitboxData().getHeight();

    return (worldX >= x && worldX < x + width) && (worldY >= y && worldY < y + height);
  }
}
