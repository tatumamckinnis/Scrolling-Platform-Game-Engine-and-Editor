/**
 * Updates game state to reflect event outcome
 * @author Gage Garcia
 */
package oogasalad.engine.event;
import oogasalad.engine.controller.GameControllerAPI;
import oogasalad.engine.event.EventOutcome.OutcomeType;
import oogasalad.engine.model.object.DynamicVariableCollection;
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.model.object.mapObject;

public class OutcomeExecutor {
    private GameControllerAPI gameController;
    private mapObject map;

    /**
     * Initialize the executor with a game controller
     * @param gameController
     */
    public OutcomeExecutor(GameControllerAPI gameController) {
        this.gameController = gameController;
        this.map = gameController.getMapObject();
    }

    /**
     * executes outcome using parameter map using game controller
     * @param outcomeType
     * @param gameObject
     */
    public void executeOutcome(EventOutcome.OutcomeType outcomeType, GameObject gameObject) {
        if (outcomeType == EventOutcome.OutcomeType.MOVE_RIGHT) {
            int dx = Integer.parseInt(gameObject.getParams().getOrDefault("MoveRightAmount", "2"));
            gameObject.setX(gameObject.getX() + dx);
        }
        if (outcomeType == EventOutcome.OutcomeType.PATROL){
            int dx = Integer.parseInt(gameObject.getParams().getOrDefault("MovementAmount", "4"));
            if(gameObject.getX() < 0){
                gameObject.setXVelocity(dx);
            }
            else if(gameObject.getX()+gameObject.getHitBoxWidth() >= map.width()){
                gameObject.setXVelocity(-dx);
            }
            else if(gameObject.getXVelocity() == 0){
                gameObject.setXVelocity(-dx);
            }
        }
        if (outcomeType == EventOutcome.OutcomeType.APPLY_GRAVITY) {
            int dy = Integer.parseInt(gameObject.getParams().getOrDefault("ApplyGravityAmount", "5"));

            // Only apply gravity if the object is in the air (falling or jumping)
            if (!gameObject.isGrounded()) {
                gameObject.setYVelocity(gameObject.getYVelocity() + dy);
            }
        }

        if (outcomeType == EventOutcome.OutcomeType.JUMP) {
            int dy = Integer.parseInt(gameObject.getParams().getOrDefault("JumpAmount", "40"));

            // Only allow jumping if the object is on the ground
            if (gameObject.isGrounded()) {
                gameObject.setYVelocity(-dy);
                gameObject.setGrounded(false); // Mark object as airborne
            }
        }
    }


}
