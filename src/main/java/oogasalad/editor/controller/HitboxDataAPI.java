package oogasalad.editor.controller;

import java.util.UUID;
import oogasalad.editor.controller.api.HitboxDataAPIInterface;
import oogasalad.editor.model.data.EditorLevelData;

public class HitboxDataAPI implements HitboxDataAPIInterface {
  EditorLevelData level;

  public HitboxDataAPI(EditorLevelData level) {
    this.level = level;
  }

  @Override
  public int getX(UUID id) {
    return level.getEditorObject(id).getHitboxData().getX();
  }

  @Override
  public int getY(UUID id) {
    return level.getEditorObject(id).getHitboxData().getY();
  }

  @Override
  public int getWidth(UUID id) {
    return level.getEditorObject(id).getHitboxData().getWidth();
  }

  @Override
  public int getHeight(UUID id) {
    return level.getEditorObject(id).getHitboxData().getHeight();
  }

  @Override
  public String getShape(UUID id) {
    return level.getEditorObject(id).getHitboxData().getShape();
  }

  @Override
  public void setX(UUID id, int x) {
    level.getEditorObject(id).getHitboxData().setX(x);
  }

  @Override
  public void setY(UUID id, int y) {
    level.getEditorObject(id).getHitboxData().setY(y);
  }

  @Override
  public void setWidth(UUID id, int width) {
    level.getEditorObject(id).getHitboxData().setWidth(width);
  }

  @Override
  public void setHeight(UUID id, int height) {
    level.getEditorObject(id).getHitboxData().setHeight(height);
  }

  @Override
  public void setShape(UUID id, String shape) {
    level.getEditorObject(id).getHitboxData().setShape(shape);
  }
}
