package oogasalad.engine.event.condition;

import oogasalad.engine.model.object.GameObject;

/**
 * Condition that is always true
 */
public class TrueCondition implements Condition {

  @Override
  public boolean isMet(GameObject gameObject) {
    return true;
  }
}