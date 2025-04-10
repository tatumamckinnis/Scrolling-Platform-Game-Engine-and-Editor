package oogasalad.exceptions;

/**
 * Exception thrown when an error occurs during the parsing of level data.
 *
 * @author Billy McCune
 */
public class LevelDataParseException extends Exception {

  /**
   * creates a new LevelDataParseException - an exception when there is an error in processing level data.
   *
   * @param message the message to display to the user.
   */
  public LevelDataParseException(String message) {
    super(message);
  }

  /**
   * creates a new LevelDataParseException - an exception when there is an error in processing level data.
   *
   * @param message the message to display to the user.
   * @param cause the cause of the exception.
   */
  public LevelDataParseException(String message, Throwable cause) {
    super(message, cause);
  }
}
