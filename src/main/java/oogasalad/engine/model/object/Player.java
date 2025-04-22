package oogasalad.engine.model.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import oogasalad.engine.model.event.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The {@code Player} class represents a user-controlled or central game character within the game.
 *
 * <p>It extends the abstract {@link GameObject} class and adds player-specific behavior and
 * properties such as power-ups and visible/hidden stats. While players are typically controlled via
 * keyboard input, this is not strictly required and depends on game design.
 *
 * <p>This class distinguishes between stats that are shown to the user (displayedStats)
 * and internal metrics used for logic or balance (hiddenStats).
 *
 * @author Alana Zinkin
 */
public class Player extends GameObject implements ImmutablePlayer {

  private static final Logger LOG = LogManager.getLogger();
  private List<String> currentPowerUps;

  private List<String> displayedStats;

  private Map<String, Double> hiddenStats;

  /**
   * Constructs a new Player object with the provided data.
   *
   * @param uuid           unique identifier for this player
   * @param type           string representing the type/category of the player
   * @param layer          rendering layer for display ordering
   * @param xVelocity      initial horizontal velocity
   * @param yVelocity      initial vertical velocity
   * @param hitBox         spatial hitbox definition for collisions
   * @param spriteInfo     visual rendering and animation data
   * @param events         list of events associated with the player
   * @param displayedStats stats shown to the user (e.g., health)
   * @param stringParams   string-based parameters passed to the player
   * @param doubleParams   numeric parameters passed to the player (e.g., jump strength)
   */
  public Player(UUID uuid, String type, int layer, double xVelocity, double yVelocity,
      HitBox hitBox, Sprite spriteInfo, List<Event> events, List<String> displayedStats,
      Map<String, String> stringParams, Map<String, Double> doubleParams) {
    super(uuid, type, layer, xVelocity, yVelocity, hitBox, spriteInfo, events, stringParams,
        doubleParams);
    currentPowerUps = new ArrayList<>();
    this.displayedStats = displayedStats;
    this.hiddenStats = new HashMap<>();
  }

  /**
   * Returns a map of the stats that are displayed to the player.
   *
   * @return a map of visible stat names and values
   */
  @Override
  public Map<String, String> getDisplayedStatsMap() {
    Map<String,String> displayedStatsMap = new HashMap<>();
    for (String stat : displayedStats) {
      if (getDoubleParams().containsKey(stat)) {
        LOG.info("Displayed stat: " + stat + " = " + getDoubleParams().get(stat));
        displayedStatsMap.put(stat,String.valueOf(getDoubleParams().get(stat).intValue()));
      }
      else if (getStringParams().containsKey(stat)) {
        displayedStatsMap.put(stat,getStringParams().get(stat));
      }
    }
    return displayedStatsMap;
  }

  public List<String> getDisplayedStats() {
    return displayedStats;
  }

  /**
   * sets the value of the current stat to the value provided
   * @param stat stat to display
   * @param value the value the stat is set to
   */
//  public void setDisplayedStat(String stat, double value) {
//    displayedStats.put(stat, value);
//  }

  /**
   * Returns a map of hidden stats used for internal calculations.
   *
   * @return a map of hidden stat names and values
   */
  public Map<String, Double> getHiddenStats() {
    return hiddenStats;
  }

  /**
   * Adds a new power-up to the player's list of active power-ups.
   *
   * @param powerUp the name of the power-up to add
   */
  public void addPowerUp(String powerUp) {
    currentPowerUps.add(powerUp);
  }

  @Override
  public void setNeedsFlipped(boolean needsFlipped) {
    this.getSpriteInfo().setNeedsFlipped(needsFlipped);
  }
}

