package oogasalad.engine.model.object;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import oogasalad.engine.model.event.Event;


/**
 * Represents a general-purpose entity within the game world.
 *
 * <p>{@code Entity} extends {@link GameObject} and serves as a flexible base class
 * for non-player elements in the game such as enemies, NPCs, obstacles, or interactable objects.
 *
 * <p>This class does not add any custom behavior to {@link GameObject} but provides
 * semantic distinction for entities that are not the player.
 *
 * @author Alana Zinkin
 */
public class Entity extends GameObject {

  /**
   * Constructs a new {@code Entity} with the provided data.
   *
   * @param uuid         unique identifier for this entity
   * @param type         type/category of the entity (e.g., "Enemy", "Obstacle")
   * @param layer        render layer for drawing order
   * @param xVelocity    initial horizontal velocity
   * @param yVelocity    initial vertical velocity
   * @param hitBox       spatial boundary used for collisions
   * @param spriteInfo   visual representation and animation data
   * @param events       list of events tied to this entity's behavior
   * @param stringParams map of string-based parameters
   * @param doubleParams map of numerical parameters
   */
  public Entity(UUID uuid, String type, int layer, double xVelocity, double yVelocity,
      HitBox hitBox, Sprite spriteInfo, List<Event> events,
      Map<String, String> stringParams, Map<String, Double> doubleParams) {
    super(uuid, type, layer, xVelocity, yVelocity, hitBox, spriteInfo, events, stringParams,
        doubleParams);
  }
}

