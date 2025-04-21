package oogasalad.server;

/**
 * Defines a server message structure.
 */
public class ServerMessage {
  String type;
  String message;

  public ServerMessage(String type, String message) {
    this.type = type;
    this.message = message;
  }
}
