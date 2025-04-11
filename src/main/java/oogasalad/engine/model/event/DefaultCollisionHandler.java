package oogasalad.engine.model.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oogasalad.engine.controller.api.GameObjectProvider;
import oogasalad.engine.model.object.GameObject;

/**
 * Calculates and stores the current collisions of every game object updateCollisions() is called by
 * the game controller each step
 *
 * @author Gage Garcia
 */
public class DefaultCollisionHandler implements CollisionHandler {

  private final Map<GameObject, List<GameObject>> collisionMap;
  private final GameObjectProvider gameObjectProvider;

  /**
   * requires a game object provider
   *
   * @param gameObjectProvider interface that gives access to all current game objects
   */
  public DefaultCollisionHandler(GameObjectProvider gameObjectProvider) {
    this.gameObjectProvider = gameObjectProvider;
    this.collisionMap = new HashMap<>();
  }

  /**
   * Update collision map
   */
  public void updateCollisions() {
    List<GameObject> gameObjects = gameObjectProvider.getGameObjects();
    if (gameObjects == null) {
      return;
    }
    collisionMap.clear();

    for (GameObject obj1 : gameObjects) {
      List<GameObject> collidingObjects = new ArrayList<>();
      for (GameObject obj2 : gameObjects) {
        if (obj1 != obj2 && isCollision(obj1, obj2)) {
          collidingObjects.add(obj2);
        }
      }
      collisionMap.put(obj1, collidingObjects);
    }
  }

  /**
   * @param gameObject to check
   * @return the list of game objects that is currently colliding with the specified object
   */
  public List<GameObject> getCollisions(GameObject gameObject) {
    return gameObject == null ? Collections.emptyList()
        : collisionMap.getOrDefault(gameObject, Collections.emptyList());
  }

  //checks if collision between two objects
  private boolean isCollision(GameObject obj1, GameObject obj2) {
    return obj1.getXPosition() < obj2.getXPosition() + obj2.getHitBoxWidth() &&
        obj1.getXPosition() + obj1.getHitBoxWidth() > obj2.getXPosition() &&
        obj1.getYPosition() < obj2.getYPosition() + obj2.getHitBoxHeight() &&
        obj1.getYPosition() + obj1.getHitBoxHeight() > obj2.getYPosition();
  }
}
