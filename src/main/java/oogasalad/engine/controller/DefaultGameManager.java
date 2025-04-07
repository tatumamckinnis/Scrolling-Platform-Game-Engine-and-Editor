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
import oogasalad.engine.controller.api.LevelAPI;
import oogasalad.exceptions.InputException;
import oogasalad.exceptions.RenderingException;
import oogasalad.exceptions.ViewInitializationException;
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.view.DefaultView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Default implementation of the {@link GameManagerAPI}, responsible for managing the game loop,
 * loading levels, and controlling game flow such as play, pause, and restart.
 *
 * <p>It uses a {@link Timeline} to drive the game loop, relies on a {@link GameControllerAPI} to
 * manage the internal game state, and interacts with file and level APIs to load game content.
 *
 * @author Alana Zinkin
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
  //game, category, level
  private String[] currentLevel;

  /**
   * Constructs a new DefaultGameManager with the given file engine and game controller.
   *
   *
   */
  public DefaultGameManager()
      throws ViewInitializationException {
    myGameLoop = initGameLoop();
    myGameController = new DefaultGameController(this);
    myLevelAPI = new DefaultLevel(myGameController);
    currentLevel = new String[3]; //consider updating to default level selection

    initializeMyView();
  }

  private void initializeMyView() throws ViewInitializationException {
    Stage primaryStage = new Stage();
    myView = new DefaultView(primaryStage, this);
    myView.initialize();
    primaryStage.setScene(myView.getCurrentScene());
    primaryStage.show();
  }


  /**
   * Initializes the game loop using a {@link Timeline} that fires at a regular interval based on
   * the configured frames per second in the resource bundle.
   *
   * @return the initialized game loop timeline
   */
  private Timeline initGameLoop() {
    Timeline gameLoop = new Timeline();
    gameLoop.setCycleCount(Timeline.INDEFINITE);
    double framesPerSecond = Double.parseDouble(
        GAME_MANAGER_RESOURCES.getString("framesPerSecond"));
    double secondDelay = 1.0 / (framesPerSecond);
    gameLoop.getKeyFrames().add(new KeyFrame(Duration.seconds(secondDelay), e -> {
      try {
        step();
      } catch (RenderingException | InputException | FileNotFoundException ex) {
        throw new RuntimeException(ex);
      }
    }));
    return gameLoop;
  }

  /**
   * Starts the game loop.
   */
  @Override
  public void playGame() {
    myGameLoop.play();
  }

  /**
   * Pauses the game loop.
   */
  @Override
  public void pauseGame() {
    myGameLoop.pause();
  }

  /**
   * Restarts the game loop. Requires select level to have been called first to have set the current level variable
   *
   */
  @Override
  public void restartGame() throws DataFormatException, IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    if (!(currentLevel[0] == null)) {
      myLevelAPI.selectGame(currentLevel[0], currentLevel[1], currentLevel[2]);
      playGame();
    }


  }

  /**
   * Selects and loads the specified game based on the game name, category, and level.
   *
   * @param game     the name of the game
   * @param category the game’s category
   * @param level    the specific level to load
   * @throws DataFormatException       if level data is malformed
   * @throws IOException               if there is an issue reading level files
   * @throws ClassNotFoundException    if a class referenced in the data is not found
   * @throws InvocationTargetException if object instantiation fails
   * @throws NoSuchMethodException     if the required constructor is missing
   * @throws InstantiationException    if the object cannot be created
   * @throws IllegalAccessException    if constructor access is restricted
   */
  @Override
  public void selectGame(String game, String category, String level)
      throws DataFormatException, IOException, ClassNotFoundException, InvocationTargetException,
      NoSuchMethodException, InstantiationException, IllegalAccessException {
    currentLevel[0] = game;
    currentLevel[1] = category;
    currentLevel[2] = level;
    myLevelAPI.selectGame(game, category, level);
  }

  /**
   *
   * @return list of available level file strings, currently hardcoded to dinosaurgame folder
   */
  @Override
  public List<String> listLevels() {
    return myLevelAPI.listLevels();
  }

  /**
   *
   * @param  keyCode to check
   * @return if that key is pressed
   */
  public boolean isKeyPressed(KeyCode keyCode) {
    return currentKeysPressed.contains(keyCode);
  }

  /**
   * Called on each tick of the game loop. Delegates game state updates to the controller.
   */
  private void step() throws RenderingException, InputException, FileNotFoundException {
    updateInputList();
    myGameController.updateGameState();
    // TODO: hardcoding view object UUID for now... Fix to make it pulled from XML file
    myView.renderGameObjects(myGameController.getImmutableObjects(), myGameController.getObjectByUUID("e816f04c-3047-4e30-9e20-2e601a99dde8"));
  }

  /**
   * updates list of currently pressed keys using view api
   */
  private void updateInputList() throws InputException {
    currentKeysPressed = myView.getCurrentInputs();
  }

}