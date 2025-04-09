package oogasalad.engine.model.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import oogasalad.engine.event.Event;

public class Player extends GameObject {

  private List<String> currentPowerUps;
  private Map<String, Double> displayedStats;
  private Map<String, Double> hiddenStats;

  public Player(UUID uuid, String type, int layer, double xVelocity, double yVelocity,
      HitBox hitBox, Sprite spriteInfo, List<Event> events, Map<String, Double> displayedStats, Map<String, String> stringParams, Map<String, Double> doubleParams) {
    super(uuid, type, layer, xVelocity, yVelocity, hitBox, spriteInfo, events, stringParams, doubleParams);
    currentPowerUps = new ArrayList<>();
    this.displayedStats = displayedStats;
    this.hiddenStats = new HashMap<>();
  }

  public Map<String, Double> getDisplayedStats() {
    return displayedStats;
  }

  public Map<String, Double> getHiddenStats() {
    return hiddenStats;
  }

  public void addPowerUp(String powerUp) {
    currentPowerUps.add(powerUp);
  }
}
