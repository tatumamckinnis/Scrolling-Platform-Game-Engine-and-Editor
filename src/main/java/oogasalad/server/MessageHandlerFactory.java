package oogasalad.server;

import java.util.Random;
import oogasalad.engine.view.ViewState;
import oogasalad.engine.view.factory.ButtonActionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is a factory that returns runnable functions for when messages from the server are
 * received.
 *
 * @author Aksel Bell
 */
public class MessageHandlerFactory {
  private static final Logger LOG = LogManager.getLogger();

  /**
   * Triggered when server sends a startGame type message. Uses code from the button action factory
   * start method.
   *
   * @param viewState a current viewState to change the scene.
   * @return a runnable that starts the game.
   */
  public static Runnable startGame(ViewState viewState) {
    ButtonActionFactory f = new ButtonActionFactory(viewState);
    return f.getAction("splashButtonStartEngine");
  }

  /**
   * Starts a javascript server.
   * @param gamePath desired server game file path.
   * @param viewState a viewState to allow changing of views.
   * @return a client web socket connected to the server.
   */
  public static ClientSocket startServer(String gamePath, ViewState viewState) {
    try {
      int myPort = generateRandomPort();
      new JavaServer(myPort, gamePath);
      Thread.sleep(1000);
      ClientSocket client = new ClientSocket(myPort, gamePath, viewState);
      client.connect();

      return client;
    } catch (Exception e) {
      LOG.error("Error starting server");
      throw new RuntimeException(e);
    }
  }

  private static int generateRandomPort() {
    Random random = new Random();
    return random.nextInt(3000) + 3000;
  }
}
