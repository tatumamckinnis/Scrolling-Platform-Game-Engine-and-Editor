package oogasalad.exceptions;

/**
 * Layer parse exception thrown when the layer cannot be parsed
 *
 * @author Billy McCune
 */
public class LayerParseException extends Exception {

  /**
   * creates a new LayerParseException - an exception when there is an error in processing layer data
   * @param message
   */
  public LayerParseException(String message) {
    super(message);
  }
}
