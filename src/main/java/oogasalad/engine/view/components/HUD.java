package oogasalad.engine.view.components;

import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import oogasalad.ResourceManager;
import oogasalad.ResourceManagerAPI;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.engine.model.object.ImmutablePlayer;
import oogasalad.engine.view.Display;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The {@code HUD} class represents a Heads-Up Display in the game view. It is responsible for
 * rendering player statistics on screen and is a type of {@link Display}.
 *
 * <p>This class does not support game object images and throws an exception if such operations are
 * attempted.
 *
 * @author Alana Zinkin
 */
public class HUD extends Display {

  /**
   * Resource manager for fetching localized exception and display text.
   */
  private static final ResourceManagerAPI resourceManager = ResourceManager.getInstance();
  private static final Logger LOG = LogManager.getLogger();
  private Pane container;
  private String HUDStylesheet;

  /**
   * Constructs a new {@code HUD} instance and initializes its layout and components.
   */
  public HUD() {
    initialize();
    String HUDStylesheetFilepath = resourceManager.getConfig("engine.view.hud",
        "hud.stylesheet");
    HUDStylesheet = Objects.requireNonNull(getClass().getResource(HUDStylesheetFilepath)).toExternalForm();
  }

  /**
   * Initializes the HUD layout and prepares its visual container.
   */
  private void initialize() {
    container = new VBox();
    container.setTranslateX(20);
    container.setLayoutX(100);
    this.getChildren().add(container);
  }

  /**
   * This display does not support removal of individual game object images.
   *
   * @param gameObject the game object attempted to be removed (unused)
   * @throws UnsupportedOperationException always thrown with a localized message
   */
  @Override
  public void removeGameObjectImage(ImmutableGameObject gameObject) {
    throw new UnsupportedOperationException(
        resourceManager.getText("exceptions", "CannotRemoveGameObjectImage"));
  }

  @Override
  public void addGameObjectImage(ImmutableGameObject gameObject) {
    throw new UnsupportedOperationException(
        resourceManager.getText("exceptions", "CannotAddGameObjectImage"));
  }

  /**
   * Renders the statistics of the given player to the HUD. It attempts to localize the stat labels
   * using the resource manager.
   *
   * @param player the {@link ImmutableGameObject} representing the player
   */
  @Override
  public void renderPlayerStats(ImmutableGameObject player) {
    container.getChildren().clear();
    Map<String, String> displayedStats = ((ImmutablePlayer) player).getDisplayedStatsMap();
    for (String stat : displayedStats.keySet()) {
      String localizedStat;
      try {
        localizedStat = resourceManager.getText("displayedText", stat);
      } catch (MissingResourceException e) {
        LOG.error("{}{}", resourceManager.getText("exceptions", "StatCannotBeTranslated"),
            e.getMessage());
        localizedStat = stat;
      }
      Text statText = new Text(String.format("%s: %s", localizedStat, displayedStats.get(stat)));
      styleStats(statText);
      container.getChildren().add(statText);
    }
  }

  private void styleStats(Text statText) {
    container.getStylesheets().add(HUDStylesheet);
    LOG.info("Applying style class: " + resourceManager.getConfig("engine.view.hud", "hud.stats.style"));
    statText.getStyleClass().add(resourceManager.getConfig("engine.view.hud", "hud.stats.style"));
  }
}
