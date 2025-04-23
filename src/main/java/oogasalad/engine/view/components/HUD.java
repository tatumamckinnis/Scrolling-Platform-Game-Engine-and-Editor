package oogasalad.engine.view.components;

import java.util.Map;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import oogasalad.ResourceManager;
import oogasalad.ResourceManagerAPI;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.engine.model.object.ImmutablePlayer;
import oogasalad.engine.view.Display;

public class HUD extends Display {

  private static final ResourceManagerAPI resourceManager = ResourceManager.getInstance();
  private Pane container;

  public HUD() {
    initialize();
  }

  /**
   * renders the HUD view
   */
  private void initialize() {
    container = new VBox();
    container.setTranslateX(20);
    container.setLayoutX(100);
    this.getChildren().add(container);
  }

  @Override
  public void removeGameObjectImage(ImmutableGameObject gameObject) {
    throw new UnsupportedOperationException(resourceManager.getText("exceptions", "CannotRemoveGameObjectImage"));
  }

  @Override
  public void renderPlayerStats(ImmutableGameObject player) {
    container.getChildren().clear();
    Map<String, String> displayedStats = ((ImmutablePlayer) player).getDisplayedStatsMap();
    for (String stat: displayedStats.keySet()) {
      Text statText = new Text(String.format("%s: %s", stat, displayedStats.get(stat)));
      container.getChildren().add(statText);
    }
  }

}
