/**
 * Outcome that moves game object to the left
 */
package oogasalad.engine.event.outcome;

import oogasalad.engine.model.object.GameObject;

public class MoveLeftOutcome implements Outcome {
    public void execute(GameObject gameObject) {
        int dx = Integer.parseInt(gameObject.getParams().getOrDefault("MoveRightAmount", "4"));
        gameObject.setX(gameObject.getX() - dx);
    }
}
