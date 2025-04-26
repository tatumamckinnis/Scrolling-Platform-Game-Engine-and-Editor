package oogasalad.engine.view.camera;

import javafx.scene.Group;

/**
 * The {@code Camera} interface defines behavior for updating the view of the game world.
 * Implementations of this interface can modify the visual representation of the game world, such as
 * panning or zooming, typically to follow a specific object.
 *
 * @author Alana Zinkin
 */
public interface Camera {

  /**
   * Updates the camera's view based on the provided game world and the object to follow.
   *
   * @param gameWorld the root group representing the current game scene or world
   */
  void updateCamera(Group gameWorld);

  /**
   * Sets the zoom level of the game world by scaling the {@link Group} node.
   *
   * <p>A zoom level of 1.0 represents the normal scale, values greater than 1.0
   * zoom in (enlarging the view), and values less than 1.0 zoom out (shrinking the view).</p>
   *
   * @param gameWorld the {@link Group} node representing the game world to scale
   */
  void scaleWorld(Group gameWorld);

  /**
   * sets the zoom of the Camera
   * @param zoom the new zoom to set
   */
  void setZoom(double zoom);

  /**
   * sets the camera x offset positions
   * @param x the x position to set to
   */
  void setCameraOffsetX(double x);

  /**
   * sets the camera y offset positions
   * @param y the y position to set to
   */
  void setCameraOffsetY(double y);
}

