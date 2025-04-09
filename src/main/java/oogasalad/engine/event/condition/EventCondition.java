/**
 * Condition Object defining enums for valid conditions to check for
 * ConditionChecker controller will have logic for checking condition
 * @author Gage Garcia
 */

package oogasalad.engine.event.condition;


public record EventCondition(EventCondition.ConditionType conditionType) {
    /**
     * Define list of valid conditions enums
     */
    public enum ConditionType {
        TRUE,
        SPACE_KEY_PRESSED,
        COLLIDED_WITH_ENEMY,
        COLLIDED_WITH_PLAYER,
        COLLIDED_WITH_PLATFORM,
        UP_ARROW_PRESSED,
        W_KEY_PRESSED,
        A_KEY_PRESSED,
        S_KEY_PRESSED,
        D_KEY_PRESSED,
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
