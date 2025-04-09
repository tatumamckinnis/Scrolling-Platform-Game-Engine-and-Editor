/**
 * Outcome that applies gravity to the game object
 * @author Gage Garcia
 */
package oogasalad.engine.event.outcome;

import oogasalad.engine.model.object.GameObject;

public class GravityOutcome implements Outcome {
    @Override
    public void execute(GameObject gameObject) {
        double dy = gameObject.getDoubleParams().getOrDefault("ApplyGravityAmount", 5.0);

        // Only apply gravity if the object is in the air (falling or jumping)
        if (!gameObject.isGrounded()) {
            gameObject.setYVelocity(gameObject.getYVelocity() + dy);
        }
    }
}
