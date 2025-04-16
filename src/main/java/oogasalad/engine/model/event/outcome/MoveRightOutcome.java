package oogasalad.engine.model.event.outcome;

import java.util.Map;
import oogasalad.engine.model.object.GameObject;

/**
 * Outcome that moves game object to the right
 *
 * @author Gage Garcia
 */
public class MoveRightOutcome implements Outcome {

  @Override
  public void execute(GameObject gameObject,
      Map<String, String> stringParameters,
      Map<String, Double> doubleParameters) {
    double dx = doubleParameters.getOrDefault("amount", 4.0);
    gameObject.setXPosition((int) (gameObject.getXPosition() + dx));
  }

}
