package oogasalad.editor.controller;

import java.util.HashMap;
import java.util.UUID;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.object.CollisionData;
import oogasalad.editor.model.data.object.EditorObject;
import oogasalad.editor.model.data.object.PhysicsData;

public class CollisionDataAPI {
  EditorLevelData level;

  public CollisionDataAPI(EditorLevelData level) {
    this.level = level;
  }

  public void addCollisionData(UUID id) {
    EditorObject object = level.getEditorObject(id);
    if (object == null) { return; }
    if (object.getCollisionData() == null) {
      object.setCollisionData(new CollisionData(new HashMap<>()));
    }
  }
}
