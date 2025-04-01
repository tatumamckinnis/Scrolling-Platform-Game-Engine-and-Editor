package oogasalad.editor.controller;

import java.util.HashMap;
import java.util.UUID;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.object.EditorObject;
import oogasalad.editor.model.data.object.PhysicsData;

public class PhysicsDataAPI {
  EditorLevelData level;

  public PhysicsDataAPI(EditorLevelData level) {
    this.level = level;
  }

  public void addPhysicsData(UUID id) {
    EditorObject object = level.getEditorObject(id);
    if (object == null) { return; }
    if (object.getPhysicsData() == null) {
      object.setPhysicsData(new PhysicsData(new HashMap<>()));
    }
  }
}
