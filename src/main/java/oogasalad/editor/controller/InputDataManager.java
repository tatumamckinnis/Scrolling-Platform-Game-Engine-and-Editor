package oogasalad.editor.controller;

import java.util.UUID;
import oogasalad.editor.model.data.object.event.EditorEventData;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.EditorObject;

public class InputDataManager extends EditorEventDataManager {

  @Override
  protected EditorEventData createDataIfAbsent(EditorObject object) {
    return object.getInputData();
  }

  public InputDataManager(EditorLevelData level) {
    super(level);
  }
}
