package oogasalad.editor.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import oogasalad.editor.model.data.EditorObject;
import oogasalad.editor.model.data.event_enum.ConditionType;
import oogasalad.editor.model.data.event_enum.OutcomeType;
import oogasalad.editor.model.data.object.DynamicVariable;
import oogasalad.editor.model.data.object.event.EditorEvent;
import oogasalad.editor.view.EditorViewListener;

/**
 * Defines the contract between the Editor View components and the underlying Controller logic.
 * Includes methods for actions, data fetching, and listener management for view updates (Observer
 * Pattern). (DESIGN-09, DESIGN-11, DESIGN-18, DESIGN-20)
 *
 * @author Tatum McKinnis, Jacob You
 */
public interface EditorController {

  /**
   * Requests the placement of a new game object.
   */
  void requestObjectPlacement(String objectGroup, String objectNamePrefix, double worldX,
      double worldY, int cellSize);

  /**
   * Notifies the controller that an object has been selected in the view.
   */
  void notifyObjectSelected(UUID objectId);

  /**
   * Returns the editorDataAPI
   */
  EditorDataAPI getEditorDataAPI();

  void notifyObjectDeselected();

  /**
   * Adds a new event definition to the specified object.
   */
  void addEvent(UUID objectId, String eventId);

  /**
   * Removes an event definition from the specified object.
   */
  void removeEvent(UUID objectId, String eventId);

  /**
   * Adds a condition to an existing event.
   */
  void addCondition(UUID objectId, String eventId, ConditionType condition);

  /**
   * Removes a condition from an existing event.
   */
  void removeCondition(UUID objectId, String eventId, ConditionType condition);

  /**
   * Adds an outcome to an existing event, potentially with a parameter.
   */
  void addOutcome(UUID objectId, String eventId, OutcomeType outcome, String parameter);

  /**
   * Removes an outcome from an existing event.
   */
  void removeOutcome(UUID objectId, String eventId, OutcomeType outcome);

  /**
   * Adds a new dynamic variable.
   */
  void addDynamicVariable(DynamicVariable variable);

  /**
   * Requests the removal of the specified game object.
   */
  void requestObjectRemoval(UUID objectId);

  /**
   * Requests an update to an existing game object using the provided data.
   */
  void requestObjectUpdate(EditorObject updatedObject);

  /**
   * Gets the EditorObject associated with the given ID.
   */
  EditorObject getEditorObject(UUID objectId);

  /**
   * Gets all events associated with the specified object ID.
   */
  Map<String, EditorEvent> getEventsForObject(UUID objectId);

  /**
   * Gets all conditions associated with a specific event.
   */
  List<ConditionType> getConditionsForEvent(UUID objectId, String eventId);

  /**
   * Gets all outcomes associated with a specific event.
   */
  List<OutcomeType> getOutcomesForEvent(UUID objectId, String eventId);

  /**
   * Gets the parameter associated with a specific outcome of an event.
   */
  String getOutcomeParameter(UUID objectId, String eventId, OutcomeType outcome);

  /**
   * Gets all available dynamic variables (context might depend on objectId).
   */
  List<DynamicVariable> getAvailableDynamicVariables(UUID objectId);


  /**
   * Registers a listener to receive notifications about model/state changes.
   */
  void registerViewListener(EditorViewListener listener);

  /**
   * Unregisters a listener.
   */
  void unregisterViewListener(EditorViewListener listener);

  /**
   * Retrieves the object at specific coordinates.
   */
  UUID getObjectIDAt(double x, double y);
}