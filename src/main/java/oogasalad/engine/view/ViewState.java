package oogasalad.engine.view;

import java.util.List;

import oogasalad.server.ClientSocket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import oogasalad.engine.controller.api.GameManagerAPI;

/**
 * Holds the internal state needed to manage the current view of the application, such as stage,
 * current display, input keys, and game manager reference. Allows for toggling between
 * screens/states by external classes such as factories.
 * <p>
 * Acts as a central, mutable state object that can be shared across internal view components. Is
 * only accessible by the factory package to encapsulate the View's state from external view
 * packages.
 * <p>
 * @author Aksel Bell, Luke Nam
 */
public class ViewState {

  private static final List<String> ALLOWED_CLASS_NAMES = List.of(
      "oogasalad.engine.view.factory.ButtonActionFactory",
      "oogasalad.server.MessageHandlerFactory"
  );
  private static final Logger LOG = LogManager.getLogger();
  private final Stage myStage;
  private final GameManagerAPI myGameManager;
  private final DefaultView myDefaultView;
  private ClientSocket mySocket;

  /**
   * No-arg constructor initializes all fields to null.
   */
  public ViewState() {
    this(null, null, null);
  }

  /**
   * Constructor that initializes fields with optional parameters.
   */
  public ViewState(Stage stage, GameManagerAPI gameManager, DefaultView defaultView) {
    this.myStage = stage;
    this.myGameManager = gameManager;
    this.myDefaultView = defaultView;
    this.mySocket = null;
  }

  /**
   * Gets the current stage. Only accessible from the allowed class.
   */
  public Stage getStage() {
    checkClassCaller();
    return myStage;
  }

  /**
   * Sets the current display. Only accessible from the allowed class.
   *
   * @param display desired display to change screen to.
   */
  public void setDisplay(Display display) {
    checkClassCaller();
    myDefaultView.setCurrentDisplay(display);
  }

  /**
   * Presses the inputted key. Only accessible from the allowed class.
   *
   * @param keyCode the key to press.
   */
  public void pressKey(KeyCode keyCode) {
    checkClassCaller();
    myDefaultView.pressKey(keyCode);
  }

  /**
   * Releases the inputted key. Only accessible from the allowed class.
   *
   * @param keyCode the key to release.
   */
  public void releaseKey(KeyCode keyCode) {
    checkClassCaller();
    myDefaultView.releaseKey(keyCode);
  }

  /**
   * Gets the current GameManager. Only accessible from the allowed class.
   */
  public GameManagerAPI getGameManager() {
    checkClassCaller();
    return myGameManager;
  }

  /**
   * Gets the current DefaultView. Only accessible from the allowed class.
   */
  public DefaultView getDefaultView() {
    checkClassCaller();
    return myDefaultView;
  }

  /**
   * If there is a client communicating with the server, set the socket here.
   * @param socket client socket.
   */
  public void setMySocket(ClientSocket socket) {
    checkClassCaller();
    mySocket = socket;
  }

  /**
   * Returns the client connected to the server.
   */
  public ClientSocket getMySocket() {
    checkClassCaller();
    return mySocket;
  }

  /**
   * ChatGPT helped write the first two lines of the method to trace the stack thread.
   * This method ensures that external classes cannot call certain methods.
   */
  private static void checkClassCaller() {
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    for (StackTraceElement element : stackTrace) {
      if (ALLOWED_CLASS_NAMES.contains(element.getClassName())) {
        return;
      }
    }
    LOG.warn("Class does not have access to variable bridge.");
    throw new SecurityException(
        "Access denied: Only allowed classes may call this method.");
  }

}
