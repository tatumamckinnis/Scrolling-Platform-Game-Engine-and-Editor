package oogasalad.engine.model.event.condition;

import oogasalad.engine.model.object.GameObject;

/**
 * returns whether a dynamic amount is >= a specified threshold
 *
 * @author Gage Garcia
 */
public class VariableThresholdCondition implements Condition {

  @Override
  public boolean isMet(GameObject gameObject) {
    double var = gameObject.getDoubleParams().get("dynamic_var");
    double threshold = gameObject.getDoubleParams().get("dynamic_threshold");
    return var >= threshold;
  }
}
