package oogasalad.editor.controller;

import java.util.UUID;
import oogasalad.editor.controller.api.EditorEventDataAPIAbstraction;
import oogasalad.editor.model.data.object.event.EditorEventData;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.EditorObject;

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

  public void createCollisionData(UUID id) {
    EditorObject object = super.getLevel().getEditorObject(id);
    object.createCollisionData();
  }
}
