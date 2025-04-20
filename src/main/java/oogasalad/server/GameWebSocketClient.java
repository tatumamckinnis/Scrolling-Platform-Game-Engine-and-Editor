package oogasalad.server;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class GameWebSocketClient extends WebSocketClient {

  public GameWebSocketClient(String serverUri) throws URISyntaxException {
    super(new URI(serverUri));
  }

  @Override
  public void onOpen(ServerHandshake handshake) {
    send("Hello world!");
  }

  @Override
  public void onMessage(String message) {
    System.out.println("Received from server: " + message);
    // Parse JSON and update game state accordingly
  }

  @Override
  public void onClose(int code, String reason, boolean remote) {
    System.out.println("Connection closed: " + reason);
  }

  @Override
  public void onError(Exception ex) {
    ex.printStackTrace();
  }
}
