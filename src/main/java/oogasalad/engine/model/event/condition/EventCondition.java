

package oogasalad.engine.model.event.condition;

import java.util.Map;

/**
 * Condition Object defining enums for valid conditions to check for ConditionChecker controller
 * will have logic for checking condition
 *
 * @author Gage Garcia
 */
public record EventCondition(EventCondition.ConditionType conditionType,
                             Map<String,String> stringProperties,
                             Map<String,Double> doubleProperties) {

  /**
   * Define list of valid conditions enums
   */
  public enum ConditionType {
    TRUE,
    COLLIDED_WITH_GROUP,
    KEY_PRESSED,
    KEY_RELEASED,
    GREATER_VARIABLE_THRESHOLD,
    LESS_THAN_VARIABLE_THRESHOLD,
    EQUAL_VARIABLE_THRESHOLD,
    AT_OR_BEYOND_X,
    AT_OR_BEYOND_Y,
  }


  /**
   * Constructor sets condition type
   *
   * @param conditionType enum representing type of condition
   */
  public EventCondition {
  }

  /**
   * get the type of condition
   *
   * @return ConditionType enum
   */
  @Override
  public ConditionType conditionType() {
    return conditionType;
  }


}
