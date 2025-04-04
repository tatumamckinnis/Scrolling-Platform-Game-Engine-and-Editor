/**
 * Evaluates EventCondition enums given their associated parameters
 * @author Gage Garcia
 */
package oogasalad.engine.event;
import javafx.scene.input.KeyCode;
import oogasalad.engine.controller.InputProvider;
import oogasalad.engine.event.condition.CollisionCondition;
import oogasalad.engine.event.condition.Condition;
import oogasalad.engine.event.condition.InputCondition;
import oogasalad.engine.event.condition.TrueCondition;
import oogasalad.engine.model.object.GameObject;

import java.util.HashMap;
import java.util.Map;

public class ConditionChecker {
    //Maps condition type to condition evaluation interface
    private final Map<EventCondition.ConditionType, Condition> conditionMap = new HashMap<>();

    /**
     * initializes mapping of event condition enum to its condition interface
     * @param inputProvider interface that allows checking of current input
     * @param collisionHandler (should be) interface that allows checking of collisions
     */
    public ConditionChecker(InputProvider inputProvider, CollisionHandler collisionHandler) {
        conditionMap.put(EventCondition.ConditionType.SPACE_KEY_PRESSED,
                new InputCondition(inputProvider, KeyCode.SPACE));
        conditionMap.put(EventCondition.ConditionType.W_KEY_PRESSED,
                new InputCondition(inputProvider, KeyCode.W));
        conditionMap.put(EventCondition.ConditionType.UP_ARROW_PRESSED,
                new InputCondition(inputProvider, KeyCode.UP));
        conditionMap.put(EventCondition.ConditionType.COLLIDED_WITH_ENEMY,
                new CollisionCondition(collisionHandler, "enemies"));
        conditionMap.put(EventCondition.ConditionType.TRUE,
                new TrueCondition());
    }
    /**
     * Evaluates condition
     * @param conditionType -> type of condition
     * @param gameObject -> game object tied to event
     * requires use of predefined mapping of conditionType -> expected params
     * @return true or false
     */
    public boolean checkCondition(EventCondition.ConditionType conditionType, GameObject gameObject) {
        Condition condition = conditionMap.get(conditionType);
        return condition.isMet(gameObject);
    }

}

