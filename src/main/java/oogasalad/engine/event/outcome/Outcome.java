package oogasalad.engine.event.outcome;

import oogasalad.engine.model.object.GameObject;

/**
 * interface representing outcome execution logic
 *
 * @author Gage Garcia
 */
public interface Outcome {

  /**
   * Executes and outcome associated with a game object
   *
   * @param gameObject the specified game object
   */
  void execute(GameObject gameObject);
}
