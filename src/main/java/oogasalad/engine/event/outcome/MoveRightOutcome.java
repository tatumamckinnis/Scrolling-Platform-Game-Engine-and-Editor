/**
 * Outcome that moves game object to the right
 */
package oogasalad.engine.event.outcome;

import oogasalad.engine.model.object.GameObject;

public class MoveRightOutcome implements Outcome {
    @Override
    public void execute(GameObject gameObject) {
        int dx = Integer.parseInt(gameObject.getParams().getOrDefault("MoveRightAmount", "2"));
        gameObject.setX(gameObject.getX() + dx);
    }
}
