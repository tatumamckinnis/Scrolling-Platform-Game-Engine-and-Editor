/**
 * Evaluates EventCondition enums given their associated parameters
 * @author Gage Garcia
 */
package oogasalad.engine.event;
import old_editor_example.DynamicVariable;
import oogasalad.engine.model.object.DynamicVariableCollection;

import java.util.List;
import java.util.Map;

public class ConditionChecker {

    /**
     * evaluates condition
     * @param conditionType -> type of condition
     * @param params -> collection of user-defined dynamic variables from an object
     * requires use of predefined mapping of conditionType -> expected params
     * @return true or false
     */
    public boolean checkCondition(EventCondition.ConditionType conditionType, DynamicVariableCollection params) {
        return true;
    }
}
