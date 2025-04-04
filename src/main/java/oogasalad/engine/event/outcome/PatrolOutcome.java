/**
 * movement pattern that moves object back and forth horizontally
 * @author Gage Garcia
 */
package oogasalad.engine.event.outcome;

import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.model.object.mapObject;

public class PatrolOutcome implements Outcome {
    private final mapObject map;

    public PatrolOutcome(mapObject map) {
        this.map = map;
    }
    @Override
    public void execute(GameObject gameObject) {
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
}
