package oogasalad.engine.view;

import java.util.ResourceBundle;
import javafx.scene.Group;
import oogasalad.engine.controller.ViewObject;

public class TimeCamera implements Camera {
  private static final ResourceBundle LEVEL_RESOURCES = ResourceBundle.getBundle(LevelView.class.getPackage().getName() + ".Level");
  private static final double CAMERA_OFFSET_X =  Double.parseDouble(LEVEL_RESOURCES.getString("LevelWidth")) / 2.0;
  private static final double CAMERA_OFFSET_Y = Double.parseDouble(LEVEL_RESOURCES.getString("LevelHeight")) / 2.0;

  @Override
  public void updateCamera(Group gameWorld, ViewObject objectToFollow) {
    // Move the game world *opposite* to the object movement
    gameWorld.setTranslateX(CAMERA_OFFSET_X - objectToFollow.getX());
    gameWorld.setTranslateY(CAMERA_OFFSET_Y - objectToFollow.getY());
  }
}
