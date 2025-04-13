package oogasalad.engine.view.components;

import java.util.ResourceBundle;
import javafx.scene.text.Text;
import oogasalad.Main;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.engine.view.Display;

public class HUD extends Display {
  private static final ResourceBundle EXCEPTIONS = ResourceBundle.getBundle(
      Main.class.getPackage().getName() + "." + "Exceptions");


  @Override
  public void initialRender() {
    Text playerScore = new Text("Score: " + "1000");
  }

  @Override
  public void removeGameObjectImage(ImmutableGameObject gameObject) {
    throw new UnsupportedOperationException(EXCEPTIONS.getString("CannotRemoveGameObjectImage"));
  }

}
