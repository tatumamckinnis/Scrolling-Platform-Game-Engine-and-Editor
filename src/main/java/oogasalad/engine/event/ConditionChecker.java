/**
 * Evaluates EventCondition enums given their associated parameters
 * @author Gage Garcia
 */
package oogasalad.engine.event;
import old_editor_example.DynamicVariable;
import oogasalad.engine.controller.CollisionHandler;
import oogasalad.engine.controller.InputHandler;
import oogasalad.engine.model.object.DynamicVariableCollection;
import oogasalad.engine.model.object.GameObject;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConditionChecker {
    private InputHandler inputHandler = new InputHandler();
    private CollisionHandler collisionHandler = new CollisionHandler();
    /**
     * evaluates condition
     * @param conditionType -> type of condition
     * @param gameObject
     * requires use of predefined mapping of conditionType -> expected params
     * @return true or false
     */
    public boolean checkCondition(EventCondition.ConditionType conditionType, GameObject gameObject) {
        if (conditionType == EventCondition.ConditionType.TRUE) {
            return true;
        }
        else if (conditionType == EventCondition.ConditionType.SPACE_KEY_PRESSED) {
            return inputHandler.isKeyPressed(' ');
        }
        else if (conditionType == EventCondition.ConditionType.COLLIDED_WITH_ENEMY) {
            List<GameObject> collidedObjects = collisionHandler.getCollisions(gameObject);
            for (GameObject collidedObject : collidedObjects) {
                if (collidedObject.getObjectType() == GameObject.ObjectType.ENEMY) {
                    return true;
                }
            }
            return false;
        }


        return false;
    }

}

