/**
 * Detects and stores collisions at each step
 * Used by condition checker to evaluate collision conditions
 * @author Gage Garcia
 */
package oogasalad.engine.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CollisionHandler {
    //underlying storage of an object -> the list of objects its colliding with
    private Map<UUID, List<UUID>> collisionMap;

    /**
     * updates current state of collisions at a step
     * Requires all gameObjects through their id's in a level
     */
    public void updateCollisions(List<UUID> gameObjectIDs) {
        collisionMap.clear();
    }
}
