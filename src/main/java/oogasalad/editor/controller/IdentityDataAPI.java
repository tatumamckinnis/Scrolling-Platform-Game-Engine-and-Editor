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
    return level.getEditorObject(id).getIdentityData().getName();
  }

  @Override
  public String getGroup(UUID id) {
    return level.getEditorObject(id).getIdentityData().getGroup();
  }

  @Override
  public void setName(UUID id, String name) {
    level.getEditorObject(id).getIdentityData().setName(name);
  }

  @Override
  public void setGroup(UUID id, String group) {
    level.getEditorObject(id).getIdentityData().setGroup(group);
  }
}
