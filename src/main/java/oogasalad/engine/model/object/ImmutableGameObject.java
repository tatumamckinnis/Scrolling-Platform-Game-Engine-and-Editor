package oogasalad.engine.model.object;

import java.io.File;
import oogasalad.fileparser.records.FrameData;

/**
 * An immutable interface for accessing read-only properties of a {@link GameObject}.
 *
 * <p>This interface defines the contract for game object representations that are safe to pass to
 * the view layer.
 *
 * <p>This promotes encapsulation and ensures that the view cannot alter the game state.
 */
public interface ImmutableGameObject {

  /**
   * Returns the unique identifier (UUID) of the game object.
   *
   * @return UUID as a string
   */
  String getUUID();

  /**
   * Returns the x-coordinate of the game object's hitbox.
   *
   * @return x-position in pixels
   */
  int getXPosition();

  /**
   * Returns the y-coordinate of the game object's hitbox.
   *
   * @return y-position in pixels
   */
  int getYPosition();

  /**
   * Returns the z-coordinate of the game object
   *
   * @return z-coordinate
   */
  int getLayer();

  /**
   * Returns the current frame data used for rendering the object.
   *
   * @return current {@link FrameData}
   */
  FrameData getCurrentFrame();

  /**
   * Returns the width of the object's hitbox.
   *
   * @return hitbox width in pixels
   */
  int getHitBoxWidth();

  /**
   * Returns the height of the object's hitbox.
   *
   * @return hitbox height in pixels
   */
  int getHitBoxHeight();

  /**
   * Returns the horizontal offset of the sprite relative to the hitbox.
   *
   * @return sprite x-offset in pixels
   */
  int getSpriteDx();

  /**
   * Returns the vertical offset of the sprite relative to the hitbox.
   *
   * @return sprite y-offset in pixels
   */
  int getSpriteDy();

  /**
   * @return file for the sprite sheet.
   */
  File getSpriteFile();

  /**
   * @return whether the object needs to be flipped in the view or not.
   */
  boolean getNeedsFlipped();

  void setNeedsFlipped(boolean didFlip);

  /**
   * @return the rotation for the object.
   */
  double getRotation();
}
