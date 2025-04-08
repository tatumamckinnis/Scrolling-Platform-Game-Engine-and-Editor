/**
 * Game manager api implementation
 */
package oogasalad.engine.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.zip.DataFormatException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.util.Duration;

import oogasalad.engine.controller.api.GameControllerAPI;
import oogasalad.engine.controller.api.GameManagerAPI;
import oogasalad.engine.controller.api.InputProvider;
import oogasalad.engine.controller.api.LevelAPI;
import oogasalad.exceptions.InputException;
import oogasalad.exceptions.RenderingException;
import oogasalad.exceptions.ViewInitializationException;
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.view.DefaultView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Default implementation of the {@link GameManagerAPI} and {@link InputProvider}.
 * <p>
 * This class is responsible for orchestrating the high-level control of the game, including:
 * <ul>
 *   <li>Initializing and controlling the game loop</li>
 *   <li>Interfacing with the {@link GameControllerAPI} and {@link LevelAPI}</li>
 *   <li>Handling user inputs</li>
 *   <li>Triggering the view rendering process</li>
 * </ul>
 * It manages the currently selected game and level, and communicates between the model and view layers.
 */
public class DefaultGameManager implements GameManagerAPI, InputProvider {

  private static final Logger LOG = LogManager.getLogger();
  private static final ResourceBundle GAME_MANAGER_RESOURCES = ResourceBundle.getBundle(
      DefaultGameManager.class.getPackageName() + "." + "GameManager");

  private Timeline myGameLoop;
  private List<GameObject> myGameObjects;
  private GameControllerAPI myGameController;
  private LevelAPI myLevelAPI;
  private DefaultView myView;
  private static List<KeyCode> currentKeysPressed;
  private String[] currentLevel;

  /**
   * Constructs a DefaultGameManager instance, initializing the game loop, game controller, level
   * loader, and the initial view. Also sets up the default level tracking state.
   *
   * @throws ViewInitializationException if the view fails to initialize
   */
  public DefaultGameManager() throws ViewInitializationException {
    myGameLoop = initGameLoop();
    myGameController = new DefaultGameController(this);
    myLevelAPI = new DefaultLevel(myGameController);
    currentLevel = new String[3]; // consider updating to default level selection
    initializeMyView();
  }

  /**
   * Starts the game loop, allowing the game to progress frame-by-frame.
   */
  @Override
  public void playGame() {
    myGameLoop.play();
  }

  /**
   * Pauses the ongoing game loop execution.
   */
  @Override
  public void pauseGame() {
    myGameLoop.pause();
  }

  /**
   * Restarts the currently selected game level by reloading it from the LevelAPI.
   *
   * @throws DataFormatException       if the level data format is invalid
   * @throws IOException               if there's an error accessing level files
   * @throws ClassNotFoundException    if dynamic loading fails
   * @throws InvocationTargetException if method calls via reflection fail
   * @throws NoSuchMethodException     if expected methods are missing
   * @throws InstantiationException    if instantiating game objects fails
   * @throws IllegalAccessException    if access to game constructors is denied
   */
  @Override
  public void restartGame()
      throws DataFormatException, IOException, ClassNotFoundException,
      InvocationTargetException, NoSuchMethodException,
      InstantiationException, IllegalAccessException {
    if (currentLevel[0] != null) {
      myLevelAPI.selectGame(currentLevel[0], currentLevel[1], currentLevel[2]);
      playGame();
    }
  }

  /**
   * Selects a new game by specifying game name, category, and level.
   *
   * @param game     the name of the game
   * @param category the category of the level
   * @param level    the specific level identifier
   * @throws DataFormatException       if the level data format is invalid
   * @throws IOException               if file reading fails
   * @throws ClassNotFoundException    if game classes are not found
   * @throws InvocationTargetException if level construction fails via reflection
   * @throws NoSuchMethodException     if necessary constructors are missing
   * @throws InstantiationException    if instantiation fails
   * @throws IllegalAccessException    if access to game resources is denied
   */
  @Override
  public void selectGame(String game, String category, String level)
      throws DataFormatException, IOException, ClassNotFoundException,
      InvocationTargetException, NoSuchMethodException,
      InstantiationException, IllegalAccessException {
    currentLevel[0] = game;
    currentLevel[1] = category;
    currentLevel[2] = level;
    myLevelAPI.selectGame(game, category, level);
  }

  /**
   * Lists all available levels for the current game context.
   *
   * @return list of level names
   */
  @Override
  public List<String> listLevels() {
    return myLevelAPI.listLevels();
  }

  /**
   * Checks whether the specified key is currently being pressed.
   *
   * @param keyCode the key to check
   * @return true if pressed, false otherwise
   */
  @Override
  public boolean isKeyPressed(KeyCode keyCode) {
    return currentKeysPressed.contains(keyCode);
  }

  /**
   * Executes a single frame of the game loop. This includes updating inputs, processing the game
   * state, and rendering updated objects to the screen.
   *
   * @throws RenderingException    if rendering fails
   * @throws InputException        if input cannot be read
   * @throws FileNotFoundException if resources are missing
   */
  private void step() throws RenderingException, InputException, FileNotFoundException {
    updateInputList();
    myGameController.updateGameState();
    // TODO: hardcoding view object UUID for now... Fix to make it pulled from XML file
    myView.renderGameObjects(
        myGameController.getImmutableObjects(),
        myGameController.getViewObjectByUUID("e816f04c-3047-4e30-9e20-2e601a99dde8")
    );
  }

  /**
   * Refreshes the list of keys currently being pressed using the view’s input system.
   *
   * @throws InputException if the input cannot be retrieved
   */
  private void updateInputList() throws InputException {
    currentKeysPressed = myView.getCurrentInputs();
  }

  /**
   * Initializes and displays the main view for the game.
   *
   * @throws ViewInitializationException if setup fails
   */
  private void initializeMyView() throws ViewInitializationException {
    Stage primaryStage = new Stage();
    myView = new DefaultView(primaryStage, this);
    myView.initialize();
    primaryStage.setScene(myView.getCurrentScene());
    primaryStage.show();
  }

  /**
   * Sets up the game loop using a frame rate specified in a resource bundle.
   *
   * @return a configured Timeline object that drives the game loop
   */
  private Timeline initGameLoop() {
    Timeline gameLoop = new Timeline();
    gameLoop.setCycleCount(Timeline.INDEFINITE);
    double framesPerSecond = Double.parseDouble(
        GAME_MANAGER_RESOURCES.getString("framesPerSecond"));
    double secondDelay = 1.0 / framesPerSecond;

    gameLoop.getKeyFrames().add(new KeyFrame(Duration.seconds(secondDelay), e -> {
      try {
        step();
      } catch (RenderingException | InputException | FileNotFoundException ex) {
        throw new RuntimeException(ex);
      }
    }));

    return gameLoop;
  }
}