/**
 * interface representing outcome execution logic
 *
 * @author Gage Garcia
 */
package oogasalad.engine.event.outcome;

import oogasalad.engine.model.object.GameObject;

public interface Outcome {

  void execute(GameObject gameObject);
}
