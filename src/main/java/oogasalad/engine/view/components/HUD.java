package oogasalad.engine.view.components;

import javafx.scene.text.Text;
import oogasalad.engine.model.object.ViewObject;
import oogasalad.engine.view.Display;

public class HUD extends Display {


  @Override
  public void initialRender() {
    Text playerScore = new Text("Score: " + "1000");
  }

}
