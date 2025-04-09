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
        List<GameObject> collisions = collisionHandler.getCollisions(player);
        for (GameObject platform : collisions) {
            if (platform.getType().equals("platforms")) {
                handlePlatform(platform);
            }
        }

    }

    private void handlePlatform(GameObject platform) {
        int playerBottom = player.getYPosition() + player.getHitBoxHeight();
        int playerTop = player.getYPosition();
        int platformTop = platform.getYPosition();
        int platformBottom = platform.getYPosition() + platform.getHitBoxHeight();
        double yVelocity = player.getYVelocity();

        boolean isFalling = yVelocity >= 0;
        boolean verticallyOverlapping = playerBottom >= platformTop && playerTop < platformTop;
        boolean horizontallyOverlapping =
                player.getXPosition() + player.getHitBoxWidth() > platform.getXPosition() &&
                        player.getXPosition() < platform.getXPosition() + platform.getHitBoxWidth();

        if (isFalling && verticallyOverlapping && horizontallyOverlapping) {
            // Snap player to platform top
            player.setYPosition(platformTop - player.getHitBoxHeight());
            player.setYVelocity(0);
            player.setGrounded(true);
        } else {
            // Only unground if player is clearly no longer on the platform
            if (player.getYPosition() + player.getHitBoxHeight() < platformTop - 2 || !horizontallyOverlapping) {
                player.setGrounded(false);
            }
        }
    }

}
