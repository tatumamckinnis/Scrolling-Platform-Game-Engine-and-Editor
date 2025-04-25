package oogasalad.exceptions;

/**
 * {@code UserDataParseException} is thrown when there is an error parsing user-generated data such
 * as configuration files, input formats, or user-created assets.
 *
 * <p>This exception may wrap an underlying cause such as a parsing or I/O error.</p>
 *
 * <p>Typical use cases include catching this exception when user-defined game data
 * is invalid or cannot be interpreted by the system.</p>
 *
 * @author Billy McCune
 * @see Exception
 */
public class UserDataParseException extends Exception {

  /**
   * Constructs a new {@code UserDataParseException} with the specified detail message.
   *
   * @param message the detail message explaining the reason for the exception
   */
  public UserDataParseException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@code UserDataParseException} with the specified detail message and cause.
   *
   * @param message the detail message explaining the reason for the exception
   * @param cause   the underlying cause of the exception (e.g., {@link java.io.IOException})
   */
  public UserDataParseException(String message, Throwable cause) {
    super(message, cause);
  }
}
