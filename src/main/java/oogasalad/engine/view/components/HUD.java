package oogasalad.engine.view.components;

import java.util.Map;
import java.util.ResourceBundle;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import oogasalad.Main;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.engine.model.object.ImmutablePlayer;
import oogasalad.engine.view.Display;
import oogasalad.engine.view.ViewState;

public class HUD extends Display {

  private static final ResourceBundle EXCEPTIONS = ResourceBundle.getBundle(
      Main.class.getPackage().getName() + "." + "Exceptions");
  private Pane container;

  /**
   * renders the HUD view
   */
  public void initialRender() {
    container = new VBox();
    container.setTranslateX(20);
    container.setLayoutX(100);
    this.getChildren().add(container);
  }

  @Override
  public void removeGameObjectImage(ImmutableGameObject gameObject) {
    throw new UnsupportedOperationException(EXCEPTIONS.getString("CannotRemoveGameObjectImage"));
  }


  @Override
  public void renderPlayerStats(ImmutableGameObject player) {
    container.getChildren().clear();
    Map<String, String> displayedStats = ((ImmutablePlayer) player).getDisplayedStatsMap();
    for (String stat: displayedStats.keySet()) {
      Text statText = new Text(STR."\{stat}: \{displayedStats.get(stat)}");
      container.getChildren().add(statText);
    }
  }

}
