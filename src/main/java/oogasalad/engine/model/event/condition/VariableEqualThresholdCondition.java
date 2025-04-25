package oogasalad.engine.model.event.condition;

import java.util.Map;
import java.util.Objects;
import oogasalad.engine.model.object.GameObject;

/**
 * Returns true if amount is exactly equal to threshold
 *
 * @author Gage Garcia
 */
public class VariableEqualThresholdCondition implements Condition {

  @Override
  public boolean isMet(GameObject gameObject, Map<String, String> stringParams,
      Map<String, Double> doubleParams) {
    String variableName = stringParams.get("variable");
    double amount = gameObject.getDoubleParams().getOrDefault(variableName, 0.0);
    double threshold = doubleParams.get("threshold");
    double cushion = 0.05;
    return amount - cushion <= threshold && amount + cushion >= threshold;
  }
}
