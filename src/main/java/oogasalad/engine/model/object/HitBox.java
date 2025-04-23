package oogasalad.engine.model.object;

import java.util.ResourceBundle;
import oogasalad.Main;

/**
 * Represents a rectangular hitbox used for collision detection and positioning in the game world.
 *
 * <p>The {@code HitBox} defines the object's spatial boundaries, including its position
 * (top-left corner) and dimensions. It is a core component of a {@link GameObject} and is used to
 * determine interactions with other objects and the game environment.
 *
 * @author Alana Zinkin
 */
public class HitBox {

  private static final ResourceBundle EXCEPTIONS = ResourceBundle.getBundle(
      "oogasalad.i18n.exceptions");

  private int x;
  private int y;
  private int width;
  private int height;

  /**
   * Constructs a {@code HitBox} with specified position and dimensions.
   *
   * @param x      the x-coordinate of the top-left corner
   * @param y      the y-coordinate of the top-left corner
   * @param width  the width of the hitbox
   * @param height the height of the hitbox
   */
  public HitBox(int x, int y, int width, int height) {
    this.x = x;
    this.y = y;
    if (width > 0 && height > 0) {
      this.width = width;
      this.height = height;
    }
    else {
      throw new IllegalArgumentException(EXCEPTIONS.getString("InvalidWidthAndHeight"));
    }
  }

  /**
   * Returns the x-coordinate of the hitbox's top-left corner.
   *
   * @return x-position in pixels
   */
  public int getX() {
    return x;
  }

  /**
   * Returns the y-coordinate of the hitbox's top-left corner.
   *
   * @return y-position in pixels
   */
  public int getY() {
    return y;
  }

  /**
   * Returns the width of the hitbox.
   *
   * @return width in pixels
   */
  public int getWidth() {
    return width;
  }

  /**
   * Returns the height of the hitbox.
   *
   * @return height in pixels
   */
  public int getHeight() {
    return height;
  }

  /**
   * Sets the x-coordinate of the hitbox's position.
   *
   * @param x new x-position in pixels
   */
  public void setX(int x) {
    this.x = x;
  }

  /**
   * Sets the y-coordinate of the hitbox's position.
   *
   * @param y new y-position in pixels
   */
  public void setY(int y) {
    this.y = y;
  }
}

