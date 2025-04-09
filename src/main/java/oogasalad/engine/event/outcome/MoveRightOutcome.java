/**
 * Outcome that moves game object to the right
 */
package oogasalad.engine.event.outcome;

import oogasalad.engine.model.object.GameObject;

public class MoveRightOutcome implements Outcome {

  @Override
  public void execute(GameObject gameObject) {
    double dx = gameObject.getDoubleParams().getOrDefault("MoveRightAmount", 4.0);
    gameObject.setXPosition((int) (gameObject.getXPosition() + dx));
  }

}
