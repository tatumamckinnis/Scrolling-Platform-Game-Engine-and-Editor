package oogasalad.exceptions;

/**
 * Property Parsing Exception used when processing property/parameter data.
 *
 *
 * @author Billy McCune
 */
public class PropertyParsingException extends Exception {

  /**
   * creates a new PropertyParsingException - an exception when there is an error in processing property data.
   *
   * @param message the message to display to the user.
   *
   */
  public PropertyParsingException(String message) {
    super(message);
  }

  /**
   * creates a new PropertyParsingException - an exception when there is an error in processing property data.
   *
   * @param message the message to display to the user.
   * @param cause the cause of the error.
   */
  public PropertyParsingException(String message, Throwable cause) {
    super(message, cause);
  }
}
