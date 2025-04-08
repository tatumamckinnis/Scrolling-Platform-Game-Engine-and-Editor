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
        String platformId = player.getParams().get("Platform_id");
        GameObject platform = gameExecutor.getGameObjectByUUID(platformId);
        //set grounded, remove y velocity
        if (player.getY() + player.getHitBoxHeight() > platform.getY()) {
            player.setGrounded(true);
            player.setYVelocity(0);

        }

    }
}
