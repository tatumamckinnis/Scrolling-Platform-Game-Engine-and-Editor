/**
 * Game manager api implementation
 */
package oogasalad.engine.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.zip.DataFormatException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import oogasalad.engine.model.object.GameObject;
import oogasalad.fileparser.FileParserAPI;
import oogasalad.fileparser.records.LevelData;
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
public class DefaultGameManager implements GameManagerAPI {

  private static final Logger LOG = LogManager.getLogger();
  private static final ResourceBundle GAME_MANAGER_RESOURCES = ResourceBundle.getBundle(
      DefaultGameManager.class.getPackageName() + "." + "GameManager");

  private Timeline myGameLoop;
  private List<GameObject> myGameObjects;
  private GameControllerAPI myGameController;
  private LevelData myLevelData;
  private EngineFileConverterAPI myEngineFile;
  private FileParserAPI myFileParser;
  private LevelAPI myLevelAPI;

  /**
   * Constructs a new DefaultGameManager with the given file engine and game controller.
   *
   * @param engineFile     the engine file API implementation
   * @param gameController the game controller to manage game objects and state
   */
  public DefaultGameManager(DefaultEngineFileConverter engineFile, DefaultGameController gameController) {
    myGameLoop = initGameLoop();
    myEngineFile = engineFile;
    myGameController = gameController;
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
    double secondDelay = 1.0 / framesPerSecond;
    gameLoop.getKeyFrames().add(new KeyFrame(Duration.seconds(secondDelay), e -> step()));
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
   * Restarts the game loop.
   * <p>Note: Implementation details TBD. Currently stops the game loop.
   */
  @Override
  public void restartGame() {
    myGameLoop.stop();
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
    myLevelAPI.selectGame(game, category, level);
  }

  /**
   * Returns the internal {@link Timeline} game loop.
   *
   * @return the game loop timeline
   */
  public Timeline getGameLoop() {
    return myGameLoop;
  }

  /**
   * Called on each tick of the game loop. Delegates game state updates to the controller.
   */
  private void step() {
    myGameController.updateGameState();
  }
}