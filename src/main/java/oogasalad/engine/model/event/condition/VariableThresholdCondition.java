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
    String varName = gameObject.getStringParams().get("variable");
    double amount = gameObject.getDoubleParams().get(varName);
    double threshold = gameObject.getDoubleParams().get("threshold");
    return amount >= threshold;
  }
}
