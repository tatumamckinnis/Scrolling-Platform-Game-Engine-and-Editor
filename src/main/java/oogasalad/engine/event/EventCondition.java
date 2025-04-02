/**
 * Condition Object defining enums for valid conditions to check for
 * ConditionChecker controller will have logic for checking condition
 * @author Gage Garcia
 */

package oogasalad.engine.event;

public class EventCondition {
    /**
     * Define list of valid conditions enums
     */
    public enum ConditionType {
        TRUE,
        SPACE_KEY_PRESSED,
        COLLIDED_WITH_ENEMY
    }


    private ConditionType conditionType;

    /**
     * Constructor sets condition type
     * @param conditionType
     */
    public EventCondition(ConditionType conditionType) {
        this.conditionType = conditionType;
    }

    /**
     * get the type of condition
     * @return ConditionType enum
     */
    public ConditionType getConditionType() {
        return conditionType;
    }


}
