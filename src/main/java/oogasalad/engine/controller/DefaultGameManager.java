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
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.view.DefaultView;
import oogasalad.exceptions.BlueprintParseException;
import oogasalad.exceptions.EventParseException;
import oogasalad.exceptions.GameObjectParseException;
import oogasalad.exceptions.HitBoxParseException;
import oogasalad.exceptions.InputException;
import oogasalad.exceptions.LayerParseException;
import oogasalad.exceptions.LevelDataParseException;
import oogasalad.exceptions.PropertyParsingException;
import oogasalad.exceptions.RenderingException;
import oogasalad.exceptions.SpriteParseException;
import oogasalad.exceptions.ViewInitializationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Game manager api implementation
 */
public class DefaultGameManager implements GameManagerAPI, InputProvider {

  private static final Logger LOG = LogManager.getLogger();
  private static final ResourceBundle GAME_MANAGER_RESOURCES = ResourceBundle.getBundle(
      DefaultGameManager.class.getPackageName() + "." + "GameManager");
  private final Timeline myGameLoop;
  private List<GameObject> myGameObjects;
  private final GameControllerAPI myGameController;
  private final LevelAPI myLevelAPI;
  private DefaultView myView;
  private static List<KeyCode> currentKeysPressed;

  private String currentLevel;

  /**
   * default constructor for the game manager
   * @throws ViewInitializationException if the view cannot render
   */
  public DefaultGameManager()
      throws ViewInitializationException {
    myGameLoop = initGameLoop();
    myGameController = new DefaultGameController(this, this);
    myLevelAPI = new DefaultLevel(myGameController);
    initializeMyView();
  }


  @Override
  public void playGame() {
    myGameLoop.play();
  }


  @Override
  public void pauseGame() {
    myGameLoop.pause();
  }


  @Override
  public void restartGame()
      throws DataFormatException, IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, LayerParseException, LevelDataParseException, PropertyParsingException, SpriteParseException, EventParseException, HitBoxParseException, BlueprintParseException, GameObjectParseException {
    if (!(currentLevel == null)) {
      myLevelAPI.selectGame(currentLevel);
      playGame();
    }
  }


  @Override
  public void selectGame(String filePath)
      throws DataFormatException, IOException, ClassNotFoundException, InvocationTargetException,
      NoSuchMethodException, InstantiationException, IllegalAccessException, LayerParseException, LevelDataParseException, PropertyParsingException, SpriteParseException, EventParseException, HitBoxParseException, BlueprintParseException, GameObjectParseException {
    currentLevel = filePath;
    myLevelAPI.selectGame(filePath);
  }

  @Override
  public List<String> listLevels() {
    return myLevelAPI.listLevels();
  }

  @Override
  public boolean isKeyPressed(KeyCode keyCode) {
    return currentKeysPressed.contains(keyCode);
  }

  /**
   * @see GameManagerAPI#displayGameObjects()
   */
  @Override
  public void displayGameObjects() throws RenderingException, FileNotFoundException {
    myView.renderGameObjects(myGameController.getImmutableObjects(),
        myGameController.getViewObjectByUUID("e816f04c-3047-4e30-9e20-2e601a99dde8"));
  }

  /**
   * @see GameManagerAPI#endGame() (String, GameObject)
   */
  @Override
  public void endGame() {
    pauseGame();
    //myView.renderEndGameScreen(text, gameObject);
  }

  private void step()
      throws RenderingException, InputException, IOException, LayerParseException, EventParseException, BlueprintParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, DataFormatException, LevelDataParseException, PropertyParsingException, SpriteParseException, HitBoxParseException, GameObjectParseException, ClassNotFoundException, InstantiationException {
    updateInputList();
    myGameController.updateGameState();
    // TODO: hardcoding view object UUID for now... Fix to make it pulled from XML file
    myView.renderGameObjects(myGameController.getImmutableObjects(),
        myGameController.getViewObjectByUUID("e816f04c-3047-4e30-9e20-2e601a99dde8"));
  }

  private void updateInputList() throws InputException {
    currentKeysPressed = myView.getCurrentInputs();
  }

  private void initializeMyView() throws ViewInitializationException {
    Stage primaryStage = new Stage();
    myView = new DefaultView(primaryStage, this);
    myView.initialize();
    primaryStage.setScene(myView.getCurrentScene());
    primaryStage.show();
  }

  private Timeline initGameLoop() {
    Timeline gameLoop = new Timeline();
    gameLoop.setCycleCount(Timeline.INDEFINITE);
    double framesPerSecond = Double.parseDouble(
        GAME_MANAGER_RESOURCES.getString("framesPerSecond"));
    double secondDelay = 1.0 / (framesPerSecond);
    gameLoop.getKeyFrames().add(new KeyFrame(Duration.seconds(secondDelay), e -> {
      try {
        step();
      } catch (RenderingException | InputException | IOException | LayerParseException |
               EventParseException | BlueprintParseException | InvocationTargetException |
               NoSuchMethodException | IllegalAccessException | DataFormatException |
               LevelDataParseException | PropertyParsingException | SpriteParseException |
               HitBoxParseException | GameObjectParseException | ClassNotFoundException |
               InstantiationException ex) {
        throw new RuntimeException(ex);
      }
    }));
    return gameLoop;
  }

}