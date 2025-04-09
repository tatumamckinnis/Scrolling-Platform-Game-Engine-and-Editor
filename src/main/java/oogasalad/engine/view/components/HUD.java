package oogasalad.engine.view.components;

import javafx.scene.Group;
import javafx.scene.text.Text;
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.model.object.ViewObject;
import oogasalad.engine.view.Display;
import oogasalad.engine.view.ViewState;

public class HUD extends Display {

  HUD(ViewObject viewObject) {

  }

  @Override
  public void render() {
    Text playerScore = new Text("Score: " + "1000");
  }

}
