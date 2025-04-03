package oogasalad.editor.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import oogasalad.editor.model.data.object.event.EditorEvent;
import oogasalad.editor.model.data.object.event.EditorEventData;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.event_enum.ConditionType;
import oogasalad.editor.model.data.event_enum.OutcomeType;
import oogasalad.editor.model.data.EditorObject;

public abstract class EditorEventDataManager {

  private EditorLevelData level;

  protected abstract EditorEventData createDataIfAbsent(EditorObject object);

  protected EditorEventDataManager(EditorLevelData level) {
    this.level = level;
  }

  public void addEvent(UUID id, String eventID) {
    EditorObject object = level.getEditorObject(id);
    if (object == null) { return; }

    EditorEventData data = createDataIfAbsent(object);
    data.getEvents().put(eventID, new EditorEvent());
  }

  public void removeEvent(UUID id, String eventID) {
    EditorObject object = level.getEditorObject(id);
    if (object == null) { return; }

    EditorEventData data = createDataIfAbsent(object);
    data.getEvents().remove(eventID);
  }

  public void addEventCondition(UUID id, String eventID, ConditionType condition) {
    EditorObject object = level.getEditorObject(id);
    if (object == null) { return; }

    EditorEventData data = createDataIfAbsent(object);
    if (data.getEvents().containsKey(eventID)) {
      data.getEvents().get(eventID).addCondition(condition);
    }
  }

  public void addEventOutcome(UUID id, String eventID, OutcomeType outcome) {
    EditorObject object = level.getEditorObject(id);
    if (object == null) { return; }

    EditorEventData data = createDataIfAbsent(object);
    if (data.getEvents().containsKey(eventID)) {
      data.getEvents().get(eventID).addOutcome(outcome);
    }
  }

  public void removeEventCondition(UUID id, String eventID, ConditionType condition) {
    EditorObject object = level.getEditorObject(id);
    if (object == null) { return; }

    EditorEventData data = createDataIfAbsent(object);
    if (data.getEvents().containsKey(eventID)) {
      data.getEvents().get(eventID).removeCondition(condition);
    }
  }

  public void removeEventOutcome(UUID id, String eventID, OutcomeType outcome) {
    EditorObject object = level.getEditorObject(id);
    if (object == null) { return; }

    EditorEventData data = createDataIfAbsent(object);
    if (data.getEvents().containsKey(eventID)) {
      data.getEvents().get(eventID).removeOutcome(outcome);
    }
  }

  public List<ConditionType> getEventConditions(UUID id, String eventID) {
    EditorObject object = level.getEditorObject(id);
    if (object == null) { return null; }

    EditorEventData data = createDataIfAbsent(object);
    if (data.getEvents().containsKey(eventID)) {
      return data.getEvents().get(eventID).getConditions();
    }
    return null;
  }

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
