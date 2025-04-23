package oogasalad.engine.model.event;

import java.util.HashMap;
import java.util.Map;
import javafx.scene.input.KeyCode;
import oogasalad.engine.controller.api.InputProvider;
import oogasalad.engine.model.event.condition.AtOrBeyondXCondition;
import oogasalad.engine.model.event.condition.AtOrBeyondYCondition;
import oogasalad.engine.model.event.condition.CollisionCondition;
import oogasalad.engine.model.event.condition.Condition;
import oogasalad.engine.model.event.condition.EventCondition;
import oogasalad.engine.model.event.condition.EventCondition.ConditionType;
import oogasalad.engine.model.event.condition.InputCondition;
import oogasalad.engine.model.event.condition.TrueCondition;
import oogasalad.engine.model.event.condition.VariableGreaterThanThresholdCondition;
import oogasalad.engine.model.event.condition.VariableLessThanThresholdCondition;
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
    conditionMap.put(EventCondition.ConditionType.TRUE,
        new TrueCondition());
    conditionMap.put(EventCondition.ConditionType.KEY_PRESSED,
        new InputCondition(inputProvider, true));
    conditionMap.put(ConditionType.KEY_RELEASED, new
        InputCondition(inputProvider, false));
    conditionMap.put(EventCondition.ConditionType.COLLIDED_WITH_GROUP,
        new CollisionCondition(collisionHandler));
    conditionMap.put(ConditionType.LESS_THAN_VARIABLE_THRESHOLD,
        new VariableLessThanThresholdCondition());
    conditionMap.put(ConditionType.GREATER_VARIABLE_THRESHOLD,
        new VariableGreaterThanThresholdCondition());
    conditionMap.put(ConditionType.AT_OR_BEYOND_X, new AtOrBeyondXCondition());
    conditionMap.put(ConditionType.AT_OR_BEYOND_Y, new AtOrBeyondYCondition());
  }

  /**
   * evaluates condition
   *
   * @param eventCondition -> event model containing type and parameters
   * @param gameObject    requires use of predefined mapping of conditionType -> expected params
   * @return true or false
   */
  public boolean checkCondition(EventCondition eventCondition, GameObject gameObject) {
    Condition condition = conditionMap.get(eventCondition.conditionType());
    return condition.isMet(gameObject, eventCondition.stringProperties(), eventCondition.doubleProperties());

  }

}

