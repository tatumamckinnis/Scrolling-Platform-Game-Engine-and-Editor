package oogasalad.editor.model.data.object_data;

public class HitboxData {
  private int x;
  private int y;
  private int width;
  private int height;
  private String shape;

  public HitboxData(int x, int y, int width, int height, String shape) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.shape = shape;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public String getShape() {
    return shape;
  }

  public void setX(int x) {
    this.x = x;
  }

  public void setY(int y) {
    this.y = y;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public void setShape(String shape) {
    this.shape = shape;
  }
}
