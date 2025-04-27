package oogasalad.engine.model.event.outcome;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;
import oogasalad.engine.model.event.CollisionHandler;
import oogasalad.engine.model.object.GameObject;
import oogasalad.exceptions.BlueprintParseException;
import oogasalad.exceptions.EventParseException;
import oogasalad.exceptions.GameObjectParseException;
import oogasalad.exceptions.HitBoxParseException;
import oogasalad.exceptions.LayerParseException;
import oogasalad.exceptions.LevelDataParseException;
import oogasalad.exceptions.PropertyParsingException;
import oogasalad.exceptions.SpriteParseException;

/**
 * Represents colliding with solid wall behavior, not letting object move through object of type wall
 * likely associated with player object
 *
 * @author Gage Garcia
 */
public class WallCollideOutcome implements Outcome {

  private final CollisionHandler collisionHandler;

  public WallCollideOutcome(CollisionHandler collisionHandler) {
    this.collisionHandler = collisionHandler;
  }

  @Override
  public void execute(GameObject gameObject, Map<String, String> stringParameters,
      Map<String, Double> doubleParameters)
      throws LayerParseException, EventParseException, BlueprintParseException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, DataFormatException, LevelDataParseException, PropertyParsingException, SpriteParseException, HitBoxParseException, GameObjectParseException, ClassNotFoundException, InstantiationException {
    List<GameObject> collidedObjects = collisionHandler.getCollisions(gameObject);
    String type = stringParameters.getOrDefault("type", "wall");
    for (GameObject collidedObject : collidedObjects) {
      if (collidedObject.getType().equals(type)) {
        if (trySnapToTop(gameObject, collidedObject)) {
          // snapped & grounded—don’t do further separation
          return;
        }

        // 2) otherwise do normal AABB separation
        separateAlongShortestAxis(gameObject, collidedObject);
      }
    }

  }

  /**
   * Exactly your platform-land code, but returns true if we snapped.
   */
  private boolean trySnapToTop(GameObject player, GameObject block) {
    int pBot = player.getYPosition() + player.getHitBoxHeight();
    int pTop = player.getYPosition();
    int bTop = block.getYPosition();

    double vy = player.getYVelocity();
    boolean falling = vy >= 0;
    boolean vertOverlap = pBot >= bTop && pTop < bTop;
    boolean horizOverlap =
        player.getXPosition() + player.getHitBoxWidth() > block.getXPosition() &&
            player.getXPosition() < block.getXPosition() + block.getHitBoxWidth();

    if (falling && vertOverlap && horizOverlap) {
      // snap onto top of block
      player.setYPosition(bTop - player.getHitBoxHeight() - 1);
      player.setYVelocity(0);
      player.setGrounded(true);
      return true;
    }
    return false;
  }

  /**
   * Your AABB “push‐out” logic for walls and ceilings.
   */
  private void separateAlongShortestAxis(GameObject player, GameObject block) {
    double px = player.getXPosition(), py = player.getYPosition();
    double pw = player.getHitBoxWidth(), ph = player.getHitBoxHeight();
    double bx = block.getXPosition(), by = block.getYPosition();
    double bw = block.getHitBoxWidth(), bh = block.getHitBoxHeight();

    double overlapX = Math.min(px + pw, bx + bw) - Math.max(px, bx);
    double overlapY = Math.min(py + ph, by + bh) - Math.max(py, by);

    if (overlapY <= overlapX) {
      // ceiling/floor
      if (py < by) {
        // hit ceiling underside
        player.setYPosition((int) (py - overlapY));
      } else {
        // push floor‐side from below
        player.setYPosition((int) (py + overlapY));
      }
      player.setYVelocity(0);
    } else {
      // walls
      if (px < bx) {
        player.setXPosition((int) (px - overlapX));
      } else {
        player.setXPosition((int) (px + overlapX));
      }
      player.setXVelocity(0);
    }
  }
}