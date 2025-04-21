package oogasalad.engine.model.object;

import oogasalad.engine.model.event.Event;
import oogasalad.fileparser.records.FrameData;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

  private Player player;
  private UUID uuid;
  private HitBox hitBox;
  private Sprite sprite;
  private List<Event> events;
  private List<String> displayedStats;
  private Map<String, String> stringParams;
  private Map<String, Double> doubleParams;

  @BeforeEach
  void setUp() {
    uuid = UUID.randomUUID();
    hitBox = new HitBox(0, 0, 50, 50);

    FrameData frame = new FrameData("idle", 0, 0, 64, 64);
    Map<String, FrameData> frameMap = new HashMap<>();
    frameMap.put("idle", frame);

    sprite = new Sprite(frameMap, frame, new HashMap<>(), 0, 0, new File("sprite.xml"), 90.0, false);

    events = new ArrayList<>();
    displayedStats = new ArrayList<>();
    displayedStats.add("health");
    displayedStats.add("stamina");

    stringParams = new HashMap<>();
    doubleParams = new HashMap<>();
    doubleParams.put("jumpStrength", 10.0);

    player = new Player(uuid, "player", 1, 0.0, 0.0, hitBox, sprite, events,
        displayedStats, stringParams, doubleParams);
  }

  @Test
  void getDisplayedStatsReturnsCorrectValues() {
    Map<String, String> stats = player.getDisplayedStatsMap();
    assertEquals("2", stats.size());
    assertEquals("100.0", stats.get("health"));
    assertEquals("50.0", stats.get("stamina"));
  }

  @Test
  void getHiddenStatsStartsEmpty() {
    assertTrue(player.getHiddenStats().isEmpty());
  }

  @Test
  void addPowerUpAddsToList() {
    player.addPowerUp("invincibility");

    // Reflection used only because no public getter for currentPowerUps
    try {
      var field = Player.class.getDeclaredField("currentPowerUps");
      field.setAccessible(true);
      List<String> powerUps = (List<String>) field.get(player);
      assertTrue(powerUps.contains("invincibility"));
    } catch (Exception e) {
      fail("Could not access currentPowerUps via reflection: " + e.getMessage());
    }
  }

  @Test
  void getUUID_Basic_constructedPlayerHasCorrectInitialValues() {
    assertEquals(uuid.toString(), player.getUUID());
  }

  @Test
  void getXVelocity_Basic_constructedPlayerHasCorrectInitialValues() {
    assertEquals(0.0, player.getXVelocity());
  }

  @Test
  void getYVelocity_Basic_constructedPlayerHasCorrectInitialValues() {
    assertEquals(0.0, player.getYVelocity());
  }

  @Test
  void getHitBoxWidth_Basic_constructedPlayerHasCorrectInitialValues() {
    assertEquals(50, player.getHitBoxWidth());
  }

  @Test
  void getHitBoxHeight_Basic_constructedPlayerHasCorrectInitialValues() {
    assertEquals(50, player.getHitBoxHeight());
  }

}
