/**
 * Evaluates EventCondition enums given their associated parameters
 *
 * @author Gage Garcia
 */
package oogasalad.engine.event;

import java.util.HashMap;
import java.util.Map;
import javafx.scene.input.KeyCode;
import oogasalad.engine.controller.api.InputProvider;
import oogasalad.engine.event.condition.CollisionCondition;
import oogasalad.engine.event.condition.Condition;
import oogasalad.engine.event.condition.EventCondition;
import oogasalad.engine.event.condition.InputCondition;
import oogasalad.engine.event.condition.TrueCondition;
import oogasalad.engine.model.object.GameObject;

public class ConditionChecker {

  private final Map<EventCondition.ConditionType, Condition> conditionMap;

  public ConditionChecker(InputProvider inputProvider, CollisionHandler collisionHandler) {
    this.conditionMap = new HashMap<>();
    conditionMap.put(EventCondition.ConditionType.SPACE_KEY_PRESSED,
        new InputCondition(inputProvider, KeyCode.SPACE));
    conditionMap.put(EventCondition.ConditionType.W_KEY_PRESSED,
        new InputCondition(inputProvider, KeyCode.W));
    conditionMap.put(EventCondition.ConditionType.A_KEY_PRESSED,
        new InputCondition(inputProvider, KeyCode.A));
    conditionMap.put(EventCondition.ConditionType.S_KEY_PRESSED,
        new InputCondition(inputProvider, KeyCode.S));
    conditionMap.put(EventCondition.ConditionType.D_KEY_PRESSED,
        new InputCondition(inputProvider, KeyCode.D));
    conditionMap.put(EventCondition.ConditionType.UP_ARROW_PRESSED,
        new InputCondition(inputProvider, KeyCode.UP));
    conditionMap.put(EventCondition.ConditionType.COLLIDED_WITH_ENEMY,
        new CollisionCondition(collisionHandler, "enemy"));
    conditionMap.put(EventCondition.ConditionType.TRUE,
        new TrueCondition());
    conditionMap.put(EventCondition.ConditionType.COLLIDED_WITH_PLAYER,
        new CollisionCondition(collisionHandler, "player"));
    conditionMap.put(EventCondition.ConditionType.COLLIDED_WITH_PLATFORM,
        new CollisionCondition(collisionHandler, "platforms"));

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

