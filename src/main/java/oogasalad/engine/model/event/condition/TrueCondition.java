package oogasalad.engine.model.event.condition;

import java.util.Map;
import oogasalad.engine.model.object.GameObject;

/**
 * Condition that is always true
 *
 * @author Billy McCune
 */
public class TrueCondition implements Condition {

  @Override
  public boolean isMet(GameObject gameObject, Map<String, String> stringParams, Map<String, Double> doubleParams){
    return true;
  }
}