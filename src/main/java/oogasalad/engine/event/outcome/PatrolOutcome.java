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
        int dx = Integer.parseInt(gameObject.getParams().getOrDefault("MovementAmount", "4"));
        if(gameObject.getX() < 0){
            gameObject.setXVelocity(dx);
        }
        else if(gameObject.getX()+gameObject.getHitBoxWidth() >= gameExecutor.getMapObject().width()){
            gameObject.setXVelocity(-dx);
        }
        else if(gameObject.getXVelocity() == 0){
            gameObject.setXVelocity(-dx);
        }
    }
}
