/**
 * Detects and stores collisions at each step
 * Used by condition checker to evaluate collision conditions
 * @author Gage Garcia
 */
package oogasalad.engine.controller;

import oogasalad.engine.model.object.GameObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CollisionHandler {
    //underlying storage of an object -> the list of objects its colliding with
    private Map<GameObject, List<GameObject>> collisionMap = new HashMap<>();
    private GameControllerAPI gameController;

    /**
     * updates current state of collisions at a step
     * Requires all gameObjects through their id's in a level
     */
    public void updateCollisions(List<GameObject> gameObject) {
        collisionMap.clear();
        //need to initialize with constructor?
        List<GameObject> gameObjects = gameController.getObjects();
        //loop through updating collision map using gameObject data to see if collision occurs

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
