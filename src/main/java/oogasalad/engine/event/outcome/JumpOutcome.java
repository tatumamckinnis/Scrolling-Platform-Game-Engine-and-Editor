/**
 * Applies jump movement to the game object
 * @author Gage Garcia
 */
package oogasalad.engine.event.outcome;

import oogasalad.engine.model.object.GameObject;

public class JumpOutcome implements Outcome {
    @Override
    public void execute(GameObject gameObject) {
        int dy = Integer.parseInt(gameObject.getParams().getOrDefault("JumpAmount", "60"));

        // Only allow jumping if the object is on the ground
        if (gameObject.isGrounded()) {
            gameObject.setYVelocity(-dy);
            gameObject.setGrounded(false); // Mark object as airborne
        }
    }
}
