package oogasalad.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class GameWebSocketClient extends WebSocketClient {
  private static final Logger LOG = LogManager.getLogger();

  public GameWebSocketClient(String serverUri) throws URISyntaxException {
    super(new URI(serverUri));
  }

  @Override
  public void onOpen(ServerHandshake handshake) {
    send("Hello world!");
  }

  @Override
  public void onMessage(String message) {
    LOG.info("Received from server: {}", message);
    // Parse JSON and update game state accordingly
  }

  @Override
  public void onClose(int code, String reason, boolean remote) {
    LOG.info("Connection closed: {}", reason);
  }

  @Override
  public void onError(Exception ex) {
    LOG.warn("Connection error {}", ex.getMessage());  }
}
