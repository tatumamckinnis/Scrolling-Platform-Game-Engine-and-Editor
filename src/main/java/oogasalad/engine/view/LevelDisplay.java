package oogasalad.engine.view;

import java.io.FileNotFoundException;
import java.util.List;
import oogasalad.ResourceManager;
import oogasalad.ResourceManagerAPI;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.engine.view.camera.Camera;
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

  private static final ResourceManagerAPI resourceManager = ResourceManager.getInstance();

  // has a background and foreground
  // need to store all the game objects and render all of them
  // talks to the camera API to show a certain part of the screen
  // upon update will rerender any objects with the IDs specified
  private static final Logger LOG = LogManager.getLogger();
  private final ViewObjectToImageConverter myConverter;

  private List<ObjectImage> sprites;

  /**
   * Default constructor for a level view. Sets the level to pause.
   */
  public LevelDisplay() {
    myConverter = new ViewObjectToImageConverter();
  }

  /**
   * Re-renders all game objects that have been updated in the backend.
   *
   * @param gameObjects a list of gameObjects with objects to be updated visually
   * @throws RenderingException thrown if there is an error while rendering
   */
  @Override
  public void renderGameObjects(List<ImmutableGameObject> gameObjects)
      throws RenderingException, FileNotFoundException {
    sprites = myConverter.convertObjectsToImages(gameObjects);
    for (ObjectImage sprite : sprites) {
      this.getChildren().add(sprite.getImageView());
    }
  }

  /**
   * Shifts the camera view focused on this level view.
   *
   * @param myCamera a camera instance which the node should shift relative to.
   */
  @Override
  public void shiftNode(Camera myCamera) {
    myCamera.updateCamera(this);
  }

  /**
   * removes a game object image from the scene
   *
   * @param gameObject the Immutable game object to remove from the scene
   */
  @Override
  public void removeGameObjectImage(ImmutableGameObject gameObject) {
    ObjectImage imageToRemove = myConverter.retrieveImageObject(gameObject);
    this.getChildren().remove(imageToRemove.getImageView());
    this.getChildren().remove(imageToRemove.getHitBox());
  }

  @Override
  public void addGameObjectImage(ImmutableGameObject gameObject) {
    ObjectImage imageToAdd = myConverter.retrieveImageObject(gameObject);
    this.getChildren().add(imageToAdd.getImageView());
    this.getChildren().add(imageToAdd.getHitBox());
  }

  @Override
  public void renderPlayerStats(ImmutableGameObject player) {
    throw new UnsupportedOperationException(resourceManager.getText("exceptions", "CannotDisplayPlayerStats"));
  }

}
