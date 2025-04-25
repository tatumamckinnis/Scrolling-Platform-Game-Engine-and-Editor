package oogasalad.engine.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Locale;
import java.util.zip.DataFormatException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.util.Duration;
import oogasalad.ResourceManager;
import oogasalad.ResourceManagerAPI;
import oogasalad.engine.controller.api.GameControllerAPI;
import oogasalad.engine.controller.api.GameManagerAPI;
import oogasalad.engine.controller.api.InputProvider;
import oogasalad.engine.controller.api.LevelAPI;
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.engine.model.object.Player;
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
import oogasalad.fileparser.records.GameObjectData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Game manager api implementation
 */
public class DefaultGameManager implements GameManagerAPI, InputProvider {

  private static final Logger LOG = LogManager.getLogger();
  private static final ResourceManagerAPI resourceManager = ResourceManager.getInstance();

  private final Timeline myGameLoop;
  private final GameControllerAPI myGameController;
  private final LevelAPI myLevelAPI;
  private DefaultView myView;
  private static List<KeyCode> currentKeysPressed;
  private List<KeyCode> currentKeysReleased;
  private String myCurrentGamePath;
  private String currentLevel;

  /**
   * default constructor for the game manager
   * @throws ViewInitializationException if the view cannot render
   */
  public DefaultGameManager()
      throws ViewInitializationException, FileNotFoundException {
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
    myCurrentGamePath = filePath;
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

  @Override
  public boolean isKeyReleased(KeyCode keyCode) {
    return currentKeysReleased.contains(keyCode);
  }

  @Override
  public void clearReleased() {
  currentKeysReleased.clear();
  }

  /**
   * @see GameManagerAPI#displayGameObjects()
   */
  @Override
  public void displayGameObjects() throws RenderingException, FileNotFoundException {
    myView.renderGameObjects(myGameController.getImmutableObjects(), myGameController.getCamera());
  }

  @Override
  public void removeGameObjectImage(ImmutableGameObject gameObject) {
    myView.removeGameObjectImage(gameObject);
  }

  @Override
  public void addGameObjectImage(ImmutableGameObject gameObject) {
    myView.addGameObjectImage(gameObject);
  }

  @Override
  public GameObject makeObjectFromData(GameObjectData gameObjectData) {
    return myLevelAPI.makeObjectFromData(gameObjectData);
  }

  @Override
  public String getCurrentLevel() throws NullPointerException {
    if (currentLevel != null) {
      return currentLevel;
    }
    throw new NullPointerException(resourceManager.getText("exceptions", "currentLevelNull"));
  }

  @Override
  public void setLanguage(String language) {
    String i18nLanguageCode = language.substring(0, 2);
    ResourceManager.getInstance().setLocale(Locale.of(i18nLanguageCode));
    LOG.info("Setting language to {}", language);
  }

  @Override
  public Object getPlayer() {
    return myGameController.getImmutablePlayers().get(0);
  }

  @Override
  public String getCurrentGameName() {
    // Extract game name from the loaded file path
    if (myCurrentGamePath != null && !myCurrentGamePath.isEmpty()) {
      // Handle path like "data/gameData/levels/GameName/level.xml"
      String[] pathParts = myCurrentGamePath.split("/");
      // Find the game name part (usually the second-to-last directory)
      if (pathParts.length >= 2) {
        return pathParts[pathParts.length - 2];
      }
    }
    return "Unknown";
  }

  @Override
  public String getCurrentLevelName() {
    // Extract level name from the loaded file path
    if (myCurrentGamePath != null && !myCurrentGamePath.isEmpty()) {
      // Handle path like "data/gameData/levels/GameName/level.xml"
      String[] pathParts = myCurrentGamePath.split("/");
      // Get the level filename (last part of path)
      if (pathParts.length >= 1) {
        String levelFile = pathParts[pathParts.length - 1];
        // Remove file extension if needed
        return levelFile.replaceAll("\\.xml$", "");
      }
    }
    return "Unknown";
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
    myView.renderGameObjects(myGameController.getImmutableObjects(), myGameController.getCamera());
    renderPlayerStats();
    myView.clearReleasedInputs();
  }

  private void renderPlayerStats() {
    for (ImmutableGameObject immutableGameObject : myGameController.getImmutablePlayers()) {
      myView.renderPlayerStats(immutableGameObject);
    }
  }

  private void updateInputList() throws InputException {
      currentKeysPressed  = myView.getCurrentInputs();
      currentKeysReleased = myView.getReleasedInputs();
  }

  private void initializeMyView() throws ViewInitializationException, FileNotFoundException {
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
        resourceManager.getConfig("engine.controller.gamemanager", "framesPerSecond"));
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