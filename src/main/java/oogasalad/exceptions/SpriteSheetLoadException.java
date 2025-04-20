package oogasalad.exceptions;

/**
 * Sprite sheet loading exception that is thrown when an error occurs when loading a sprite sheet
 * atlas from XML format.
 *
 * @author Jacob You
 */
public class SpriteSheetLoadException extends Exception {


  /**
   * Creates a new SpriteSheetLoadException - an exception when there is an error in loading sprite
   * sheet data.
   *
   * @param message the message to display to the user.
   */
  public SpriteSheetLoadException(String message) {
    super(message);
  }

  /**
   * Creates a new SpriteSheetLoadException - an exception when there is an error in loading sprite
   * sheet data.
   *
   * @param message the message to display to the user.
   * @param cause   the cause of the error.
   */
  public SpriteSheetLoadException(String message, Throwable cause) {
    super(message, cause);
  }
}
