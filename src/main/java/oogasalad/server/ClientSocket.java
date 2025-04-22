package oogasalad.server;

import com.google.gson.Gson;
import java.util.Objects;
import javafx.application.Platform;
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
  private static final String SERVER_URI = "ws://localhost:%d?filepath=%s";

  /**
   * Instantiate a new socket.
   */
  public ClientSocket(int port, String gamePath, ViewState viewState) throws URISyntaxException {
    super(new URI(String.format(SERVER_URI, port, gamePath)));
    gson = new Gson();
    this.viewState = viewState;
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

    if(Objects.equals(serverMessage.type, "startGame")) {
      Platform.runLater(() -> {
        MessageHandlerFactory.startGame(viewState).run();
      });
    }

    if (Objects.equals(serverMessage.type, "keyPressed")) {
      // get the key code from the message, then add it to the game controls using the view state's game manager
    }
  }
}
