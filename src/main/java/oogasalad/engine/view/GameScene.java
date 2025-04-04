package oogasalad.engine.view;

import java.io.FileNotFoundException;
import java.util.List;
import oogasalad.engine.controller.GameManagerAPI;
import oogasalad.engine.controller.ViewObject;
import oogasalad.exceptions.RenderingException;
import oogasalad.engine.view.components.GameControlPanel;
import oogasalad.engine.view.components.HUD;
import oogasalad.engine.view.components.NewGameComponents;

/**
 * This class is the view for an active game. It manages rendering the game scene, displaying UI
 * elements, and handling updates.
 *
 * @author Aksel Bell
 */
public class GameScene extends Display {
  private GameManagerAPI myGameManager;
  private GameControlPanel myGameControlPanel;
  private HUD myHUD;
  private NewGameComponents myNewGameComponents;
  private LevelView myLevelView;

  /**
   * Initializes a game scene object.
   */
  public GameScene(GameManagerAPI gameManager) {
    this.myGameManager = gameManager;
    this.myGameControlPanel = new GameControlPanel();
    this.myHUD = new HUD();
    this.myNewGameComponents = new NewGameComponents(gameManager);
    this.myLevelView = new LevelView(); //sets background and sets to pause
  }

  /**
   * Updates objects visually based on backend changes.
   * @param gameObjects list of GameObjects to update or to add to the screen.
   * @throws RenderingException thrown if problem during rendering.
   */
  public void renderGameObjects(List<ViewObject> gameObjects)
      throws RenderingException, FileNotFoundException {
    myLevelView.renderGameObjects(gameObjects);
  }

  public void setControlButtonsClicked(Runnable function) {
    myGameControlPanel.setOnHomeClicked(function);
  }

  /**
   * @see Display#render()
   */
  @Override
  public void render() {
    this.getChildren().addAll(myHUD, myGameControlPanel, myLevelView, myNewGameComponents);
    myGameControlPanel.render();
    myLevelView.render();
    myNewGameComponents.render();
  }

  /**
   * Shifts the level view focus.
   */
  @Override
  public void shiftNode() {
    myLevelView.shiftNode();
  }
}