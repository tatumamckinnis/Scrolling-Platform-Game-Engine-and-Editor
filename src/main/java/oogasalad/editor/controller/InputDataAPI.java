package oogasalad.editor.controller;

import java.util.UUID;
import oogasalad.editor.controller.api.EditorEventDataAPIAbstraction;
import oogasalad.editor.model.data.object.event.EditorEventData;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.EditorObject;

public class InputDataAPI extends EditorEventDataAPIAbstraction {

  @Override
  protected EditorEventData createDataIfAbsent(EditorObject object) {
    if (object.getInputData() == null) {
      object.createInputData();
    }
    return object.getInputData();
  }

  public InputDataAPI(EditorLevelData level) {
    super(level);
  }

  public void createInputData(UUID id) {
    EditorObject object = super.getLevel().getEditorObject(id);
    object.createInputData();
  }
}
