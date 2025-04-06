package oogasalad.editor.controller;

import java.util.UUID;
import oogasalad.editor.model.data.object.event.EditorEventData;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.EditorObject;

public class PhysicsDataManager extends EditorEventDataManager {

  @Override
  protected EditorEventData createDataIfAbsent(EditorObject object) {
    return object.getPhysicsData();
  }

  public PhysicsDataManager(EditorLevelData level) {
    super(level);
  }
}
