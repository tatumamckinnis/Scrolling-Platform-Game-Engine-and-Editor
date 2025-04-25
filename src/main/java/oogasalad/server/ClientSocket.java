package oogasalad.server;

import com.google.gson.Gson;
import oogasalad.engine.view.ViewState;
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
  private final ViewState viewState;
  private static final String SERVER_URI = ConfigLoader.getServerURL("server.dev.link") + "?filepath=%s&lobby=%s";
  private final int lobby;

  /**
   * Instantiate a new socket.
   */
  public ClientSocket(int lobby, String gamePath, ViewState viewState) throws URISyntaxException {
    super(new URI(String.format(SERVER_URI, gamePath, lobby)));
    gson = new Gson();
    this.viewState = viewState;
    this.lobby = lobby;
  }

  /**
   * Returns the lobby the client is currently in.
   */
  public int getLobby() {
    return lobby;
  }

  /**
   * Callback function that runs when connections has successfully been established.
   */
  @Override
  public void onOpen(ServerHandshake handshake) {
  }

  /**
   * Callback function that runs when client receives a message from the server.
   */
  @Override
  public void onMessage(String message) {
    ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
    LOG.info("Received from server: {}, {}", serverMessage.getType(), serverMessage.getMessage());

    MessageHandlerFactory.handleMessage(viewState, serverMessage).run();
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
}
