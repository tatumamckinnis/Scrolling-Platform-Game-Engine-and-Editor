package oogasalad.server;

import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import oogasalad.engine.view.ViewState;
import oogasalad.engine.view.factory.ButtonActionFactory;
import oogasalad.engine.view.screen.OnlineLobby;
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
    return () -> {
      Platform.runLater(f.getAction("splashButtonStartEngine"));
    };
  }

  public static Runnable playGame(ViewState viewState) {
    ButtonActionFactory f = new ButtonActionFactory(viewState);
    return () -> {
      Platform.runLater(f.getAction("levelPlayButton"));
    };
  }

  public static Runnable pauseGame(ViewState viewState) {
    ButtonActionFactory f = new ButtonActionFactory(viewState);
    return () -> {
      Platform.runLater(f.getAction("levelPauseButton"));
    };
  }

  public static Runnable restartGame(ViewState viewState) {
    ButtonActionFactory f = new ButtonActionFactory(viewState);
    return () -> {
      Platform.runLater(f.getAction("levelRestartButton"));
    };
  }

  public static Runnable endGame(ViewState viewState) {
    ButtonActionFactory f = new ButtonActionFactory(viewState);
    return () -> {
      Platform.runLater(f.getAction("levelHomeButton"));
    };
  }

  /**
   * Triggered when a client's connection is approved. Switches the screen to a lobby screen.
   * @param viewState the current view state.
   * @return runnable that enters the lobby.
   */
  public static Runnable enterLobby(ViewState viewState) {
    return () -> Platform.runLater(() -> {
      try {
        Stage currentStage = viewState.getStage();
        OnlineLobby lobbyDisplay = new OnlineLobby(viewState.getMySocket().getPlayers(), viewState);
        viewState.setDisplay(lobbyDisplay);
      } catch (Exception e) {
        LOG.error("Error entering lobby", e);
      }
    });
  }

  /**
   * Triggers a press key based on the key a server sends it to press.
   * @param viewState the current view state.
   * @param message the key to press
   * @return runnable function.
   */
  public static Runnable pressKey(ViewState viewState, String message) {
    return () -> Platform.runLater(() -> {
      viewState.pressKey(KeyCode.valueOf(message));
    });
  }

  /**
   * Triggers a released key based on the key a server sends it to press.
   * @param viewState the current view state.
   * @param message the key to release.
   * @return runnable function.
   */
  public static Runnable releaseKey(ViewState viewState, String message) {
    return () -> Platform.runLater(() -> {
      viewState.releaseKey(KeyCode.valueOf(message));
    });
  }
}
