/**
 * Calculates and stores the current collisions of every game object
 * updateCollisions() is called by the game controller each step
 * @author Gage Garcia
 */
package oogasalad.engine.event;

import oogasalad.engine.controller.api.GameObjectProvider;
import oogasalad.engine.model.object.GameObject;

import java.util.*;

public class DefaultCollisionHandler implements CollisionHandler {
    private Map<GameObject, List<GameObject>> collisionMap;
    private final GameObjectProvider gameObjectProvider;

    public DefaultCollisionHandler(GameObjectProvider gameObjectProvider) {
        this.gameObjectProvider = gameObjectProvider;
        this.collisionMap = new HashMap<>();
    }

    /**
     * Update collision map
     */
    public void updateCollisions() {
        List<GameObject> gameObjects = gameObjectProvider.getGameObjects();
        if (gameObjects == null) return;
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
     *
     * @param gameObject to check
     * @return the list of game objects that is currently colliding with the specified object
     */
    public List<GameObject> getCollisions(GameObject gameObject) {
        return gameObject == null ? Collections.emptyList() : collisionMap.getOrDefault(gameObject, Collections.emptyList());
    }

    //checks if collision between two objects
    private boolean isCollision(GameObject obj1, GameObject obj2) {
        return obj1.getX() < obj2.getX() + obj2.getWidth() &&
                obj1.getX() + obj1.getWidth() > obj2.getX() &&
                obj1.getY() < obj2.getY() + obj2.getHeight() &&
                obj1.getY() + obj1.getHeight() > obj2.getY();
    }
}
