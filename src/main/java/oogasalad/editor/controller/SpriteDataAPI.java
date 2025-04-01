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
  public double getX(UUID id) {
    return level.getEditorObject(id).getSpriteData().x();
  }

  @Override
  public double getY(UUID id) {
    return level.getEditorObject(id).getSpriteData().y();
  }

  @Override
  public String getSpritePath(UUID id) {
    return level.getEditorObject(id).getSpriteData().spritePath();
  }

  @Override
  public void setX(UUID id, double x) {

  }

  @Override
  public void setY(UUID id, double y) {

  }

  @Override
  public void setSpritePath(UUID id, String path) {

  }
}
