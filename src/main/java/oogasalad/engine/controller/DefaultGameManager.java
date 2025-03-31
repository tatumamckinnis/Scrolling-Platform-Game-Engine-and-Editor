/**
 * Game manager api implementation
 */
package oogasalad.engine.controller;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.zip.DataFormatException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import oogasalad.game.file.parser.records.LevelData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DefaultGameManager implements GameManager {

  private static final Logger LOG = LogManager.getLogger();
  private static final ResourceBundle GAME_MANAGER_RESOURCES = ResourceBundle.getBundle(
      DefaultGameManager.class.getPackageName() + "." + "GameManager");

  private Timeline myGameLoop;
  private GameController myGameController;
  private LevelData myLevelData;
  private EngineFileAPI myEngineFile;

  /**
   * Constructor for initializing a new Game Manager
   */
  public DefaultGameManager(DefaultEngineFile engineFile, DefaultGameController gameController) {
    myGameLoop = initGameLoop();
    myEngineFile = engineFile;
    myGameController = gameController;
  }

  /**
   * initializes a new Timeline by setting CycleCount to indefinite and retrieving the frames per
   * second from the Game Manager resource bundle
   *
   * @return a new Timeline
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

  @Override
  public void playGame() {
    myGameLoop.play();
  }

  @Override
  public void pauseGame() {
    myGameLoop.pause();
  }

  //TODO: add implementation details for restarting a game and defining what restarting a game looks like
  @Override
  public void restartGame() {
    myGameLoop.stop();
  }

  @Override
  public void loadLevel(String level) throws DataFormatException, IOException {
    myLevelData = myEngineFile.loadFileToEngine();
  }

  /**
   * retrieves the game loop object
   *
   * @return Timeline game loop object
   */
  public Timeline getGameLoop() {
    return myGameLoop;
  }

  /**
   * Is called by the game loop timeline on each "tick" of the game by updating the front-end and
   * back-end simultaneously
   */
  private void step() {
    myGameController.updateGameState();
  }
}
