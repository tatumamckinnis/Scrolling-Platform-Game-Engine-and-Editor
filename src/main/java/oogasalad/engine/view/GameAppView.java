package oogasalad.engine.view;

import java.util.List;
import javafx.scene.Scene;
import oogasalad.engine.exception.InputException;
import oogasalad.engine.exception.RenderingException;
import oogasalad.engine.exception.ViewInitializationException;
import oogasalad.engine.model.object.GameObject;

/**
 * This class represents the current view that a user sees. It implements the GameAppView API
 *
 * @author Aksel Bell
 */
public class GameAppView implements GameAppAPI {
  private Display currentDisplay;
  private Scene currentScene;

  @Override
  public void initialize(String title) throws ViewInitializationException {
    SplashScreen splashScreen = new SplashScreen();

    splashScreen.setOnStartClicked(() -> {
      try {
        startGame();
      } catch (ViewInitializationException e) {
        System.err.println("Failed to start game: " + e.getMessage()); // TODO change to log
      }
    });

    splashScreen.render();
    currentDisplay = splashScreen;
    int width = splashScreen.getSplashWidth();
    int height = splashScreen.getSplashHeight();
    currentScene = new Scene(currentDisplay, width, height);
  }

  @Override
  public void renderGameObjects(List<GameObject> gameObjects) throws RenderingException {
    currentDisplay.renderGameObjects(gameObjects);
  }

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
  }

  /**
   * Returns the current scene. Called by the GameManager to set the starting Scene to be displayed.
   */
  public Scene getCurrentScene() {
    return currentScene;
  }
}
