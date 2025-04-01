package oogasalad.editor.controller.api;

import java.util.UUID;

public interface HitboxDataAPIInterface {
  public double getX(UUID id);
  public double getY(UUID id);
  public double getWidth(UUID id);
  public double getHeight(UUID id);
  public String getShape(UUID id);

  public void setX(UUID id, double x);
  public void setY(UUID id, double y);
  public void setWidth(UUID id, double width);
  public void setHeight(UUID id, double height);
  public void setShape(UUID id, String shape);
}
