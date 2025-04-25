package oogasalad.exceptions;

/**
 * {@code UserDataWriteException} is thrown when an error occurs while attempting to write
 * user-generated data, such as saving configuration files, game assets, or other user-created
 * content.
 *
 * <p>This exception can wrap underlying I/O or serialization errors that prevent
 * data from being properly saved.</p>
 *
 * @author Billy McCune
 * @see Exception
 */
public class UserDataWriteException extends Exception {

  /**
   * Constructs a new {@code UserDataWriteException} with the specified detail message.
   *
   * @param message the detail message explaining the reason for the exception
   */
  public UserDataWriteException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@code UserDataWriteException} with the specified detail message and cause.
   *
   * @param message the detail message explaining the reason for the exception
   * @param cause   the underlying cause of the exception (e.g., {@link java.io.IOException})
   */
  public UserDataWriteException(String message, Throwable cause) {
    super(message, cause);
  }

}

