/**
 * Outcome that triggers lose game
 * @author Gage Garcia
 */
package oogasalad.engine.event.outcome;

import oogasalad.engine.model.object.GameObject;

public class LoseGameOutcome implements Outcome {
    @Override
    public void execute(GameObject gameObject) {
        System.out.println("You lose the game");
    }
}
