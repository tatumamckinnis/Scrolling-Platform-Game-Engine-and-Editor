/**
 * Outcome that lets object go through platform from below but stay
 * on from above
 * @author Gage Garcia
 */

package oogasalad.engine.event.outcome;

import oogasalad.engine.controller.api.GameExecutor;
import oogasalad.engine.event.CollisionHandler;
import oogasalad.engine.model.object.GameObject;

import java.util.List;

public class PlatformPassThroughOutcome implements Outcome {
    private final CollisionHandler collisionHandler;
    private GameObject player;
    public PlatformPassThroughOutcome(CollisionHandler collisionHandler) {
        this.collisionHandler = collisionHandler;
    }
    @Override
    public void execute(GameObject player) {
        this.player = player;
        //player.setGrounded(false); // Assume ungrounded until proven otherwise

        List<GameObject> collisions = collisionHandler.getCollisions(player);
        for (GameObject platform : collisions) {
            if (platform.getType().equals("platforms")) {
                if (trySnapToPlatform(platform)) {
                    player.setGrounded(true); // Re-ground if standing on valid platform
                    //break;
                }
            }
        }

    }

    private boolean trySnapToPlatform(GameObject platform) {
        int playerBottom = player.getY() + player.getHitBoxHeight();
        int playerTop = player.getY();
        int platformTop = platform.getY();

        double yVelocity = player.getYVelocity();

        boolean isFalling = yVelocity >= 0;
        boolean verticallyOverlapping = playerBottom >= platformTop && playerTop < platformTop;
        boolean horizontallyOverlapping =
                player.getX() + player.getHitBoxWidth() > platform.getX() &&
                        player.getX() < platform.getX() + platform.getHitBoxWidth();

        if (isFalling && verticallyOverlapping && horizontallyOverlapping) {
            player.setY(platformTop - player.getHitBoxHeight() + 1);
            player.setYVelocity(0);
            return true;
        }

        return false;
    }

}
