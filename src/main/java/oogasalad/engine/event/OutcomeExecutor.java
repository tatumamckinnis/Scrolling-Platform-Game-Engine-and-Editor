/**
 * Updates game state to reflect event outcome
 * @author Gage Garcia
 */
package oogasalad.engine.event;
import oogasalad.engine.controller.api.GameControllerAPI;
import oogasalad.engine.event.outcome.*;
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.model.object.mapObject;

import java.util.HashMap;
import java.util.Map;

public class OutcomeExecutor {
    private GameControllerAPI gameController;
    private mapObject map;
    private Map<EventOutcome.OutcomeType, Outcome> outcomeMap;

    /**
     * Initialize the executor with a game controller
     * @param gameController
     * Initialize mapping of outcome enum to outcome interface
     */
    public OutcomeExecutor(GameControllerAPI gameController) {
        this.gameController = gameController;
        this.map = gameController.getMapObject();
        this.outcomeMap = new HashMap<>();
        outcomeMap.put(EventOutcome.OutcomeType.MOVE_RIGHT,
                new MoveRightOutcome());
        outcomeMap.put(EventOutcome.OutcomeType.JUMP,
                new JumpOutcome());
        outcomeMap.put(EventOutcome.OutcomeType.APPLY_GRAVITY,
                new GravityOutcome());
        outcomeMap.put(EventOutcome.OutcomeType.PATROL,
                new PatrolOutcome(gameController.getMapObject()));
        outcomeMap.put(EventOutcome.OutcomeType.LOSE_GAME,
                new LoseGameOutcome());
    }


    /**
     * executes outcome using parameter map using game controller
     * @param outcomeType
     * @param gameObject
     */
    public void executeOutcome(EventOutcome.OutcomeType outcomeType, GameObject gameObject) {
        Outcome outcome = outcomeMap.get(outcomeType);
        outcome.execute(gameObject);
    }


}
