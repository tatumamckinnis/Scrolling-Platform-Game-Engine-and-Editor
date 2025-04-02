package oogasalad.editor.controller;

import oogasalad.editor.controller.api.EditorEventDataAPIAbstraction;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.object.EditorEventData;
import oogasalad.editor.model.data.object.EditorObject;

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
}
