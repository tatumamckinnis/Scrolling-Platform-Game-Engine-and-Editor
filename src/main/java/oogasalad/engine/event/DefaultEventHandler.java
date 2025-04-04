/**
 * Event handling class that implements event EventHandler interface
 */
package oogasalad.engine.event;

import oogasalad.engine.controller.api.GameControllerAPI;
import oogasalad.engine.controller.api.InputProvider;
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
        CollisionHandler collisionHandler = gameController.getCollisionHandler();
        conditionChecker = new ConditionChecker(inputProvider, collisionHandler);
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
            boolean validGroup = false; // This group is false until proven true

            for (EventCondition eventCondition : conditionGroup) {
                if (conditionChecker.checkCondition(eventCondition.getConditionType(), gameObject)) {
                    validGroup = true; // One condition in this OR-group is true
                    break; // No need to check further in this OR-group
                }
            }

            if (!validGroup) { // If the OR-group never became true, entire event is invalid
                validEvent = false;
                break; // No need to check further, we already know event fails
            }
        }

        if (validEvent) {
            for (EventOutcome outcome : event.getOutcomes()) {
               // System.out.println("Executing Outcome: "+outcome.getOutcomeType().toString());
                outcomeExecutor.executeOutcome(outcome.getOutcomeType(), gameObject);
            }
        }
    }
}
