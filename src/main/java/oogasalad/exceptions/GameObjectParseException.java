package oogasalad.exceptions;

/**
 * Exception thrown when an error occurs while parsing a game object.
 *
 * <p>This may happen when loading game objects from data files (e.g., XML or JSON)
 * and encountering issues such as missing fields, invalid types, or unrecognized tags.
 * </p>
 * <p>This exception helps distinguish object-level parsing errors from more general I/O or
 * blueprint issues.
 * </p>
 *
 * @author Billy McCune
 */
public class GameObjectParseException extends Exception {

  /**
   * Constructs a new {@code GameObjectParseException} with a descriptive message.
   *
   * @param message the detail message explaining the cause of the exception.
   */
  public GameObjectParseException(String message) {
    super(message);
  }


  /**
   * Constructs a new {@code GameObjectParseException} with a descriptive message.
   *
   * @param message the detail message explaining the cause of the exception.
   * @param cause   the cause of the message.
   */
  public GameObjectParseException(String message, Throwable cause) {
    super(message, cause);
  }

}
