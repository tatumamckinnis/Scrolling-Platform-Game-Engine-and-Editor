package oogasalad.engine.model.object;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import oogasalad.engine.event.Event;

/**
 * The player class represents a player object, which is typically controlled using key inputs (but
 * not required to be) Player's are typically how the user represents their own actions within the
 * game
 *
 * @author Alana Zinkin
 */
public class Entity extends GameObject {
  public Entity(UUID uuid, String type, int layer, double xVelocity, double yVelocity,
      HitBox hitBox, Sprite spriteInfo, List<Event> events, Map<String, String> stringParams, Map<String, Double> doubleParams) {
    super(uuid, type, layer, xVelocity, yVelocity, hitBox, spriteInfo, events, stringParams, doubleParams);
  }
}
