package oogasalad.engine.model.event.condition;

import oogasalad.engine.model.object.GameObject;

/**
 * Interface representing a condition that either returns true or false
 *
 * @author Gage Garcia
 */
public interface Condition {

  /**
   * @param gameObject -> the game object tied to the event
   * @return whether the condition is met
   */
  boolean isMet(GameObject gameObject);
}
