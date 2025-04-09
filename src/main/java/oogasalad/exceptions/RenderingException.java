package oogasalad.exceptions;

/**
 * Exception thrown when there is an error during the rendering process. Provides detailed
 * information about rendering failures.
 *
 * @author Aksel Bell
 */
public class RenderingException extends Exception {

  /**
   * constructor for creating a rendering exception
   *
   * @param message the message to display to user
   */
  public RenderingException(String message) {
    super(message);
  }
}
