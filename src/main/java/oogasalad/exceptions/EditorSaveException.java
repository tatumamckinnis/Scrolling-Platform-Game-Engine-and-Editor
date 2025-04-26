package oogasalad.exceptions;

/**
 * Exception thrown when an error occurs during the saving of editor data
 *
 * @author Jacob You
 */
public class EditorSaveException extends Exception {

  /**
   * creates a new EditorSaveException - an exception when there is an error in saving editor data
   *
   * @param message the message to display to the user.
   */
  public EditorSaveException(String message) {
    super(message);
  }

  /**
   * creates a new EditorSaveException - an exception when there is an error in saving editor data
   *
   * @param message the message to display to the user.
   * @param cause   the cause of the error.
   */
  public EditorSaveException(String message, Throwable cause) {
    super(message, cause);
  }
}
