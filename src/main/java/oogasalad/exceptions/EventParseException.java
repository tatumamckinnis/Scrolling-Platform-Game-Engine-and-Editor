package oogasalad.exceptions;

/**
 * Exception thrown when an error occurs during the parsing of an event node.
 *
 * @author Billy McCune
 */
public class EventParseException extends Exception {

  /**
   * creates a new EventParseException - an exception when there is an error in processing event data
   * @param message the message to display to the user
   */
  public EventParseException(String message) {
    super(message);
  }
}
