package oogasalad.engine.view;

import java.util.List;
import oogasalad.engine.exception.InputException;
import oogasalad.engine.exception.RenderingException;
import oogasalad.engine.exception.ViewInitializationException;
import oogasalad.engine.model.object.GameObject;

/**
 * This class represents the current view that a user sees. It implements the GameAppView API
 *
 * @author Aksel Bell
 */
public class CurrentView implements GameAppView {
  private Display currentDisplay;

  public void startGame() throws ViewInitializationException {
    // called when the start button from splash screen is called
    // changes the current display to GameScene, and renders
    //
  }

  @Override
  public void initialize(String title, int width, int height) throws ViewInitializationException {
    // sets the current scene to a splash screen
    // currentDisplay = new splashScreen();
    // currentDisplay.render(); // need to then add this display to the root
  }

  @Override
  public void renderGameObjects(List<GameObject> gameObjects) throws RenderingException {
    // if the current display is the splash screen, return
    // if the current display is an active game, then call updateObjects(gameObjects) on the currentDisplay
  }

  @Override
  public List<String> getCurrentInputs() throws InputException {
    return List.of();
  }
}
