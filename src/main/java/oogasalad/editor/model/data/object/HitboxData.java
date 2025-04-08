package oogasalad.editor.model.data.object;

/**
 * Represents the hitbox data for an editor object, including its position, dimensions, and shape.
 */
public class HitboxData {
  private int x;
  private int y;
  private int width;
  private int height;
  private String shape;

  /**
   * Constructs a new HitboxData instance with the specified coordinates, dimensions, and shape.
   *
   * @param x the x-coordinate of the hitbox
   * @param y the y-coordinate of the hitbox
   * @param width the width of the hitbox
   * @param height the height of the hitbox
   * @param shape the shape of the hitbox
   */
  public HitboxData(int x, int y, int width, int height, String shape) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.shape = shape;
  }

  /**
   * Returns the x-coordinate of the hitbox.
   *
   * @return the x-coordinate
   */
  public int getX() {
    return x;
  }

  /**
   * Returns the y-coordinate of the hitbox.
   *
   * @return the y-coordinate
   */
  public int getY() {
    return y;
  }

  /**
   * Returns the width of the hitbox.
   *
   * @return the width of the hitbox
   */
  public int getWidth() {
    return width;
  }

  /**
   * Returns the height of the hitbox.
   *
   * @return the height of the hitbox
   */
  public int getHeight() {
    return height;
  }

  /**
   * Returns the shape of the hitbox.
   *
   * @return a string representing the shape of the hitbox
   */
  public String getShape() {
    return shape;
  }

  /**
   * Sets the x-coordinate of the hitbox.
   *
   * @param x the new x-coordinate
   */
  public void setX(int x) {
    this.x = x;
  }

  /**
   * Sets the y-coordinate of the hitbox.
   *
   * @param y the new y-coordinate
   */
  public void setY(int y) {
    this.y = y;
  }

  /**
   * Sets the width of the hitbox.
   *
   * @param width the new width of the hitbox
   */
  public void setWidth(int width) {
    this.width = width;
  }

  /**
   * Sets the height of the hitbox.
   *
   * @param height the new height of the hitbox
   */
  public void setHeight(int height) {
    this.height = height;
  }

  /**
   * Sets the shape of the hitbox.
   *
   * @param shape the new shape
   */
  public void setShape(String shape) {
    this.shape = shape;
  }
}
