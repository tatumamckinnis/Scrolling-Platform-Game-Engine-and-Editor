package oogasalad.engine.model.event;

import java.util.HashMap;
import java.util.Map;
import javafx.scene.input.KeyCode;
import oogasalad.engine.controller.api.InputProvider;
import oogasalad.engine.model.event.condition.CollisionCondition;
import oogasalad.engine.model.event.condition.Condition;
import oogasalad.engine.model.event.condition.EventCondition;
import oogasalad.engine.model.event.condition.EventCondition.ConditionType;
import oogasalad.engine.model.event.condition.InputCondition;
import oogasalad.engine.model.event.condition.TrueCondition;
import oogasalad.engine.model.event.condition.VariableThresholdCondition;
import oogasalad.engine.model.object.GameObject;

/**
 * Evaluates EventCondition enums given their associated parameters
 *
 * @author Gage Garcia
 */
public class ConditionChecker {

  private final Map<EventCondition.ConditionType, Condition> conditionMap;

  /**
   * requires an input provider and a collision handler
   *
   * @param inputProvider    interface providing access to input
   * @param collisionHandler interface providing access to collisions
   */
  public ConditionChecker(InputProvider inputProvider, CollisionHandler collisionHandler) {
    this.conditionMap = new HashMap<>();
    conditionMap.put(EventCondition.ConditionType.SPACE_KEY_PRESSED,
        new InputCondition(inputProvider, KeyCode.SPACE, true));
    conditionMap.put(EventCondition.ConditionType.W_KEY_PRESSED,
        new InputCondition(inputProvider, KeyCode.W, true));
    conditionMap.put(EventCondition.ConditionType.A_KEY_PRESSED,
        new InputCondition(inputProvider, KeyCode.A, true));
    conditionMap.put(EventCondition.ConditionType.S_KEY_PRESSED,
        new InputCondition(inputProvider, KeyCode.S, true));
    conditionMap.put(EventCondition.ConditionType.D_KEY_PRESSED,
        new InputCondition(inputProvider, KeyCode.D, true));
    conditionMap.put(EventCondition.ConditionType.UP_ARROW_PRESSED,
        new InputCondition(inputProvider, KeyCode.UP,true));
    conditionMap.put(EventCondition.ConditionType.COLLIDED_WITH_ENEMY,
        new CollisionCondition(collisionHandler, "enemy"));
    conditionMap.put(EventCondition.ConditionType.TRUE,
        new TrueCondition());
    conditionMap.put(EventCondition.ConditionType.COLLIDED_WITH_PLAYER,
        new CollisionCondition(collisionHandler, "player"));
    conditionMap.put(EventCondition.ConditionType.COLLIDED_WITH_PLATFORM,
        new CollisionCondition(collisionHandler, "platforms"));
    conditionMap.put(EventCondition.ConditionType.RIGHT_ARROW_PRESSED,
        new InputCondition(inputProvider, KeyCode.RIGHT,true));
    conditionMap.put(EventCondition.ConditionType.LEFT_ARROW_PRESSED,
        new InputCondition(inputProvider, KeyCode.LEFT,true));
    conditionMap.put(ConditionType.VARIABLE_THRESHOLD,
        new VariableThresholdCondition());
    conditionMap.put(ConditionType.W_KEY_RELEASED, new
        InputCondition(inputProvider, KeyCode.W, false));
    conditionMap.put(ConditionType.D_KEY_RELEASED,
        new InputCondition(inputProvider, KeyCode.D, false));
    conditionMap.put(ConditionType.A_KEY_RELEASED, new InputCondition(inputProvider, KeyCode.A, false));
  }

  /**
   * evaluates condition
   *
   * @param conditionType -> type of condition
   * @param gameObject    requires use of predefined mapping of conditionType -> expected params
   * @return true or false
   */
  public boolean checkCondition(EventCondition.ConditionType conditionType, GameObject gameObject) {
    Condition condition = conditionMap.get(conditionType);
    return condition.isMet(gameObject);

  }

}

