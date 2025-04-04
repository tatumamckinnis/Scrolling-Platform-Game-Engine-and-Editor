package oogasalad.exceptions;

/**
 * Exception thrown when there is an error processing user input.
 * Indicates issues with input devices or event handling.
 *
 * @author Aksel Bell
 */
public class InputException extends Exception {
  public InputException(String message) {
    super(message);
  }
}