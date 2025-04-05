package oogasalad.engine.view;

import javafx.scene.Group;
import oogasalad.engine.controller.ViewObject;

public abstract class Camera {

  public abstract void updateCamera(Group gameWorld, ViewObject objectToFollow);
}
