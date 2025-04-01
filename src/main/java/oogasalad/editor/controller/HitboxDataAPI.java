package oogasalad.editor.controller;

import java.util.UUID;
import oogasalad.editor.controller.api.HitboxDataAPIInterface;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.object.HitboxData;

public class HitboxDataAPI implements HitboxDataAPIInterface {
  EditorLevelData level;

  public HitboxDataAPI(EditorLevelData level) {
    this.level = level;
  }

  @Override
  public double getX(UUID id) {
    return level.getEditorObject(id).getHitboxData().x();
  }

  @Override
  public double getY(UUID id) {
    return level.getEditorObject(id).getHitboxData().y();
  }

  @Override
  public double getWidth(UUID id) {
    return level.getEditorObject(id).getHitboxData().width();
  }

  @Override
  public double getHeight(UUID id) {
    return level.getEditorObject(id).getHitboxData().height();
  }

  @Override
  public String getShape(UUID id) {
    return level.getEditorObject(id).getHitboxData().shape();
  }

  @Override
  public void setX(UUID id, double x) {

  }

  @Override
  public void setY(UUID id, double y) {

  }

  @Override
  public void setWidth(UUID id, double width) {

  }

  @Override
  public void setHeight(UUID id, double height) {

  }

  @Override
  public void setShape(UUID id, String shape) {

  }
}
