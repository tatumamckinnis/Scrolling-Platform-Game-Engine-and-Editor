package oogasalad.server;

import java.util.Random;
import oogasalad.engine.view.ViewState;
import oogasalad.engine.view.factory.ButtonActionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is a factory that returns runnable functions for when different messages from the
 * server are received.
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
}
