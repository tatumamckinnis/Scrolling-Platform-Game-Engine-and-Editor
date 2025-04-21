package oogasalad.engine.model.event.condition;

import java.util.List;
import java.util.Map;
import oogasalad.engine.model.event.CollisionHandler;
import oogasalad.engine.model.object.GameObject;

/**
 * Condition that's met if collided with object of a given type
 *
 * @author Gage Garcia
 */
public class CollisionCondition implements Condition {

  private final CollisionHandler collisionHandler;

  /**
   * @param collisionHandler interface that gives access to currently colliding object
   */
  public CollisionCondition(CollisionHandler collisionHandler) {
    this.collisionHandler = collisionHandler;
  }

  @Override
  public boolean isMet(GameObject gameObject, Map<String, String> stringParams, Map<String, Double> doubleParams) {
    String collidedGroup = stringParams.get("group");
    List<GameObject> collidedObjects = collisionHandler.getCollisions(gameObject);
    for (GameObject collidedObject : collidedObjects) {
      if (collidedObject.getType().equals(collidedGroup)) {
        return true;
      }
    }
    return false;
  }
}
