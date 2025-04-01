/**
 * Event handling class that implements event EventHandler interface
 */
package oogasalad.engine.event;

import oogasalad.engine.controller.GameController;
import oogasalad.engine.model.object.DynamicVariableCollection;

import java.util.List;

public class DefaultEventHandler implements EventHandler {
    private ConditionChecker conditionChecker;
    private OutcomeExecutor outcomeExecutor;

    /**
     * Initializes event handler
     * @param gameController
     */
    public DefaultEventHandler(GameController gameController) {
        outcomeExecutor = new OutcomeExecutor(gameController);
    }
    /**
     * process given event
     * @param event
     */
    public void handleEvent(Event event) {
        DynamicVariableCollection params = event.getParams();
        boolean validEvent = true;
        for (EventCondition condition : event.getConditions()) {
            if (!conditionChecker.checkCondition(condition.getConditionType(), params)) {
                validEvent = false;
            }
        }
        if (validEvent) {
            for (EventOutcome outcome : event.getOutcomes()) {
                outcomeExecutor.executeOutcome(outcome.getOutcomeType(), params);
            }
        }

    }
}
