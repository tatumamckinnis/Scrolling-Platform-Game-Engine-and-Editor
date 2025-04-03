package oogasalad.engine.view;

import java.io.FileNotFoundException;
import java.security.Key;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import oogasalad.engine.controller.DefaultGameManager;
import oogasalad.engine.controller.GameManagerAPI;
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
  private final GameManagerAPI gameManager;
  private final List<KeyCode> currentInputs = new ArrayList<>();
  private static final Logger LOG = LogManager.getLogger();

  /**
   * Constructor to initialize the GameAppView with a Stage reference.
   */
  public GameAppView(Stage stage, GameManagerAPI gameManager) throws ViewInitializationException {
    this.currentStage = stage;
    this.gameManager = gameManager;
  }

  /**
   * @see GameAppView#initialize()
   */
  @Override
  public void initialize() throws ViewInitializationException {
    SplashScreen splashScreen = new SplashScreen(gameManager);

    splashScreen.setOnStartClicked(() -> { // TODO should also pass in functions for other splash screen buttons buttons
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
  }

  /**
   * @see GameAppView#renderGameObjects(List)
   */
  @Override
  public void renderGameObjects(List<GameObject> gameObjects)
      throws RenderingException, FileNotFoundException {
    currentDisplay.renderGameObjects(gameObjects);
  }

  /**
   * @see GameAppAPI#getCurrentInputs()
   */
  public List<KeyCode> getCurrentInputs() throws InputException {
    return Collections.unmodifiableList(currentInputs);
  }

  /**
   * Starts a game by setting the current scene to a game scene.
   * Defines functions for button routing.
   * @throws ViewInitializationException if errors with initialization.
   */
  private void startGame() throws ViewInitializationException {
    GameScene game = new GameScene();

    game.setControlButtonsClicked(() -> { // TODO needs to set all other buttons in this function
          try {
            goToHome();
          } catch (ViewInitializationException e) {
            LOG.warn("Failed to start game: " + e.getMessage());
          }
        });
    gameManager.playGame();

    currentDisplay = game;
    currentDisplay.render();
    currentScene.setRoot(currentDisplay);

    int newWidth = 500; // Change this to actual GameScene width
    int newHeight = 500; // Change this to actual GameScene height
    currentStage.setWidth(newWidth);
    currentStage.setHeight(newHeight);

    currentScene.setOnKeyPressed(event -> {
      KeyCode keyCode = event.getCode(); // Store KeyCode instead of int
      System.out.println("AHHHHHHHHHHHHHHHH KEYS PRESSED: " + keyCode);
      if (!currentInputs.contains(keyCode)) {
        currentInputs.add(keyCode);
      }
    });

    currentScene.setOnKeyReleased(event -> {
      KeyCode keyCode = event.getCode();
      currentInputs.remove(keyCode); // Remove by KeyCode instead of integer
    });
  }

  /**
   * Returns any view to the homepage.
   */
  private void goToHome() throws ViewInitializationException {
    initialize();
    currentStage.setScene(currentScene);
//    currentStage.setWidth(currentScene.getWidth());
//    currentStage.setHeight(currentScene.getHeight());
  }

  /**
   * @return the current game scene.
   */
  public Scene getCurrentScene() {
    return currentScene;
  }
}
