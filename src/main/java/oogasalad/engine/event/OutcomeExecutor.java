/**
 * Updates game state to reflect event outcome
 * @author Gage Garcia
 */
package oogasalad.engine.event;
import oogasalad.engine.controller.GameControllerAPI;
import oogasalad.engine.model.object.DynamicVariableCollection;
import oogasalad.engine.model.object.GameObject;

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
     * @param gameObject
     */
    public void executeOutcome(EventOutcome.OutcomeType outcomeType, GameObject gameObject) {
        if (outcomeType == EventOutcome.OutcomeType.MOVE_RIGHT) {
            int dx = Integer.parseInt(gameObject.getParams().getOrDefault("MoveRightAmount", "5"));
            gameObject.setX(gameObject.getX() + dx);
        }
        if (outcomeType == EventOutcome.OutcomeType.APPLY_GRAVITY) {
            int dy = Integer.parseInt(gameObject.getParams().getOrDefault("ApplyGravityAmount", "1"));
            gameObject.setYVelocity(gameObject.getYVelocity() - dy);
        }
        if (outcomeType == EventOutcome.OutcomeType.JUMP) {
            int dy = Integer.parseInt(gameObject.getParams().getOrDefault("JumpAmount", "5"));
            gameObject.setYVelocity(gameObject.getYVelocity() + dy);
        }
    }

}
