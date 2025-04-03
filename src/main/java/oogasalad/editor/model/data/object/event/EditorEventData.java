package oogasalad.editor.model.data.object.event;

import java.util.HashMap;
import java.util.Map;

public abstract class EditorEventData {
  private Map<String, EditorEvent> events;

  public EditorEventData() {
    events = new HashMap<>();
  }

  public Map<String, EditorEvent> getEvents() {
    return events;
  }
}
