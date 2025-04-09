/**
 * Outcome that applies gravity to the game object
 * @author Gage Garcia
 */
package oogasalad.engine.event.outcome;

import oogasalad.engine.event.CollisionHandler;
import oogasalad.engine.model.object.GameObject;

import java.util.List;

public class GravityOutcome implements Outcome {
    private CollisionHandler collisionHandler;
    public GravityOutcome(CollisionHandler collisionHandler) {
        this.collisionHandler = collisionHandler;
    }
    @Override
    public void execute(GameObject gameObject) {
        int dy = Integer.parseInt(gameObject.getParams().getOrDefault("ApplyGravityAmount", "5"));
        List<GameObject> collisions = collisionHandler.getCollisions(gameObject);

        if (collisions.isEmpty()) {
            gameObject.setGrounded(false);
        }
        // Only apply gravity if the object is in the air (falling or jumping)
        if (!gameObject.isGrounded()) {
            gameObject.setYVelocity(gameObject.getYVelocity() + dy);
        }
    }
}
