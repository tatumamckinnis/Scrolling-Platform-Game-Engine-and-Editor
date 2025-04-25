package oogasalad.engine.view;

import java.io.FileNotFoundException;
import java.util.List;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.engine.view.camera.Camera;
import oogasalad.engine.view.components.GameControlPanel;
import oogasalad.engine.view.components.HUD;
import oogasalad.engine.view.components.NewGameComponents;
import oogasalad.exceptions.RenderingException;

/**
 * This class is the view for an active game. It manages rendering the game scene, displaying UI
 * elements, and handling updates.
 *
 * @author Aksel Bell
 */
public class GameDisplay extends Display {

  private final ViewState myViewState;
  private final GameControlPanel myGameControlPanel;
  private final HUD myHUD;
  private final NewGameComponents myNewGameComponents;
  private final LevelDisplay myLevelView;

  /**
   * Initializes a game scene object.
   */
  public GameDisplay(ViewState viewState) {
    this.myViewState = viewState;
    this.myGameControlPanel = new GameControlPanel(viewState);
    this.myHUD = new HUD();
    this.myNewGameComponents = new NewGameComponents(viewState);
    this.myLevelView = new LevelDisplay(); //sets background and sets to pause

    this.getChildren().addAll(myGameControlPanel, myLevelView, myNewGameComponents, myHUD);
  }

  /**
   * Updates objects visually based on backend changes.
   *
   * @param gameObjects list of GameObjects to update or to add to the screen.
   * @throws RenderingException thrown if problem during rendering.
   */
  public void renderGameObjects(List<ImmutableGameObject> gameObjects)
      throws RenderingException, FileNotFoundException {
    myLevelView.renderGameObjects(gameObjects);
  }

  /**
   * Shifts the level view focus.
   */
  @Override
  public void shiftNode(Camera myCamera) {
    myLevelView.shiftNode(myCamera);
  }

  @Override
  public void removeGameObjectImage(ImmutableGameObject gameObject) {
    myLevelView.removeGameObjectImage(gameObject);
  }

  @Override
  public void addGameObjectImage(ImmutableGameObject gameObject) {
    myLevelView.addGameObjectImage(gameObject);
  }

  @Override
  public void renderPlayerStats(ImmutableGameObject player) {
    myHUD.renderPlayerStats(player);

  }
}