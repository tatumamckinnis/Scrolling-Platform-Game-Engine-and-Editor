package oogasalad.engine.event.outcome;

import oogasalad.engine.model.object.GameObject;

public class MoveLeftOutcome implements Outcome {

  @Override
  public void execute(GameObject gameObject) {
    double dx = gameObject.getDoubleParams().getOrDefault("MoveLeftAmount", 4.0);
    gameObject.setXPosition((int) (gameObject.getXPosition() - dx));
  }

}
