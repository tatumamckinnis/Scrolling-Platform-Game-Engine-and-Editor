package oogasalad.engine.view;

import javafx.scene.Group;
import oogasalad.engine.model.object.ViewObject;

/**
 * The {@code Camera} interface defines behavior for updating the view of the game world.
 * Implementations of this interface can modify the visual representation of the game world, such as
 * panning or zooming, typically to follow a specific object.
 */
public interface Camera {

  /**
   * Updates the camera's view based on the provided game world and the object to follow.
   *
   * @param gameWorld      the root group representing the current game scene or world
   * @param objectToFollow the {@link ViewObject} that the camera should track or center on
   */
  void updateCamera(Group gameWorld, ViewObject objectToFollow);
}

