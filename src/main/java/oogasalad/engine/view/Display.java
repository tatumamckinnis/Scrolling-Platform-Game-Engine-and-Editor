package oogasalad.engine.view;

import java.io.FileNotFoundException;
import java.util.List;
import javafx.scene.Group;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.engine.model.object.ImmutablePlayer;
import oogasalad.engine.view.camera.Camera;
import oogasalad.exceptions.RenderingException;

/**
 * This is an abstract class representing any visual component in the game. It will be extended by
 * all classes that need to be displayed in the scene.
 *
 * @author Aksel Bell
 */
public abstract class Display extends Group {

  /**
   * Allows a Display to render game objects onto the screen. Default implementation does nothing.
   * Subclasses (like GameScene) can override this if needed.
   */
  public void renderGameObjects(List<ImmutableGameObject> gameObjects)
      throws RenderingException, FileNotFoundException {
  }

  public void renderGameObjectHitBoxes(List<ImmutableGameObject> gameObjects) throws FileNotFoundException {
    ;
  }

  /**
   * Special implementation of javafx setTranslate() function such that some types of nodes can
   * implement the shift or choose not to. Default implementation chooses not to shift node. For
   * example, a splash screen should never be able to be translated but a levelView should.
   *
   * @param camera a camera instance which the node should shift relative to.
   */
  public void shiftNode(Camera camera) {
  }

  /**
   * This method will provide the option to hide the display from the scene. Subclasses can define
   * how they should be hidden.
   */
  public void hide() {
    this.setVisible(false);
  }

  /**
   * This method will make the display visible again.
   */
  public void show() {
    this.setVisible(true);
  }

  /**
   * removes game object from the level view scene
   *
   * @param gameObject the game object that should be removed
   */
  public abstract void removeGameObjectImage(ImmutableGameObject gameObject);

  /**
   * renders the player's score, lives, and other displayed statistics
   * @param player game objects of the player type
   */
  public abstract void renderPlayerStats(ImmutableGameObject player);
}