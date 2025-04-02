package oogasalad.engine.view;

import java.util.List;
import oogasalad.engine.exception.RenderingException;
import oogasalad.engine.model.object.GameObject;
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
  private GameControlPanel myGameControlPanel;
  private HUD myHUD;
  private NewGameComponents myNewGameButtons;
  private LevelView myLevelView;

  /**
   * Initializes a game scene object.
   */
  public GameScene() {
    this.myGameControlPanel = new GameControlPanel();
    this.myHUD = new HUD();
    this.myNewGameButtons = new NewGameComponents();
    this.myLevelView = new LevelView(); //sets background and sets to pause
  }

  /**
   * Starts the active game by rendering the initial foreground and setting up gameplay.
   */
  public void playGame() {
    myLevelView.setPlay(true);
  }

  /**
   * Displays the "Game Over" screen with options to play again.
   */
  public void displayPlayAgainMenu() {
    myNewGameButtons.setVisible(true);
  }

  /**
   * Hides the "Game Over" screen and resets for a new game session.
   */
  public void closePlayAgainMenu() {
    myNewGameButtons.setVisible(false);
  }

  /**
   * Updates objects visually based on backend changes.
   * @param gameObjects list of GameObjects to update or to add to the screen.
   * @throws RenderingException thrown if problem during rendering.
   */
  public void renderGameObjects(List<GameObject> gameObjects) throws RenderingException {
    myLevelView.renderGameObjects(gameObjects);
  }

  /**
   * @see Display#render()
   */
  @Override
  public void render() {
    this.getChildren().addAll(myHUD, myGameControlPanel, myLevelView);
    myLevelView.render();
  }
}