package oogasalad.exceptions;

/**
 * sprite parser exception thrown when sprite file cannot be parsed.
 *
 * @author Billy McCune
 */
public class SpriteParseException extends Exception {

  /**
   * creates a new SpriteParseException - an exception when there is an error in processing user
   * sprites
   *
   * @param message the message to display to the user
   */
  public SpriteParseException(String message) {
    super(message);
  }
}
