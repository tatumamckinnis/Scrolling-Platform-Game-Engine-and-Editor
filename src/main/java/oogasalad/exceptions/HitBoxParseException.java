package oogasalad.exceptions;

/**
 * Hit box parse exception thrown when the hitbox cannot be parsed.
 *
 * @author Billy McCune
 */
public class HitBoxParseException extends Exception {

  /**
   * creates a new HitBoxParseException - an exception when there is an error in processing hit box data
   * @param message the message to display to the user
   */
 public HitBoxParseException(String message) {
   super(message);
 }
}
