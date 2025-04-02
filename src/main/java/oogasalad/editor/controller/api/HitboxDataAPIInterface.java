package oogasalad.editor.controller.api;

import java.util.UUID;

public interface HitboxDataAPIInterface {
  public int getX(UUID id);
  public int getY(UUID id);
  public int getWidth(UUID id);
  public int getHeight(UUID id);
  public String getShape(UUID id);

  public void setX(UUID id, int x);
  public void setY(UUID id, int y);
  public void setWidth(UUID id, int width);
  public void setHeight(UUID id, int height);
  public void setShape(UUID id, String shape);
}
