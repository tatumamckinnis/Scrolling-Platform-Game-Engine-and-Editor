package oogasalad.engine.view;

import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import javafx.scene.Group;
import oogasalad.engine.model.object.ViewObject;

/**
 * The {@code TimeCamera} class implements the {@link Camera} interface to simulate a camera that
 * follows a specific {@link ViewObject} in the game world. It adjusts the position of the entire
 * game world to keep the object centered on screen.
 *
 * @author Alana Zinkin
 */
public class TimeCamera implements Camera {

  private static final ResourceBundle LEVEL_RESOURCES = ResourceBundle.getBundle(
      LevelDisplay.class.getPackage().getName() + ".Level");

  /**
   * Horizontal offset to center the camera based on level width.
   */
  private static final double CAMERA_OFFSET_X = Double.parseDouble(
      LEVEL_RESOURCES.getString("LevelWidth")) / 2.0;

  /**
   * Vertical offset to center the camera based on level height.
   */
  private static final double CAMERA_OFFSET_Y = Double.parseDouble(
      LEVEL_RESOURCES.getString("LevelHeight")) / 2.0;

  /**
   * Updates the camera view by translating the game world to center the followed object. The game
   * world is moved in the opposite direction of the object's position, so that the object remains
   * centered on the screen.
   *
   * @param gameWorld      the root {@link Group} representing the entire game scene
   * @param objectToFollow the {@link ViewObject} to be followed by the camera
   */
  @Override
  public void updateCamera(Group gameWorld, ViewObject objectToFollow) {
    if (gameWorld == null) {
      throw new NullPointerException(LEVEL_RESOURCES.getString("GameWorldNull"));
    }
    try {
      gameWorld.setTranslateX(CAMERA_OFFSET_X - objectToFollow.getX());
      gameWorld.setTranslateY(CAMERA_OFFSET_Y - objectToFollow.getY());
    } catch (Exception e) {
      throw new NoSuchElementException(LEVEL_RESOURCES.getString("ObjectDoesntExist"));
    }

  }
}

