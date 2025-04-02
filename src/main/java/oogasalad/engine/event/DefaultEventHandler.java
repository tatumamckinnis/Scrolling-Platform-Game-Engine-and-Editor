/**
 * Event handling class that implements event EventHandler interface
 */
package oogasalad.engine.event;

import oogasalad.engine.controller.GameControllerAPI;
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
    public DefaultEventHandler(GameControllerAPI gameController) {
        outcomeExecutor = new OutcomeExecutor(gameController);
    }
    /**
     * process given event, all conditions must be true to execute
     * @param event
     */
    public void handleEvent(Event event) {
        GameObject gameObject = event.getGameObject();
        boolean validEvent = true;
        for (EventCondition condition : event.getConditions()) {
            if (!conditionChecker.checkCondition(condition.getConditionType(), gameObject)) {
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
