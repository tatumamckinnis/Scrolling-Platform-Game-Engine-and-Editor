package oogasalad.editor.model.data.object_data;

import java.util.HashMap;
import java.util.Map;

public abstract class EditorEventData {
  private Map<String, oogasalad.editor.model.data.object_data.EditorEvent> events;

  public EditorEventData() {
    events = new HashMap<>();
  }

  public Map<String, oogasalad.editor.model.data.object_data.EditorEvent> getEvents() {
    return events;
  }
}
