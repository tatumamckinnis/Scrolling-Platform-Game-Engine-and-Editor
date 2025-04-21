package oogasalad.server;

import com.google.gson.Gson;
import java.util.Objects;
import oogasalad.engine.controller.api.GameManagerAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * A client web socket that connects to the javascript server.
 *
 * @author Aksel Bell
 */
public class ClientSocket extends WebSocketClient {
  private static final Logger LOG = LogManager.getLogger();
  private final Gson gson;

  /**
   * Instantiate a new socket.
   */
  public ClientSocket(String serverUri) throws URISyntaxException {
    super(new URI(serverUri));
    gson = new Gson();
  }

  /**
   * Callback function that runs when connections has successfully been established.
   */
  @Override
  public void onOpen(ServerHandshake handshake) {
    ServerMessage serverMessage = new ServerMessage("echo", "Hello world!");
    send(gson.toJson(serverMessage));
  }

  /**
   * Callback function that runs when client receives a message from the server.
   */
  @Override
  public void onMessage(String message) {
    ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
    handleServerMessages(serverMessage);
  }

  /**
   * Callback function that runs when a client closes the connection.
   */
  @Override
  public void onClose(int code, String reason, boolean remote) {
    LOG.info("Connection closed: {}", reason);
  }

  /**
   * Callback function that runs when an error happens.
   */
  @Override
  public void onError(Exception ex) {
    LOG.warn("Connection error {}", ex.getMessage());
  }

  private void handleServerMessages(ServerMessage serverMessage) {
    LOG.info("Received from server: {}, {}", serverMessage.type, serverMessage.message);

    if(Objects.equals(serverMessage.type, "start")) {
      // TODO: change constructor to take in a view state and then call button action factory
      // TODO: then get action for start game
    }
  }
}
