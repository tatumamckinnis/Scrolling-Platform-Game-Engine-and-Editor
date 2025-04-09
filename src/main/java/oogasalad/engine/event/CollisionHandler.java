/**
 * Interface that implements collision updating/getting
 *
 * @author Gage Garcia
 */
package oogasalad.engine.event;

import java.util.List;
import oogasalad.engine.model.object.GameObject;

public interface CollisionHandler {

  void updateCollisions();

  List<GameObject> getCollisions(GameObject gameObject);

}
