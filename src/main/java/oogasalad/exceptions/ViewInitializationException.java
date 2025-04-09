package oogasalad.exceptions;

/**
 * Exception thrown when the view cannot be initialized. Indicates issues with setting up the
 * display environment.
 *
 * @author Aksel Bell
 */
public class ViewInitializationException extends Exception {

  public ViewInitializationException(String message) {
    super(message);
  }
}
