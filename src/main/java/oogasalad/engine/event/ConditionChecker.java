/**
 * Evaluates EventCondition enums given their associated parameters
 * @author Gage Garcia
 */
package oogasalad.engine.event;
import javafx.scene.input.KeyCode;
import oogasalad.engine.controller.GameManagerAPI;
import oogasalad.engine.controller.InputProvider;
import oogasalad.engine.model.object.GameObject;

import java.awt.event.KeyEvent;
import java.util.List;

public class ConditionChecker {
    private CollisionHandler collisionHandler;
    private InputProvider inputProvider;

    public ConditionChecker(InputProvider inputProvider) {
        this.inputProvider = inputProvider;
    }
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
            System.out.println("SPACE KEY PRESSED is " + inputProvider.isKeyPressed(KeyCode.SPACE));
            return inputProvider.isKeyPressed(KeyCode.SPACE);
        }

        else if (conditionType == EventCondition.ConditionType.UP_ARROW_PRESSED) {
            System.out.println("UP ARROW KEY PRESSED is " + inputProvider.isKeyPressed(KeyCode.UP));
            return inputProvider.isKeyPressed(KeyCode.UP);
        }
        else if (conditionType == EventCondition.ConditionType.W_KEY_PRESSED) {
            System.out.println("W ARROW KEY PRESSED is " + inputProvider.isKeyPressed(KeyCode.UP));
            return inputProvider.isKeyPressed(KeyCode.W);
        }
        else if (conditionType == EventCondition.ConditionType.COLLIDED_WITH_ENEMY) {
            List<GameObject> collidedObjects = collisionHandler.getCollisions(gameObject);
            for (GameObject collidedObject : collidedObjects) {
                if (collidedObject.getType().equals("Enemy")) {
                    return true;
                }
            }
            return false;
        }


        return false;
    }

}

