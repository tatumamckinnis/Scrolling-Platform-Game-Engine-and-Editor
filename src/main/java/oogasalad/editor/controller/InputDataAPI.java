package oogasalad.editor.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.event_enum.ConditionType;
import oogasalad.editor.model.data.event_enum.OutcomeType;
import oogasalad.editor.model.data.object.EditorEvent;
import oogasalad.editor.model.data.object.EditorObject;
import oogasalad.editor.model.data.object.InputData;

public class InputDataAPI {
  EditorLevelData level;

  public InputDataAPI(EditorLevelData level) {
    this.level = level;
  }

  public void addInputData(UUID id) {
    EditorObject object = level.getEditorObject(id);
    if (object == null) { return; }
    if (object.getInputData() == null) {
      object.setInputData(new InputData(new HashMap<>()));
    }
  }

  /**
   * Sets a parameter for an outcome in an event
   * @param id Object ID
   * @param eventID Event ID
   * @param outcome Outcome type
   * @param parameterName Name of the dynamic variable to use as parameter
   */
  public void setInputEventOutcomeParameter(UUID id, String eventID, OutcomeType outcome, String parameterName) {
    EditorObject object = level.getEditorObject(id);
    if (object == null || object.getInputData() == null) { return; }

    EditorEvent event = object.getInputData().events().get(eventID);
    if (event != null) {
      event.setParameter(outcome, parameterName);
    }
  }

  /**
   * Gets the parameter for an outcome in an event
   * @param id Object ID
   * @param eventID Event ID
   * @param outcome Outcome type
   * @return Parameter name or null if not set
   */
  public String getInputEventOutcomeParameter(UUID id, String eventID, OutcomeType outcome) {
    EditorObject object = level.getEditorObject(id);
    if (object == null || object.getInputData() == null) { return null; }

    EditorEvent event = object.getInputData().events().get(eventID);
    if (event != null) {
      return event.getParameter(outcome);
    }
    return null;
  }

  /**
   * Gets all events for an object
   * @param id Object ID
   * @return Map of event IDs to EditorEvent objects
   */
  public Map<String, EditorEvent> getInputEvents(UUID id) {
    EditorObject object = level.getEditorObject(id);
    if (object == null || object.getInputData() == null) {
      return new HashMap<>();
    }

    return object.getInputData().events();
  }

  public void addInputEvent(UUID id, String eventID) {
    EditorObject object = level.getEditorObject(id);
    if (object == null) { return; }
    if (object.getInputData() != null) {
      object.getInputData().events().put(eventID, new EditorEvent());
    }
  }

  public void removeInputEvent(UUID id, String eventID) {
    EditorObject object = level.getEditorObject(id);
    if (object == null) { return; }
    if (object.getInputData() != null) {
      object.getInputData().events().remove(eventID);
    }
  }

  public void addInputEventCondition(UUID id, String eventID, ConditionType condition) {
    EditorObject object = level.getEditorObject(id);
    if (object == null) { return; }
    if (object.getInputData() != null) {
      object.getInputData().events().get(eventID).addCondition(condition);
    }
  }

  public void addInputEventOutcome(UUID id, String eventID, OutcomeType outcome) {
    EditorObject object = level.getEditorObject(id);
    if (object == null) { return; }
    if (object.getInputData() != null) {
      object.getInputData().events().get(eventID).addOutcome(outcome);
    }
  }

  public void removeInputEventCondition(UUID id, String eventID, ConditionType condition) {
    EditorObject object = level.getEditorObject(id);
    if (object == null) { return; }
    if (object.getInputData() != null) {
      object.getInputData().events().get(eventID).removeCondition(condition);
    }
  }

  public void removeInputEventOutcome(UUID id, String eventID, OutcomeType outcome) {
    EditorObject object = level.getEditorObject(id);
    if (object == null) { return; }
    if (object.getInputData() != null) {
      object.getInputData().events().get(eventID).removeOutcome(outcome);
    }
  }

  public List<ConditionType> getInputEventConditions(UUID id, String eventID) {
    EditorObject object = level.getEditorObject(id);
    if (object == null) { return null; }
    if (object.getInputData() != null) {
      return object.getInputData().events().get(eventID).getConditions();
    }
    return null;
  }

  public List<OutcomeType> getInputEventOutcomes(UUID id, String eventID) {
    EditorObject object = level.getEditorObject(id);
    if (object == null) { return null; }
    if (object.getInputData() != null) {
      return object.getInputData().events().get(eventID).getOutcomes();
    }
    return null;
  }
}
