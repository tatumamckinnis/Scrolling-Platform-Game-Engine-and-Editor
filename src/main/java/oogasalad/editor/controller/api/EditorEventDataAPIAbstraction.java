package oogasalad.editor.controller.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import oogasalad.editor.model.data.object_data.EditorEvent;
import oogasalad.editor.model.data.object_data.EditorEventData;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.event_enum.ConditionType;
import oogasalad.editor.model.data.event_enum.OutcomeType;
import oogasalad.editor.model.data.EditorObject;

public abstract class EditorEventDataAPIAbstraction implements EditorEventDataAPIInterface {

  private EditorLevelData level;

  protected abstract EditorEventData createDataIfAbsent(EditorObject object);

  protected EditorEventDataAPIAbstraction(EditorLevelData level) {
    this.level = level;
  }

  @Override
  public void addEvent(UUID id, String eventID) {
    EditorObject object = level.getEditorObject(id);
    if (object == null) { return; }

    EditorEventData data = createDataIfAbsent(object);
    data.getEvents().put(eventID, new EditorEvent());
  }

  @Override
  public void removeEvent(UUID id, String eventID) {
    EditorObject object = level.getEditorObject(id);
    if (object == null) { return; }

    EditorEventData data = createDataIfAbsent(object);
    data.getEvents().remove(eventID);
  }

  @Override
  public void addEventCondition(UUID id, String eventID, ConditionType condition) {
    EditorObject object = level.getEditorObject(id);
    if (object == null) { return; }

    EditorEventData data = createDataIfAbsent(object);
    if (data.getEvents().containsKey(eventID)) {
      data.getEvents().get(eventID).addCondition(condition);
    }
  }

  @Override
  public void addEventOutcome(UUID id, String eventID, OutcomeType outcome) {
    EditorObject object = level.getEditorObject(id);
    if (object == null) { return; }

    EditorEventData data = createDataIfAbsent(object);
    if (data.getEvents().containsKey(eventID)) {
      data.getEvents().get(eventID).addOutcome(outcome);
    }
  }

  @Override
  public void removeEventCondition(UUID id, String eventID, ConditionType condition) {
    EditorObject object = level.getEditorObject(id);
    if (object == null) { return; }

    EditorEventData data = createDataIfAbsent(object);
    if (data.getEvents().containsKey(eventID)) {
      data.getEvents().get(eventID).removeCondition(condition);
    }
  }

  @Override
  public void removeEventOutcome(UUID id, String eventID, OutcomeType outcome) {
    EditorObject object = level.getEditorObject(id);
    if (object == null) { return; }

    EditorEventData data = createDataIfAbsent(object);
    if (data.getEvents().containsKey(eventID)) {
      data.getEvents().get(eventID).removeOutcome(outcome);
    }
  }

  @Override
  public List<ConditionType> getEventConditions(UUID id, String eventID) {
    EditorObject object = level.getEditorObject(id);
    if (object == null) { return null; }

    EditorEventData data = createDataIfAbsent(object);
    if (data.getEvents().containsKey(eventID)) {
      return data.getEvents().get(eventID).getConditions();
    }
    return null;
  }

  @Override
  public List<OutcomeType> getEventOutcomes(UUID id, String eventID) {
    EditorObject object = level.getEditorObject(id);
    if (object == null) { return null; }

    EditorEventData data = createDataIfAbsent(object);
    if (data.getEvents().containsKey(eventID)) {
      return data.getEvents().get(eventID).getOutcomes();
    }
    return null;
  }

  public Map<String, EditorEvent> getEvents(UUID id) {
    EditorObject object = level.getEditorObject(id);
    if (object == null) { return null; }

    EditorEventData data = createDataIfAbsent(object);
    return data.getEvents();
  }

  public String getEventOutcomeParameter(UUID id, String eventID, OutcomeType outcome) {
    EditorObject object = level.getEditorObject(id);
    if (object == null) { return null; }

    EditorEventData data = createDataIfAbsent(object);
    if (data.getEvents().containsKey(eventID)) {
      return data.getEvents().get(eventID).getOutcomeParameter(outcome);
    }
    return null;
  }

  public String getEventConditionParameter(UUID id, String eventID, ConditionType condition) {
    EditorObject object = level.getEditorObject(id);
    if (object == null) { return null; }

    EditorEventData data = createDataIfAbsent(object);
    if (data.getEvents().containsKey(eventID)) {
      return data.getEvents().get(eventID).getConditionParameter(condition);
    }
    return null;
  }

  protected EditorLevelData getLevel() {
    return level;
  }
}
