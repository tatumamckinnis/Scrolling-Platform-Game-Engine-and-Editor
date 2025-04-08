/**
 * Event handling class that implements event EventHandler interface
 * @author Gage Garcia
 */
package oogasalad.engine.event;

import oogasalad.engine.controller.api.GameExecutor;
import oogasalad.engine.controller.api.InputProvider;
import oogasalad.engine.event.condition.EventCondition;
import oogasalad.engine.event.outcome.EventOutcome;
import oogasalad.engine.model.object.GameObject;

import java.util.List;

public class DefaultEventHandler implements EventHandler {
    private final ConditionChecker conditionChecker;
    private final OutcomeExecutor outcomeExecutor;

    /**
     * Initializes event handler
     * @param gameExecutor interface that allows outcome updates to game state
     */
    public DefaultEventHandler(InputProvider inputProvider, CollisionHandler collisionHandler, GameExecutor gameExecutor) {
        outcomeExecutor = new OutcomeExecutor(gameExecutor);
        conditionChecker = new ConditionChecker(inputProvider, collisionHandler);
    }

    public void handleEvent(Event event) {
        GameObject gameObject = event.getGameObject();
        boolean validEvent = true;
        List<List<EventCondition>> conditionGroups = event.getConditions();

        for (List<EventCondition> conditionGroup : conditionGroups) {
            boolean validGroup = false; // This group is false until proven true

            for (EventCondition eventCondition : conditionGroup) {
                if (conditionChecker.checkCondition(eventCondition.conditionType(), gameObject)) {
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
                outcomeExecutor.executeOutcome(outcome.outcomeType(), gameObject);
            }
        }
    }
}
