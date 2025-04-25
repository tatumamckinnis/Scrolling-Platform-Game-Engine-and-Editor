package oogasalad.server;

import java.lang.reflect.Method;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import oogasalad.engine.view.ViewState;
import oogasalad.engine.view.factory.ButtonActionFactory;
import oogasalad.engine.view.screen.OnlineLobby;
import oogasalad.exceptions.InputException;
import oogasalad.exceptions.ViewInitializationException;
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
   * Handles the server message
   *
   * @param viewState current viewstate object
   * @param message   the message to send
   * @return a new Runnable function
   */
  public static Runnable handleMessage(ViewState viewState, ServerMessage message) {
    try {
      Method method = MessageHandlerFactory.class.getDeclaredMethod(message.getType(),
          ViewState.class, ServerMessage.class);
      method.setAccessible(true);

      return (Runnable) method.invoke(null, viewState, message);
    } catch (Exception e) {
      LOG.info("No matching handler found for server message type: {}", message.getType());
      return () -> {
      };
    }
  }

  /**
   * Triggered when server sends a startGame type message. Uses code from the button action factory
   * start method.
   *
   * @param viewState a current viewState to change the scene.
   * @param message   a server message to interpret.
   * @return a runnable that starts the game.
   */
  private static Runnable startGame(ViewState viewState, ServerMessage message) {
    ButtonActionFactory f = new ButtonActionFactory(viewState);
    return () -> {
      try {
        Platform.runLater(f.startGame());
      } catch (ViewInitializationException | InputException e) {
        LOG.error("Error starting game", e);
        throw new RuntimeException(e);
      }
    };
  }

  /**
   * Triggered when a client receives a play game message from the server.
   *
   * @param viewState the current view state to change the scene.
   * @param message   a server message to interpret.
   * @return a runnable that plays the game.
   */
  private static Runnable playGame(ViewState viewState, ServerMessage message) {
    ButtonActionFactory f = new ButtonActionFactory(viewState);
    return () -> {
      Platform.runLater(f.playGame());
    };
  }

  /**
   * Triggered when a client receives a pause game message from the server.
   *
   * @param viewState the current view state.
   * @param message   a server message to interpret.
   * @return a runnable that pauses the game.
   */
  private static Runnable pauseGame(ViewState viewState, ServerMessage message) {
    ButtonActionFactory f = new ButtonActionFactory(viewState);
    return () -> {
      Platform.runLater(f.pauseGame());
    };
  }

  /**
   * Triggered when a client receives a restart game message from the server.
   *
   * @param viewState the current view state.
   * @param message   a server message to interpret.
   * @return a runnable that restarts the game.
   */
  private static Runnable restartGame(ViewState viewState, ServerMessage message) {
    ButtonActionFactory f = new ButtonActionFactory(viewState);
    return () -> {
      Platform.runLater(f.restartGame());
    };
  }

  /**
   * Triggered when a client receives an end game message from the server.
   *
   * @param viewState the current view state.
   * @param message   a server message to interpret.
   * @return a runnable that ends the game.
   */
  private static Runnable goToHome(ViewState viewState, ServerMessage message) {
    ButtonActionFactory f = new ButtonActionFactory(viewState);
    return () -> {
      try {
        Platform.runLater(f.goToHome());
      } catch (ViewInitializationException e) {
        LOG.error("Error closing game", e);
        throw new RuntimeException(e);
      }
    };
  }

  /**
   * Triggered when a client's connection is approved. Switches the screen to an updated lobby
   * screen with number of players.
   *
   * @param viewState the current view state.
   * @param message   a server message to interpret.
   * @return runnable that enters the lobby.
   */
  private static Runnable newConnection(ViewState viewState, ServerMessage message) {
    return () -> Platform.runLater(() -> {
      try {
        OnlineLobby lobbyDisplay = new OnlineLobby(
            Integer.parseInt(message.getMessage()),
            viewState,
            viewState.getMySocket().getLobby());
        viewState.setDisplay(lobbyDisplay);
      } catch (Exception e) {
        LOG.error("Error entering lobby", e);
      }
    });
  }

  /**
   * Triggers a press key based on the key a server sends it to press.
   *
   * @param viewState the current view state.
   * @param message   a server message to interpret.
   * @return runnable function.
   */
  private static Runnable pressKey(ViewState viewState, ServerMessage message) {
    return () -> Platform.runLater(() -> {
      viewState.pressKey(KeyCode.valueOf(message.getMessage()));
    });
  }

  /**
   * Triggers a released key based on the key a server sends it to press.
   *
   * @param viewState the current view state.
   * @param message   a server message to interpret.
   * @return runnable function.
   */
  private static Runnable releaseKey(ViewState viewState, ServerMessage message) {
    return () -> Platform.runLater(() -> {
      viewState.releaseKey(KeyCode.valueOf(message.getMessage()));
    });
  }
}
