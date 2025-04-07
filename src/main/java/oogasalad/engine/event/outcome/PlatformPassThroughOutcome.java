/**
 * Outcome that lets object go through platform from below but stay
 * on from above
 * @author Gage Garcia
 */

package oogasalad.engine.event.outcome;

import oogasalad.engine.controller.api.GameControllerAPI;
import oogasalad.engine.model.object.GameObject;

import java.util.UUID;

public class PlatformPassThroughOutcome implements Outcome {
    private GameControllerAPI gameControllerAPI;
    public PlatformPassThroughOutcome(GameControllerAPI gameControllerAPI) {
        this.gameControllerAPI = gameControllerAPI;
    }
    @Override
    public void execute(GameObject player) {
        String platformId = player.getParams().get("Platform_id");
        GameObject platform = gameControllerAPI.getGameObjectByUUID(platformId);
        //set grounded, remove y velocity
        if (player.getY() + player.getHitBoxHeight() > platform.getY()) {
            player.setGrounded(true);
            player.setYVelocity(0);

        }

    }
}
