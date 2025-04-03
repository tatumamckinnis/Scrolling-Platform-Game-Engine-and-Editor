package oogasalad.editor.controller;

import java.util.UUID;
import oogasalad.editor.model.data.EditorLevelData;

public class HitboxDataManager {

  EditorLevelData level;

  public HitboxDataManager(EditorLevelData level) {
    this.level = level;
  }

  public int getX(UUID id) {
    return level.getEditorObject(id).getHitboxData().getX();
  }

  public int getY(UUID id) {
    return level.getEditorObject(id).getHitboxData().getY();
  }

  public int getWidth(UUID id) {
    return level.getEditorObject(id).getHitboxData().getWidth();
  }

  public int getHeight(UUID id) {
    return level.getEditorObject(id).getHitboxData().getHeight();
  }

  public String getShape(UUID id) {
    return level.getEditorObject(id).getHitboxData().getShape();
  }

  public void setX(UUID id, int x) {
    level.getEditorObject(id).getHitboxData().setX(x);
  }

  public void setY(UUID id, int y) {
    level.getEditorObject(id).getHitboxData().setY(y);
  }

  public void setWidth(UUID id, int width) {
    level.getEditorObject(id).getHitboxData().setWidth(width);
  }

  public void setHeight(UUID id, int height) {
    level.getEditorObject(id).getHitboxData().setHeight(height);
  }

  public void setShape(UUID id, String shape) {
    level.getEditorObject(id).getHitboxData().setShape(shape);
  }
}
