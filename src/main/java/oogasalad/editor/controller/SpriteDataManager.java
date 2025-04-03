package oogasalad.editor.controller;

import java.util.UUID;
import oogasalad.editor.model.data.EditorLevelData;

public class SpriteDataManager {

  private EditorLevelData level;

  public SpriteDataManager(EditorLevelData level) {
    this.level = level;
  }

  public int getX(UUID id) {
    return level.getEditorObject(id).getSpriteData().getX();
  }

  public int getY(UUID id) {
    return level.getEditorObject(id).getSpriteData().getY();
  }

  public void setX(UUID id, int x) {
    level.getEditorObject(id).getSpriteData().setX(x);
  }

  public void setY(UUID id, int y) {
    level.getEditorObject(id).getSpriteData().setY(y);
  }
}
