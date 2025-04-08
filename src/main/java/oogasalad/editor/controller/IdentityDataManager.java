package oogasalad.editor.controller;

import java.util.UUID;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.EditorObject;

public class IdentityDataManager {
  private EditorLevelData level;

  public IdentityDataManager(EditorLevelData level) {
    this.level = level;
  }

  public String getName(UUID id) {
    return level.getEditorObject(id).getIdentityData().getName();
  }

  public String getGroup(UUID id) {
    return level.getEditorObject(id).getIdentityData().getGroup();
  }

  public void setName(UUID id, String name) {
    level.getEditorObject(id).getIdentityData().setName(name);
  }

  public void setGroup(UUID id, String group) {
    level.getEditorObject(id).getIdentityData().setGroup(group);
  }

  public int getLayerPriority(UUID id) {
    return level.getEditorObject(id).getIdentityData().getLayer().getPriority();
  }
}
