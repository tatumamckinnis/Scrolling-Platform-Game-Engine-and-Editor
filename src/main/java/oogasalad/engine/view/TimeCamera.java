package oogasalad.engine.view;

import javafx.scene.Group;
import oogasalad.engine.controller.ViewObject;

public class TimeCamera extends Camera {
  private static final double CAMERA_OFFSET_X = 1200 / 2.0;
  private static final double CAMERA_OFFSET_Y = 1000 / 2.0;

  @Override
  public void updateCamera(Group gameWorld, ViewObject objectToFollow) {
    double targetX = objectToFollow.getX();
    double targetY = objectToFollow.getY();
    // Move the game world *opposite* to the object movement
    gameWorld.setTranslateX(CAMERA_OFFSET_X - targetX);
    gameWorld.setTranslateY(CAMERA_OFFSET_Y - targetY);
  }
}
