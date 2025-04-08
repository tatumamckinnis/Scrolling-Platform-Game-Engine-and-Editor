/**
 * Outcome that lets object go through platform from below but stay
 * on from above
 * @author Gage Garcia
 */

package oogasalad.engine.event.outcome;

import oogasalad.engine.controller.api.GameExecutor;
import oogasalad.engine.model.object.GameObject;

public class PlatformPassThroughOutcome implements Outcome {
    private final GameExecutor gameExecutor;
    public PlatformPassThroughOutcome(GameExecutor gameExecutor) {
        this.gameExecutor = gameExecutor;
    }
    @Override
    public void execute(GameObject player) {
        String platformId = "e0373626-9e90-4ed3-9863-39e287926802";
        GameObject platform = gameExecutor.getGameObjectByUUID(platformId);

        int playerBottom = player.getY() + player.getHitBoxHeight();
        int playerTop = player.getY();
        int platformTop = platform.getY();
        int platformBottom = platform.getY() + platform.getHitBoxHeight();
        double yVelocity = player.getYVelocity();

        boolean isFalling = yVelocity >= 0;
        boolean verticallyOverlapping = playerBottom >= platformTop && playerTop < platformTop;
        boolean horizontallyOverlapping =
                player.getX() + player.getHitBoxWidth() > platform.getX() &&
                        player.getX() < platform.getX() + platform.getHitBoxWidth();

        if (isFalling && verticallyOverlapping && horizontallyOverlapping) {
            // Snap player to platform top
            player.setY(platformTop - player.getHitBoxHeight());
            player.setYVelocity(0);
            player.setGrounded(true);
        } else {
            // Only unground if player is clearly no longer on the platform
            if (player.getY() + player.getHitBoxHeight() < platformTop - 2 || !horizontallyOverlapping) {
                player.setGrounded(false);
            }
        }
    }

}
