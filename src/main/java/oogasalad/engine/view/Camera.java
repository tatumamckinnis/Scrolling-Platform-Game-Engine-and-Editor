package oogasalad.engine.view;

import javafx.scene.Group;
import oogasalad.engine.controller.ViewObject;

public interface Camera {

  public void updateCamera(Group gameWorld, ViewObject objectToFollow);
}
