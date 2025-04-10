package oogasalad.engine.view;

import java.io.FileNotFoundException;
import java.util.List;
import oogasalad.engine.model.object.ViewObject;
import oogasalad.engine.view.util.ViewObjectToImageConverter;
import oogasalad.exceptions.RenderingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is the view for a level in a game. It includes all visual elements in a level.
 *
 * @author Aksel Bell
 */
public class LevelDisplay extends Display {

  // has a background and foreground
  // need to store all the game objects and render all of them
  // talks to the camera API to show a certain part of the screen
  // upon update will rerender any objects with the IDs specified
  private static final Logger LOG = LogManager.getLogger();
  private final ViewObjectToImageConverter myConverter;

  /**
   * Default constructor for a level view. Sets the level to pause.
   */
  public LevelDisplay() {
    myConverter = new ViewObjectToImageConverter();
  }

  /**
   * @see Display#initialRender()
   */
  @Override
  public void initialRender() {
    LOG.info("Rendering level...");
  }

  /**
   * Re-renders all game objects that have been updated in the backend.
   *
   * @param gameObjects a list of gameObjects with objects to be updated visually
   * @throws RenderingException thrown if there is an error while rendering
   */
  public void renderGameObjects(List<ViewObject> gameObjects)
      throws RenderingException, FileNotFoundException {
    List<ObjectImage> sprites = myConverter.convertObjectsToImages(gameObjects);
    for (ObjectImage sprite : sprites) {
      this.getChildren().add(sprite.getImageView());
      this.getChildren().add(sprite.getHitBox());
    }
  }

  /**
   * Shifts the camera view focused on this level view.
   *
   * @param myCamera             a camera instance which the node should shift relative to.
   * @param cameraObjectToFollow a central ViewObject to follow.
   */
  @Override
  public void shiftNode(Camera myCamera, ViewObject cameraObjectToFollow) {
    myCamera.updateCamera(this, cameraObjectToFollow);
  }

}
