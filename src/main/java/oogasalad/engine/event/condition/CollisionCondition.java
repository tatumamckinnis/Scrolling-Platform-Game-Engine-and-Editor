package oogasalad.engine.event.condition;

import oogasalad.engine.event.CollisionHandler;
import oogasalad.engine.event.DefaultCollisionHandler;
import oogasalad.engine.model.object.GameObject;

import java.util.List;

public class CollisionCondition implements Condition {
    private final CollisionHandler collisionHandler;
    private final String collidedGroup;

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
