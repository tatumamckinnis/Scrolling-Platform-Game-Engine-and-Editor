/**
 * Updates game state to reflect event outcome
 * @author Gage Garcia
 */
package oogasalad.engine.event;
import oogasalad.engine.controller.GameControllerAPI;
import oogasalad.engine.model.object.DynamicVariableCollection;

public class OutcomeExecutor {
    private GameControllerAPI gameController;

    /**
     * Initialize the executor with a game controller
     * @param gameController
     */
    public OutcomeExecutor(GameControllerAPI gameController) {
        this.gameController = gameController;
    }

    /**
     * executes outcome using parameter map using game controller
     * @param outcomeType
     * @param params Collection of user-defined dynamic variables
     */
    public void executeOutcome(EventOutcome.OutcomeType outcomeType, DynamicVariableCollection params) {

    }

}
