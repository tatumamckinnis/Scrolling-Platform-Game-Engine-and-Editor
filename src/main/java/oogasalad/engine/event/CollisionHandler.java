/**
 * Detects and stores collisions at each step
 * Used by condition checker to evaluate collision conditions
 * @author Gage Garcia
 */
package oogasalad.engine.event;

import oogasalad.engine.controller.DefaultGameController;
import oogasalad.engine.controller.GameControllerAPI;
import oogasalad.engine.model.object.GameObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollisionHandler {
    //underlying storage of an object -> the list of objects its colliding with
    private Map<GameObject, List<GameObject>> collisionMap = new HashMap<>();
    private GameControllerAPI gameController = new DefaultGameController();

    /**
     * updates current state of collisions at a step
     * Requires all gameObjects through their id's in a level
     */
    public void updateCollisions(List<GameObject> gameObject) {
        collisionMap.clear();
        //need to initialize with constructor?
        List<GameObject> gameObjects = gameController.getObjects();
        //loop through updating collision map using gameObject data to see if collision occurs
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

    private boolean isCollision(GameObject obj1, GameObject obj2) {
        // @param hitBoxX the x-position of the hitbox
        //   * @param hitBoxY the y-position of the hitbox
        //   * @param hitBoxWidth width of the hitbox
        //   * @param hitBoxHeight height of the hitbox
        return obj1.getX() < obj2.getX() + obj2.getWidth() &&
                obj1.getX() + obj1.getWidth() > obj2.getX() &&
                obj1.getY() < obj2.getY() + obj2.getHeight() &&
                obj1.getY() + obj1.getHeight() > obj2.getY();
    }

    /**
     *
     * @param gameObject
     * @return
     */
    public List<GameObject> getCollisions(GameObject gameObject) {
        return collisionMap.get(gameObject);
    }
}
