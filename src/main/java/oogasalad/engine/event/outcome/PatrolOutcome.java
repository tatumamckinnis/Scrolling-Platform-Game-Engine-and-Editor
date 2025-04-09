/**
 * movement pattern that moves object back and forth horizontally
 * @author Gage Garcia
 */
package oogasalad.engine.event.outcome;

import oogasalad.engine.controller.api.GameExecutor;
import oogasalad.engine.model.object.GameObject;

public class PatrolOutcome implements Outcome {
    private final GameExecutor gameExecutor;

    public PatrolOutcome(GameExecutor gameExecutor) {
        this.gameExecutor = gameExecutor;
    }

    @Override
    public void execute(GameObject gameObject) {
        double dx = gameObject.getDoubleParams().getOrDefault("MovementAmount", 4.0);
        if(gameObject.getXPosition() < 0){
            gameObject.setXVelocity(dx);
        }
        else if(gameObject.getXPosition()+gameObject.getHitBoxWidth() >= gameExecutor.getMapObject().maxX()){
            gameObject.setXVelocity(-dx);
        }
        else if(gameObject.getXVelocity() == 0){
            gameObject.setXVelocity(-dx);
        }
    }
}
