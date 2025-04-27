package oogasalad.engine.view.camera;

import javafx.scene.Group;
import oogasalad.ResourceManager;
import oogasalad.ResourceManagerAPI;

/**
 * AutoScrollingCamera provides an automatic scrolling behavior for the game.
 * <p>
 * Every time updateCamera is called (e.g. once per frame in the game loop), the camera moves by a
 * fixed scroll speed. This is achieved by adjusting the translation of the game world.
 *
 * @author Alana Zinkin
 */
public class AutoScrollingCamera implements Camera {

  private static final ResourceManagerAPI resourceManager = ResourceManager.getInstance();

  private double scrollSpeedX;
  private double scrollSpeedY; // Change if vertical scrolling is needed
  // Current accumulated offsets for the camera
  private double xOffset;
  private double yOffset;
  private double zoom;

  /**
   * Updates the camera, causing the game world to scroll automatically. This method should be
   * called at a regular interval (e.g., each frame).
   *
   * @param gameWorld the JavaFX Group representing the game world
   * @throws NullPointerException if gameWorld is null
   */
  @Override
  public void updateCamera(Group gameWorld) {
    if (gameWorld == null) {
      throw new NullPointerException(resourceManager.getText("exceptions", "GameWorldNull"));
    }
    scaleWorld(gameWorld);
    // Increment the current offsets by the scroll speeds.
    xOffset += scrollSpeedX;
    yOffset += scrollSpeedY;
    // Apply the offsets to the game world.
    // Negative translation moves the world in the opposite direction to simulate camera movement.
    gameWorld.setTranslateX(-xOffset);
    gameWorld.setTranslateY(-yOffset);
  }

  @Override
  public void scaleWorld(Group gameWorld) {
    try {
      gameWorld.setScaleX(zoom);
      gameWorld.setScaleY(zoom);
    } catch (Exception e) {
      throw new NullPointerException(
          resourceManager.getText("exceptions", "GameWorldNull") + e.getMessage());
    }

  }

  @Override
  public void setZoom(double zoom) {
    this.zoom = zoom;
  }

  @Override
  public void setCameraOffsetX(double x) {
    this.xOffset = x;
  }

  @Override
  public void setCameraOffsetY(double y) {
    this.yOffset = y;
  }

  /**
   * sets the X scroll speed for the camera
   *
   * @param scrollSpeedX the X scroll speed
   */
  public void setScrollSpeedX(double scrollSpeedX) {
    this.scrollSpeedX = scrollSpeedX;
  }

  /**
   * sets the Y scroll speed for the camera
   *
   * @param scrollSpeedY the Y scroll speed
   */
  public void setScrollSpeedY(double scrollSpeedY) {
    this.scrollSpeedY = scrollSpeedY;
  }

}
