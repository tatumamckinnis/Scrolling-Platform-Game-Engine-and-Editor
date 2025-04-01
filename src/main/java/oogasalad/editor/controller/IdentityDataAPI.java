package oogasalad.editor.controller;

import java.util.UUID;
import oogasalad.editor.controller.api.IdentityDataAPIInterface;
import oogasalad.editor.model.data.EditorLevelData;

public class IdentityDataAPI implements IdentityDataAPIInterface {
  private EditorLevelData level;

  public IdentityDataAPI(EditorLevelData level) {
    this.level = level;
  }

  @Override
  public String getName(UUID id) {
    return level.getEditorObject(id).getIdentityData().name();
  }

  @Override
  public String getGroup(UUID id) {
    return level.getEditorObject(id).getIdentityData().group();
  }

  @Override
  public void setName(UUID id, String name) {

  }

  @Override
  public void setGroup(UUID id, String group) {

  }
}
