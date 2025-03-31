/**
 * Updates game state to reflect event outcome
 * @author Gage Garcia
 */
package oogasalad.engine.event;
import java.util.Map;
import oogasalad.engine.controller.GameController;

public class OutcomeExecutor {
    private GameController gameController;

    /**
     * Initialize the executor with a game controller
     * @param gameController
     */
    public OutcomeExecutor(GameController gameController) {
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
