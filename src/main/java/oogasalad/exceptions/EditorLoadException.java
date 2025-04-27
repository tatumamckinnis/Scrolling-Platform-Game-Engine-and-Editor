package oogasalad.exceptions;

/**
 * Exception thrown when an error occurs during the loading of editor data
 *
 * @author Jacob You
 */
public class EditorLoadException extends Exception {

  /**
   * creates a new EditorLoadException - an exception when there is an error in loading editor data
   *
   * @param message the message to display to the user.
   */
  public EditorLoadException(String message) {
    super(message);
  }

  /**
   * creates a new EditorLoadException - an exception when there is an error in loading editor data
   *
   * @param message the message to display to the user.
   * @param cause   the cause of the error.
   */
  public EditorLoadException(String message, Throwable cause) {
    super(message, cause);
  }
}
