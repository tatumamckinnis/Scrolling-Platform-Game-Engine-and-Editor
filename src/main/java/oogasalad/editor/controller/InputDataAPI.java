package oogasalad.editor.controller;

import java.util.HashMap;
import java.util.List;
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
