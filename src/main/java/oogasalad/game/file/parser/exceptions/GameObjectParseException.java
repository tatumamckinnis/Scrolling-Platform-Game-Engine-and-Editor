package oogasalad.game.file.parser.exceptions;

public class GameObjectParseException extends Exception {
  public GameObjectParseException(String message) {
    super(message);
  }

  public GameObjectParseException(String message, Throwable cause) {
    super(message, cause);
  }
}
