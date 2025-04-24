package oogasalad.server;

import com.google.gson.Gson;

/**
 * Defines a server message data structure.
 *
 * @author Aksel Bell
 */
public class ServerMessage {

  private String type;
  private String message;
  private static Gson gson = new Gson();

  public ServerMessage(String type, String message) {
    this.type = type;
    this.message = message;
  }

  /**
   * Sends the gson message to the desired socket.
   *
   * @param socket a socket connected to the server.
   */
  public void sendToSocket(ClientSocket socket) {
    socket.send(gson.toJson(this));
  }

  /**
   * @return the type of the message
   */
  public String getType() {
    return type;
  }

  /**
   * @return the message
   */
  public String getMessage() {
    return message;
  }
}
