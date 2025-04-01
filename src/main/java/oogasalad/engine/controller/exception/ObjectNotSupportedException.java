package oogasalad.engine.controller.exception;

/**
 * Exception thrown when an unsupported object type is encountered by the engine.
 *
 * <p>This runtime exception is typically used to indicate that a particular object
 * or operation is not supported by the current implementation or game configuration.
 *
 * @author Alana Zinkin
 */
public class ObjectNotSupportedException extends RuntimeException {

  /**
   * Constructs a new ObjectNotSupportedException with a specified detail message.
   *
   * @param message the detail message explaining the reason for the exception
   */
  public ObjectNotSupportedException(String message) {
    super(message);
  }
}
