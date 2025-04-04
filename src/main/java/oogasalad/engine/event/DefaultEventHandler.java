/**
 * Event handling class that implements event EventHandler interface
 * @author Gage Garcia
 */
package oogasalad.engine.event;

import oogasalad.engine.controller.GameControllerAPI;
import oogasalad.engine.controller.GameManagerAPI;
import oogasalad.engine.controller.InputProvider;
import oogasalad.engine.model.object.DynamicVariableCollection;
import oogasalad.engine.model.object.GameObject;

import java.util.List;

public class DefaultEventHandler implements EventHandler {
    private final ConditionChecker conditionChecker;
    private final OutcomeExecutor outcomeExecutor;

    /**
     * Initializes event handler
     * @param gameController interface that gives access to a collision handler
     */
    public DefaultEventHandler(InputProvider inputProvider, GameControllerAPI gameController) {
        outcomeExecutor = new OutcomeExecutor(gameController);
        CollisionHandler collisionHandler = gameController.getCollisionHandler();
        conditionChecker = new ConditionChecker(inputProvider, collisionHandler);
    }
    /**
     * process given event
     * condition checking done [[A OR B] AND [C OR D]  AND [E OR F]]
     * @param event the event model to process
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
