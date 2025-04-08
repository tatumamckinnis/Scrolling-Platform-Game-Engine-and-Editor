/**
 * removes game object from level
 */
package oogasalad.engine.event.outcome;

import oogasalad.engine.controller.api.GameControllerAPI;
import oogasalad.engine.model.object.GameObject;

public class DestroyObjectOutcome implements Outcome {
    private GameControllerAPI gameControllerAPI;
    public DestroyObjectOutcome(GameControllerAPI gameControllerAPI) {
        this.gameControllerAPI = gameControllerAPI;
    }
    @Override
    public void execute(GameObject obj) {
        gameControllerAPI.destroyGameObject(obj);
    }
}
