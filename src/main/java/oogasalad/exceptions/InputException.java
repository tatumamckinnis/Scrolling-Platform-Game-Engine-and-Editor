package oogasalad.exceptions;

/**
 * Exception thrown when there is an error processing user input. Indicates issues with input.
 * devices or event handling.
 *
 * @author Aksel Bell
 */
public class InputException extends Exception {

  /**
   * creates a new InputException - an exception when there is an error in processing user input.
   *
   * @param message the message to display to the user.
   */
  public InputException(String message) {
    super(message);
  }
}