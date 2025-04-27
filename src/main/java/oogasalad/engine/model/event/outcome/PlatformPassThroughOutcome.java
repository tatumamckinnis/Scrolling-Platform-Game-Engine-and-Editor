package oogasalad.engine.model.event.outcome;

import java.util.List;
import java.util.Map;
import oogasalad.engine.model.event.CollisionHandler;
import oogasalad.engine.model.object.GameObject;

/**
 * Outcome that lets object go through platform from below but stay on from above
 *
 * @author Gage Garcia
 */

public class PlatformPassThroughOutcome implements Outcome {

  private final CollisionHandler collisionHandler;

  /**
   * requires a collision handler
   *
   * @param collisionHandler interface that gives access to current state of collisions
   */
  public PlatformPassThroughOutcome(CollisionHandler collisionHandler) {
    this.collisionHandler = collisionHandler;
  }

  @Override
  public void execute(GameObject player,
      Map<String, String> stringParameters,
      Map<String, Double> doubleParameters) {

    String type = stringParameters.getOrDefault("type", "platform");
    List<GameObject> collisions = collisionHandler.getCollisions(player);
    for (GameObject platform : collisions) {
      if (platform.getType().equals(type)) {
        if (trySnapToPlatform(player, platform)) {
          player.setGrounded(true); // Re-ground if standing on valid platform
        }
      }
    }
  }

  private boolean trySnapToPlatform(GameObject player, GameObject platform) {
    int playerBottom = player.getYPosition() + player.getHitBoxHeight();
    int playerTop = player.getYPosition();
    int platformTop = platform.getYPosition();

    double yVelocity = player.getYVelocity();

    boolean isFalling = yVelocity >= 0;
    boolean verticallyOverlapping = playerBottom >= platformTop && playerTop < platformTop;
    boolean horizontallyOverlapping =
        player.getXPosition() + player.getHitBoxWidth() > platform.getXPosition() &&
            player.getXPosition() < platform.getXPosition() + platform.getHitBoxWidth();

    if (isFalling && verticallyOverlapping && horizontallyOverlapping) {
      player.setYPosition(platformTop - player.getHitBoxHeight() + 1);
      player.setYVelocity(0);
      return true;
    }
    return false;
  }

}