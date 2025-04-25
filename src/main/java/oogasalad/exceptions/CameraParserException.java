package oogasalad.exceptions;

/**
 * Signals an error encountered during the parsing of camera data.
 *
 * <p>
 * This runtime exception is specifically thrown when camera data cannot be processed correctly,
 * such as when required camera data is missing or the format is invalid.
 * </p>
 *
 * <p>
 * Being an unchecked exception, it does not require explicit declaration in a method's
 * {@code throws} clause.
 * </p>
 *
 * @author Billy McCune
 */
public class CameraParserException extends RuntimeException {

  /**
   * Constructs a new {@code CameraParserException} with the specified detail message.
   *
   * @param message the detail message that describes the exception
   */
  public CameraParserException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@code CameraParserException} with the specified detail message and cause.
   *
   * @param message the detail message that describes the exception
   * @param cause   the underlying cause of the exception, which can later be retrieved by the
   *                {@link #getCause()} method. A {@code null} value indicates that the cause is
   *                nonexistent or unknown
   */
  public CameraParserException(String message, Throwable cause) {
    super(message, cause);
  }
}
