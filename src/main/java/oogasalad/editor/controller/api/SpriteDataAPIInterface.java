package oogasalad.editor.controller.api;

import java.util.UUID;

public interface SpriteDataAPIInterface {
  public int getX(UUID id);
  public int getY(UUID id);
  public String getSpritePath(UUID id);

  public void setX(UUID id, int x);
  public void setY(UUID id, int y);
  public void setSpritePath(UUID id, String path);
}
