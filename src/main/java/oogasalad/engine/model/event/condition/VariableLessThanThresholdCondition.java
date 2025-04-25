package oogasalad.engine.model.event.condition;

import java.util.Map;
import oogasalad.engine.model.object.GameObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * returns whether a dynamic amount is <= a specified threshold
 *
 * @author Gage Garcia
 */
public class VariableLessThanThresholdCondition implements Condition {
  private static final Logger LOG = LogManager.getLogger();

  @Override
  public boolean isMet(GameObject gameObject, Map<String, String> stringParams, Map<String, Double> doubleParams) {
    String variableName = stringParams.get("variable");
    Double amount = gameObject.getDoubleParams().getOrDefault(variableName,0.0);
    Double threshold = doubleParams.get("threshold");
    return amount <= threshold;
  }
}
