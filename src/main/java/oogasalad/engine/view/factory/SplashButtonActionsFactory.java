package oogasalad.engine.view.factory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import oogasalad.engine.controller.GameManagerAPI;
import oogasalad.engine.exception.ViewInitializationException;
import oogasalad.engine.view.Display;
import oogasalad.engine.view.GameAppAPI;
import oogasalad.engine.view.GameAppView;
import oogasalad.engine.view.GameScene;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class returns the desired function for a specific button.
 *
 * @author Aksel Bell
 */
public class SplashButtonActionsFactory {
  private static final Logger LOG = LogManager.getLogger();
  private static final String buttonIDToActionFilePath = "ENTER_PATH";
  private static final Properties buttonIDToActionProperties = new Properties();
  private final GameAppAPI gameView;

  /**
   * Loads property file map of buttonIDs to Actions.
   */
  public SplashButtonActionsFactory(GameAppAPI gameView) {
    try {
      InputStream stream = getClass().getResourceAsStream(buttonIDToActionFilePath);
      buttonIDToActionProperties.load(stream);
    } catch (IOException e) {
      LOG.warn("Unable to load button action properties");
    }
    this.gameView = gameView;
  }

  /**
   * Returns the corresponding runnable function for the specified button
   * @param buttonID string of the button's unique ID
   * @return runnable function for the button's onClick action
   */
  public Runnable getAction(String buttonID) {
    String methodName = buttonIDToActionProperties.getProperty(buttonID);

//    try {
//      Method method = SplashButtonActionsFactory.class.getDeclaredMethod(methodName);
//      method.setAccessible(true);
//
//      return method.invoke();
//    } catch (Exception e) {
//      throw new RuntimeException("Failed to return function: " + methodName, e);
//    }
    return null;
  }

  /**
   * Start button on the home page.
   * @throws ViewInitializationException thrown if error initializing the view.
   */
  public void startGame() throws ViewInitializationException {
    GameAppView view = (GameAppView) gameView;
    GameManagerAPI manager = view.getGameManager();
    Scene currentScene = view.getCurrentScene();
    Stage currentStage = view.getCurrentStage();
    List<KeyCode> currentInputs = new ArrayList<>();

    GameScene game = new GameScene(manager);
    manager.playGame();

    game.render();
    currentScene.setRoot(game);

    int newWidth = 500; // Change this to actual GameScene width
    int newHeight = 500; // Change this to actual GameScene height
    currentStage.setWidth(newWidth);
    currentStage.setHeight(newHeight);

    currentScene.setOnKeyPressed(event -> {
      KeyCode keyCode = event.getCode(); // Store KeyCode instead of int
      if (!currentInputs.contains(keyCode)) {
        currentInputs.add(keyCode);
      }
    });

    currentScene.setOnKeyReleased(event -> {
      KeyCode keyCode = event.getCode();
      currentInputs.remove(keyCode); // Remove by KeyCode instead of integer
    });
  }

  // other ones will be take no parameters
}
