package oogasalad.engine.model.object;

import java.io.File;
import oogasalad.fileparser.records.FrameData;

/**
 * An immutable interface for accessing read-only properties of a {@link GameObject}.
 *
 * <p>This interface defines the contract for game object representations that are safe to pass to
 * the view layer. Implementations like {@link ViewObject} provide access to essential rendering
 * information without exposing internal mutability.
 *
 * <p>This promotes encapsulation and ensures that the view cannot alter the game state.
 */
public interface ImmutableGameObject {

  /**
   * Returns the unique identifier (UUID) of the game object.
   *
   * @return UUID as a string
   */
  String getUuid();

  /**
   * Returns the x-coordinate of the game object's hitbox.
   *
   * @return x-position in pixels
   */
  int getX();

  /**
   * Returns the y-coordinate of the game object's hitbox.
   *
   * @return y-position in pixels
   */
  int getY();

  int getZ();

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

  File getSpriteFile();


}
