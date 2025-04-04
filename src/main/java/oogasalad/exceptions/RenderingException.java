package oogasalad.exceptions;

/**
 * Exception thrown when there is an error during the rendering process.
 * Provides detailed information about rendering failures.
 *
 * @author Aksel Bell
 */
public class RenderingException extends Exception {
  public RenderingException(String message) {
    super(message);
  }
}
