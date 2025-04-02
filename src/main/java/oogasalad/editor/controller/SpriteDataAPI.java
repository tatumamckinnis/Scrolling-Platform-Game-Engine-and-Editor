package oogasalad.editor.controller;

import java.util.UUID;
import oogasalad.editor.controller.api.SpriteDataAPIInterface;
import oogasalad.editor.model.data.EditorLevelData;

public class SpriteDataAPI implements SpriteDataAPIInterface {
  private EditorLevelData level;

  public SpriteDataAPI(EditorLevelData level) {
    this.level = level;
  }

  @Override
  public int getX(UUID id) {
    return level.getEditorObject(id).getSpriteData().getX();
  }

  @Override
  public int getY(UUID id) {
    return level.getEditorObject(id).getSpriteData().getY();
  }

  @Override
  public String getSpritePath(UUID id) {
    return level.getEditorObject(id).getSpriteData().getSpritePath();
  }

  @Override
  public void setX(UUID id, int x) {
    level.getEditorObject(id).getSpriteData().setX(x);
  }

  @Override
  public void setY(UUID id, int y) {
    level.getEditorObject(id).getSpriteData().setY(y);
  }

  @Override
  public void setSpritePath(UUID id, String path) {
    level.getEditorObject(id).getSpriteData().setSpritePath(path);
  }
}
