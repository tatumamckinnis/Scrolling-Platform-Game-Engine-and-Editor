/**
 * Updates game state to reflect event outcome
 * @author Gage Garcia
 */
package oogasalad.engine.event;
import java.util.Map;
import oogasalad.engine.controller.GameController;
import oogasalad.engine.model.object.DynamicVariableCollection;
import oogasalad.engine.model.object.GameObject;

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
     * @param gameObject
     */
    public void executeOutcome(EventOutcome.OutcomeType outcomeType, GameObject gameObject) {
        if (outcomeType == EventOutcome.OutcomeType.MOVE_RIGHT) {
            int dx = Integer.parseInt(gameObject.getParams().getOrDefault("MoveRightAmount", "5"));
            gameObject.setX(gameObject.getxPos() + dx);
        }
    }

}
