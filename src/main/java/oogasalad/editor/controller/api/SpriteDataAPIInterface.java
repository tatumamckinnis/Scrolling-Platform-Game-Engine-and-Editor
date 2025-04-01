package oogasalad.editor.controller.api;

import java.util.UUID;

public interface SpriteDataAPIInterface {
  public double getX(UUID id);
  public double getY(UUID id);
  public String getSpritePath(UUID id);

  public void setX(UUID id, double x);
  public void setY(UUID id, double y);
  public void setSpritePath(UUID id, String path);
}
