package oogasalad.editor.controller;

import oogasalad.editor.controller.api.EditorEventDataAPIAbstraction;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.object.EditorEventData;
import oogasalad.editor.model.data.object.EditorObject;

public class CollisionDataAPI extends EditorEventDataAPIAbstraction {

  @Override
  protected EditorEventData createDataIfAbsent(EditorObject object) {
    if (object.getCollisionData() == null) {
      object.createCollisionData();
    }
    return object.getCollisionData();
  }

  public CollisionDataAPI(EditorLevelData level) {
    super(level);
  }
}
