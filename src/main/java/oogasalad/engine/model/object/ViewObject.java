package oogasalad.engine.model.object;

import oogasalad.fileparser.records.FrameData;

/**
 * Immutable view-facing representation of a {@link GameObject}.
 *
 * <p>The {@code ViewObject} wraps a {@link GameObject} and exposes only the
 * read-only information necessary for rendering in the view layer, such as position, frame data,
 * and dimensions. It implements the {@link ImmutableGameObject} interface to enforce immutability
 * and prevent unintended mutations by the view.
 *
 * <p>This class acts as a safe abstraction layer between the engine's model and the UI,
 * ensuring encapsulation and clean separation of concerns.
 */
public class ViewObject implements ImmutableGameObject {

  private final GameObject gameObject;

  /**
   * Constructs a new ViewObject that wraps a given {@link GameObject}.
   *
   * @param gameObject the original game object to wrap
   */
  public ViewObject(GameObject gameObject) {
    this.gameObject = gameObject;
  }

  /**
   * Returns the UUID of the wrapped game object.
   *
   * @return the unique identifier as a string
   */
  @Override
  public String getUuid() {
    return gameObject.getUUID();
  }

  /**
   * Returns the x-coordinate of the object.
   *
   * @return x-position in pixels
   */
  @Override
  public int getX() {
    return gameObject.getXPosition();
  }

  /**
   * Returns the y-coordinate of the object.
   *
   * @return y-position in pixels
   */
  @Override
  public int getY() {
    return gameObject.getYPosition();
  }

  /**
   * Returns the current frame used for rendering the object.
   *
   * @return frame data for the object
   */
  @Override
  public FrameData getCurrentFrame() {
    return gameObject.getCurrentFrame();
  }

  /**
   * Returns the width of the object's hitbox.
   *
   * @return hitbox width in pixels
   */
  @Override
  public int getHitBoxWidth() {
    return gameObject.getHitBoxWidth();
  }

  /**
   * Returns the height of the object's hitbox.
   *
   * @return hitbox height in pixels
   */
  @Override
  public int getHitBoxHeight() {
    return gameObject.getHitBoxHeight();
  }

  /**
   * Returns the horizontal offset of the sprite relative to the hitbox.
   *
   * @return sprite x-offset
   */
  @Override
  public int getSpriteDx() {
    return gameObject.getSpriteDx();
  }

  /**
   * Returns the vertical offset of the sprite relative to the hitbox.
   *
   * @return sprite y-offset
   */
  @Override
  public int getSpriteDy() {
    return gameObject.getSpriteDy();
  }
}

