package oogasalad.engine.model.event.condition;

import java.util.List;
import oogasalad.engine.model.event.CollisionHandler;
import oogasalad.engine.model.object.GameObject;

/**
 * Condition that's met if collided with object of a given type
 *
 * @author Gage Garcia
 */
public class CollisionCondition implements Condition {

  private final CollisionHandler collisionHandler;
  private final String collidedGroup;

  /**
   * @param collisionHandler interface that gives access to currently colliding object
   * @param collidedGroup    String name of group to check collisions for
   */
  public CollisionCondition(CollisionHandler collisionHandler, String collidedGroup) {
    this.collisionHandler = collisionHandler;
    this.collidedGroup = collidedGroup;
  }

  @Override
  public boolean isMet(GameObject gameObject) {
    List<GameObject> collidedObjects = collisionHandler.getCollisions(gameObject);
    for (GameObject collidedObject : collidedObjects) {
      if (collidedObject.getType().equals(collidedGroup)) {
        return true;
      }
    }
    return false;
  }
}
