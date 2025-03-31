/**
 * Event handling class that implements event EventHandler interface
 */
package oogasalad.engine.event;

import oogasalad.engine.controller.GameController;

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


    }
}
