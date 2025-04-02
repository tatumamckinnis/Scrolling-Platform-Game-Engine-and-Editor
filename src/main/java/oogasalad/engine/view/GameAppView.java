package oogasalad.engine.view;

import java.util.List;
import javafx.scene.Scene;
import javafx.stage.Stage;
import oogasalad.engine.exception.InputException;
import oogasalad.engine.exception.RenderingException;
import oogasalad.engine.exception.ViewInitializationException;
import oogasalad.engine.model.object.GameObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class represents the current view that a user sees. It implements the GameAppView API
 *
 * @author Aksel Bell
 */
public class GameAppView implements GameAppAPI {
  private Display currentDisplay;
  private Scene currentScene;
  private final Stage currentStage;
  private static final Logger LOG = LogManager.getLogger();

  /**
   * Constructor to initialize the GameAppView with a Stage reference.
   */
  public GameAppView(Stage stage) {
    this.currentStage = stage;
  }

  /**
   * @see GameAppView#initialize(String)
   */
  @Override
  public void initialize(String title) throws ViewInitializationException {
    SplashScreen splashScreen = new SplashScreen();

    splashScreen.setOnStartClicked(() -> {
      try {
        startGame();
      } catch (ViewInitializationException e) {
       LOG.warn("Failed to start game: " + e.getMessage());
      }
    });

    splashScreen.render();
    int width = splashScreen.getSplashWidth();
    int height = splashScreen.getSplashHeight();
    currentDisplay = splashScreen;

    currentScene = new Scene(currentDisplay, width, height);
    currentStage.setScene(currentScene);
    currentStage.show();
  }

  /**
   * @see GameAppView#renderGameObjects(List)
   */
  @Override
  public void renderGameObjects(List<GameObject> gameObjects) throws RenderingException {
    currentDisplay.renderGameObjects(gameObjects);
  }

  /**
   * @see GameAppView#getCurrentInputs()
   * @return
   * @throws InputException
   */
  @Override
  public List<String> getCurrentInputs() throws InputException {
    return List.of();
  }

  /**
   * Starts a game by setting the current scene to a game scene.
   * @throws ViewInitializationException if errors with initialization.
   */
  private void startGame() throws ViewInitializationException {
    currentDisplay = new GameScene();
    currentDisplay.render();
    currentScene.setRoot(currentDisplay);

    int newWidth = 500; // Change this to actual GameScene width
    int newHeight = 500; // Change this to actual GameScene height

    currentStage.setWidth(newWidth);
    currentStage.setHeight(newHeight);
  }

  /**
   * Returns the current scene. Called by the GameManager to set the starting Scene to be displayed.
   */
  public Scene getCurrentScene() {
    return currentScene;
  }
}
