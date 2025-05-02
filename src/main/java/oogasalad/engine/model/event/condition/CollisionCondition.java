package oogasalad.engine.model.event.condition;

import java.util.List;
import java.util.Map;
import oogasalad.engine.model.event.CollisionHandler;
import oogasalad.engine.model.object.GameObject;
// Import LogManager and Logger
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Condition that's met if collided with object of a given type
 *
 * @author Gage Garcia
 */
public class CollisionCondition implements Condition {

  // Add Logger
  private static final Logger LOG = LogManager.getLogger(CollisionCondition.class);

  private final CollisionHandler collisionHandler;

  /**
   * @param collisionHandler interface that gives access to currently colliding object
   */
  public CollisionCondition(CollisionHandler collisionHandler) {
    this.collisionHandler = collisionHandler;
  }

  @Override
  public boolean isMet(GameObject gameObject, Map<String, String> stringParams, Map<String, Double> doubleParams) {
    // --- DEBUG LOGGING START ---
    LOG.info("Checking CollisionCondition for GameObject ID: {}", gameObject.getUUID()); // Log which object is checking

    String collidedGroup = stringParams.get("group");
    LOG.info(" -> Event Parameter 'group' = '{}'", collidedGroup); // Log the parameter value expected

    if (collidedGroup == null) {
      LOG.warn(" -> 'group' parameter is missing in event definition!");
      return false;
    }

    List<GameObject> collidedObjects = collisionHandler.getCollisions(gameObject);
    LOG.info(" -> Found {} collision(s) for GameObject ID: {}", collidedObjects.size(), gameObject.getUUID()); // Log how many collisions detected

    boolean conditionMet = false; // Flag to track if condition is met

    for (GameObject collidedObject : collidedObjects) {
      String actualType = collidedObject.getType();
      LOG.info(" -> Checking collision with Object ID: {}, Type: '{}'", collidedObject.getUUID(), actualType); // Log the type of the collided object

      if (actualType == null) {
        LOG.warn(" -> Collided object ID {} has null type!", collidedObject.getUUID());
        continue; // Skip if type is null
      }

      boolean typesMatch = actualType.equals(collidedGroup);
      LOG.info("    -> Comparing actual type '{}' with expected group parameter '{}'. Match = {}", actualType, collidedGroup, typesMatch); // Log comparison result

      if (typesMatch) {
        conditionMet = true; // Set flag
        // Removed break to log all matches, kept return true for immediate exit on first match
        LOG.info(" -> Match found! CollisionCondition returning true.");
        return true; // Condition is met
      }
    }

    LOG.info(" -> No matching collision found. CollisionCondition returning false.");
    // --- DEBUG LOGGING END ---
    return false; // Return false if loop finishes without finding a match
  }
}