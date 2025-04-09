package oogasalad.exceptions;

/**
 * Exception thrown when the view cannot be initialized. Indicates issues with setting up the
 * display environment.
 *
 * @author Aksel Bell
 */
public class ViewInitializationException extends Exception {

  /**
   * Constructs a new view visualization error
   *
   * @param message the message to display to user
   */
  public ViewInitializationException(String message) {
    super(message);
  }
}
