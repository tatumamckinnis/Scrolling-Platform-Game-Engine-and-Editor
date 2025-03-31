package old_editor_example;

public class SpriteData {
  private String spritePath;  // File path or identifier for the sprite image.
  private double x;
  private double y;
  private double width;
  private double height;

  public SpriteData(String spritePath, double x, double y, double width, double height) {
    this.spritePath = spritePath;
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  // Getters and setters.
  public String getSpritePath() {
    return spritePath;
  }
  public void setSpritePath(String spritePath) {
    this.spritePath = spritePath;
  }
  public double getX() {
    return x;
  }
  public void setX(double x) {
    this.x = x;
  }
  public double getY() {
    return y;
  }
  public void setY(double y) {
    this.y = y;
  }
  public double getWidth() {
    return width;
  }
  public void setWidth(double width) {
    this.width = width;
  }
  public double getHeight() {
    return height;
  }
  public void setHeight(double height) {
    this.height = height;
  }
}
