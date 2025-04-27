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
        preventObjectMovement(gameObject, collidedObject);
      }
    }

  }

  private void preventObjectMovement(GameObject player, GameObject block) {
    // fetch positions & sizes
    double px = player.getXPosition();
    double py = player.getYPosition();
    double pw = player.getHitBoxWidth();
    double ph = player.getHitBoxHeight();

    double bx = block.getXPosition();
    double by = block.getYPosition();
    double bw = block.getHitBoxWidth();
    double bh = block.getHitBoxHeight();

    // compute overlaps
    double overlapX = Math.min(px + pw, bx + bw) - Math.max(px, bx);
    double overlapY = Math.min(py + ph, by + bh) - Math.max(py, by);

    // decide which axis to resolve
    if (overlapY <= overlapX) {
      // vertical resolution (ceiling or floor)
      int playerBottom = (int)(py + ph);
      int playerTop    = (int) py;
      int blockTop     = (int) by;
      double vy        = player.getYVelocity();
      boolean falling  = vy >= 0;
      boolean vertOK   = playerBottom >= blockTop && playerTop < blockTop;
      boolean horizOK  = (px + pw) > bx && px < (bx + bw);

      if (falling && vertOK && horizOK) {
        // **snap** to the top surface
        player.setYPosition(blockTop - (int)ph + 1);
        player.setYVelocity(0);
        return;
      }

      // else push out (ceiling or floor)
      if (py < by) {
        // hit the ceiling side
        player.setYPosition((int) (py - overlapY));
      } else {
        // hit the floor side (from below)
        player.setYPosition((int) (py + overlapY));
      }
      player.setYVelocity(0);

    } else {
      // horizontal resolution (walls)
      if (px < bx) {
        // collided on left side of block
        player.setXPosition((int) (px - overlapX));
      } else {
        // collided on right side
        player.setXPosition((int) (px + overlapX));
      }
      player.setXVelocity(0);
    }
  }
}
