package oogasalad.engine.view;

import java.util.List;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import oogasalad.engine.controller.api.GameManagerAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class provides controlled access to internal components of {@link DefaultView} for trusted
 * internal classes such as factories.
 *
 * It exists to maintain the encapsulation of the {@link DefaultView} instance variables by allowing
 * the {@link DefaultView} to expose its instance variables to classes within the view package.
 *
 * @author Aksel Bell
 */
public class ViewVariableBridge {
  private static final String ALLOWED_CLASS_NAME = "oogasalad.engine.view.factory.ButtonActionFactory";
  private static final Logger LOG = LogManager.getLogger();

  /**
   * @param view a GameAppView whose frontend instance variables are needed.
   * @return the stage of the view.
   */
  public static Stage getStage(DefaultView view) {
    checkClassCaller();
    return view.getCurrentStage();
  }

  public static void setDisplay(DefaultView view, Display display) {
    checkClassCaller();
    view.setCurrentDisplay(display);
  }

  public static void setCurrentInputs(DefaultView view, List<KeyCode> currentInputs) {
    checkClassCaller();
    view.setCurrentInputs(currentInputs);
  }

  /**
   * @param view a GameAppView whose frontend instance variables are needed.
   * @return the game manager of the view.
   */
  public static GameManagerAPI getManager(DefaultView view) {
    checkClassCaller();
    return view.getGameManager();
  }

  /**
   * ChatGPT helped write the first two lines of the method to trace the stack thread.
   */
  private static void checkClassCaller() {
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    for (StackTraceElement element : stackTrace) {
      if (element.getClassName().equals(ALLOWED_CLASS_NAME)) {
        return;
      }
    }
    LOG.warn("Class does not have access to variable bridge.");
    throw new SecurityException("Access denied: Only " + ALLOWED_CLASS_NAME + " may call this method.");
  }

}
