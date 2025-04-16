package oogasalad.exceptions;

/**
 * Exception thrown when an error occurs during the parsing of a blueprint file.
 *
 * <p>This may occur if the blueprint XML or data structure is malformed,
 * missing required attributes, or contains invalid values that prevent proper object construction.
 * </p>
 *
 * <p>Used to signal issues specifically related to game object blueprint parsing
 * during the loading phase.
 * </p>
 *
 * @author Billy McCune
 */
public class BlueprintParseException extends Exception {

  /**
   * Constructs a new {@code BlueprintParseException} with the specified detail message.
   *
   * @param message the detail message describing the cause of the error.
   */
  public BlueprintParseException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@code BlueprintParseException} with the specified detail message.
   *
   * @param message the detail message describing the cause of the error.
   * @param cause   the cause of the exception.
   */
  public BlueprintParseException(String message, Throwable cause) {
    super(message, cause);
  }

}

