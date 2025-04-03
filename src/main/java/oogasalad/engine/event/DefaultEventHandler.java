/**
 * Event handling class that implements event EventHandler interface
 */
package oogasalad.engine.event;

import oogasalad.engine.controller.GameControllerAPI;
import oogasalad.engine.controller.GameManagerAPI;
import oogasalad.engine.controller.InputProvider;
import oogasalad.engine.model.object.DynamicVariableCollection;
import oogasalad.engine.model.object.GameObject;

import java.util.List;

public class DefaultEventHandler implements EventHandler {
    private ConditionChecker conditionChecker;
    private OutcomeExecutor outcomeExecutor;

    /**
     * Initializes event handler
     * @param gameController
     */
    public DefaultEventHandler(InputProvider inputProvider, GameControllerAPI gameController) {
        outcomeExecutor = new OutcomeExecutor(gameController);
        conditionChecker = new ConditionChecker(inputProvider);
    }
    /**
     * process given event, all conditions must be true to execute
     * @param event
     */
    public void handleEvent(Event event) {
        GameObject gameObject = event.getGameObject();
        boolean validEvent = true;
        List<List<EventCondition>> conditionGroups = event.getConditions();

        for (List<EventCondition> conditionGroup : conditionGroups) {
            boolean validGroup = false;
            for (EventCondition eventCondition : conditionGroup) {
                if (conditionChecker.checkCondition(eventCondition.getConditionType(), gameObject)) {
                    validGroup = true;
                }
            }
            if (validGroup == false) {
                validEvent = false;
            }
        }
        if (validEvent) {
            for (EventOutcome outcome : event.getOutcomes()) {
                outcomeExecutor.executeOutcome(outcome.getOutcomeType(), gameObject);
            }
        }

    }
}
