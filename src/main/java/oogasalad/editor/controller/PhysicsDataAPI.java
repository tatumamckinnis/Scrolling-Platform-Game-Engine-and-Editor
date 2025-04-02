package oogasalad.editor.controller;

import java.util.UUID;
import oogasalad.editor.controller.api.EditorEventDataAPIAbstraction;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.object.EditorEventData;
import oogasalad.editor.model.data.object.EditorObject;

public class PhysicsDataAPI extends EditorEventDataAPIAbstraction {

  @Override
  protected EditorEventData createDataIfAbsent(EditorObject object) {
    if (object.getPhysicsData() == null) {
      object.createPhysicsData();
    }
    return object.getPhysicsData();
  }

  public PhysicsDataAPI(EditorLevelData level) {
    super(level);
  }

  public void createPhysicsData(UUID id) {
    EditorObject object = super.getLevel().getEditorObject(id);
    object.createInputData();
  }
}
