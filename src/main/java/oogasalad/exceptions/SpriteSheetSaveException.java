package oogasalad.exceptions;

/**
 * Sprite sheet saving exception that is thrown when an error occurs when saving a sprite sheet
 * atlas into XML format.
 *
 * @author Jacob You
 */
public class SpriteSheetSaveException extends Exception {


  /**
   * Creates a new SpriteSheetSaveException - an exception when there is an error in saving sprite
   * sheet data.
   *
   * @param message the message to display to the user.
   */
  public SpriteSheetSaveException(String message) {
    super(message);
  }

  /**
   * Creates a new SpriteSheetSaveException - an exception when there is an error in saving sprite
   * sheet data.
   *
   * @param message the message to display to the user.
   * @param cause   the cause of the error.
   */
  public SpriteSheetSaveException(String message, Throwable cause) {
    super(message, cause);
  }
}
