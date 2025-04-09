package oogasalad.engine.event;

import java.util.List;
import oogasalad.engine.model.object.GameObject;

/**
 * Interface that implements collision updating/getting
 *
 * @author Gage Garcia
 */
public interface CollisionHandler {

  /**
   * Updates current state of collisions
   */
  void updateCollisions();

  /**
   * Return list of game object that are currently colliding with a game object
   *
   * @param gameObject the specified object
   * @return list of the colliding game objects
   */
  List<GameObject> getCollisions(GameObject gameObject);

}
