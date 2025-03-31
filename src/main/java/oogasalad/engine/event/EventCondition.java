/**
 * Condition Object defining enums for valid conditions to check for
 * ConditionChecker controller will have logic for checking condition
 * Maintains mapping of required params to evaluate condition
 * @author Gage Garcia
 */

package oogasalad.engine.event;

import java.util.Map;

public class EventCondition {
    /**
     * Define list of valid conditions enums
     */
    public enum ConditionType {
        SPACE_KEY_PRESSED,
        VAR_BELOW_CONDITION
    }

    private ConditionType conditionType;

    //stores expected parameter mapping for every conditionType
    //ie VAR_BELOW_CONDITION -> <VAR, value>, <Condition, value>
    private Map<ConditionType, Map<String, String>> paramMap;

    /**
     * get the type of condition
     * @return ConditionType enum
     */
    public ConditionType getConditionType() {
        return conditionType;
    }

    /**
     * @param conditionType enum
     * @return the associated expected parameter map
     */
    public Map<String, String> getParamMap(ConditionType conditionType) {
        return paramMap.get(conditionType);
    }
}
