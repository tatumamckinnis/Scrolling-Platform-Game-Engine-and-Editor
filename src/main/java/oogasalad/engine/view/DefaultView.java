package oogasalad.engine.view;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import oogasalad.engine.controller.api.GameManagerAPI;
import oogasalad.engine.model.object.ViewObject;
import oogasalad.engine.view.screen.SplashScreen;
import oogasalad.exceptions.InputException;
import oogasalad.exceptions.RenderingException;
import oogasalad.exceptions.ViewInitializationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class represents the current view that a user sees. It implements the GameAppView API
 *
 * @author Aksel Bell
 */
public class DefaultView implements ViewAPI {

  private static final Logger LOG = LogManager.getLogger();
  private static final ResourceBundle GAME_APP_VIEW_RESOURCES = ResourceBundle.getBundle(
      DefaultView.class.getPackage().getName() + "." + "Level");
  private static final int LEVEL_WIDTH = Integer.parseInt(
      GAME_APP_VIEW_RESOURCES.getString("LevelWidth"));
  private static final int LEVEL_HEIGHT = Integer.parseInt(
      GAME_APP_VIEW_RESOURCES.getString("LevelHeight"));

  private Display currentDisplay;
  private Scene currentScene;
  private final Stage currentStage;
  private final GameManagerAPI gameManager;
  private List<KeyCode> currentInputs;
  private final Camera myCamera;

  /**
   * Constructor to initialize the GameAppView with a Stage reference.
   */
  public DefaultView(Stage stage, GameManagerAPI gameManager) throws ViewInitializationException {
    this.currentStage = stage;
    this.gameManager = gameManager;
    this.myCamera = new TimeCamera();
    currentScene = new Scene(new Group(), LEVEL_WIDTH, LEVEL_HEIGHT);
    currentInputs = new ArrayList<>();
  }

  /**
   * @see DefaultView#initialize()
   */
  @Override
  public void initialize() throws ViewInitializationException {
    ViewState currentState = new ViewState(currentStage, gameManager, this);
    SplashScreen splashScreen = new SplashScreen(currentState);

    splashScreen.initialRender();
    int width = splashScreen.getSplashWidth();
    int height = splashScreen.getSplashHeight();
    currentDisplay = splashScreen;

    currentScene = new Scene(currentDisplay, width, height);
  }

  /**
   * @see DefaultView#renderGameObjects(List, ViewObject)
   */
  @Override
  public void renderGameObjects(List<ViewObject> gameObjects, ViewObject cameraObjectToFollow)
      throws RenderingException, FileNotFoundException {
    currentDisplay.renderGameObjects(gameObjects);
    currentDisplay.shiftNode(myCamera, cameraObjectToFollow);
  }

  /**
   * @see ViewAPI#getCurrentInputs()
   */
  public List<KeyCode> getCurrentInputs() throws InputException {
    return Collections.unmodifiableList(currentInputs);
  }

  /**
   * @return the current game scene. Public method because the controller must call it to set the
   * scene initially.
   */
  public Scene getCurrentScene() {
    return currentScene;
  }

  /**
   * Set the current display.
   */
  void setCurrentDisplay(Display display) {
    currentDisplay = display;
    currentScene.setRoot(currentDisplay);
  }

  /**
   * Set the current inputs.
   *
   * @param currentInputs an arraylist to point to.
   */
  void setCurrentInputs(List<KeyCode> currentInputs) {
    this.currentInputs = currentInputs;
  }
}
