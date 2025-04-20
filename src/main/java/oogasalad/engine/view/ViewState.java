package oogasalad.engine.view;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import oogasalad.engine.controller.api.GameManagerAPI;

/**
 * Holds the internal state needed to manage the current view of the application, such as stage,
 * current display, input keys, and game manager reference. Allows for toggling between
 * screens/states by external classes such as the factory package.
 * <p>
 * Acts as a central, mutable state object that can be shared across internal view components. Is
 * only accessible by the factory package to encapsulate the View's state from external view
 * packages.
 * <p>
 * @author Aksel Bell, Luke Nam
 */
public class ViewState {

  private static final String ALLOWED_CLASS_NAME_1 = "oogasalad.engine.view.factory.ButtonActionFactory";
  private static final String ALLOWED_CLASS_NAME_2 = "oogasalad.engine.view.components.NewGameComponents"; // TODO add these to a list, or add play button in new game to the factory
  private static final Logger LOG = LogManager.getLogger();
  private final Stage myStage;
  private final GameManagerAPI myGameManager;
  private final DefaultView myDefaultView;

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
   * Sets the current inputs. Only accessible from the allowed class.
   *
   * @param currentInputs list of key codes to set the current inputs to.
   */
  public void setCurrentInputs(List<KeyCode> currentInputs, List<KeyCode> releasedInputs) {
    checkClassCaller();
    myDefaultView.setReleasedInputs(releasedInputs);
    myDefaultView.setCurrentInputs(currentInputs);
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
   * ChatGPT helped write the first two lines of the method to trace the stack thread.
   */
  private static void checkClassCaller() {
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    for (StackTraceElement element : stackTrace) {
      if (element.getClassName().equals(ALLOWED_CLASS_NAME_1) || element.getClassName()
          .equals(ALLOWED_CLASS_NAME_2)) {
        return;
      }
    }
    LOG.warn("Class does not have access to variable bridge.");
    throw new SecurityException(
        "Access denied: Only " + ALLOWED_CLASS_NAME_1 + " may call this method.");
  }
}
