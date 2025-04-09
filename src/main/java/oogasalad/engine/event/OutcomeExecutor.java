/**
 * Updates game state to reflect event outcome
 * @author Gage Garcia
 */
package oogasalad.engine.event;
import oogasalad.engine.controller.api.GameExecutor;
import oogasalad.engine.event.outcome.*;
import oogasalad.engine.model.object.GameObject;

import java.util.HashMap;
import java.util.Map;

public class OutcomeExecutor {
    private final Map<EventOutcome.OutcomeType, Outcome> outcomeMap;

    /**
     * Initialize the executor with a game controller
     * @param gameController
     * Initialize mapping of outcome enum to outcome interface
     */
    public OutcomeExecutor(CollisionHandler collisionHandler, GameExecutor gameExecutor) {
        this.outcomeMap = new HashMap<>();
        outcomeMap.put(EventOutcome.OutcomeType.MOVE_RIGHT,
                new MoveRightOutcome());
        outcomeMap.put(EventOutcome.OutcomeType.JUMP,
                new JumpOutcome());
        outcomeMap.put(EventOutcome.OutcomeType.APPLY_GRAVITY,
                new GravityOutcome());
        outcomeMap.put(EventOutcome.OutcomeType.PATROL,
                new PatrolOutcome(gameExecutor));
        outcomeMap.put(EventOutcome.OutcomeType.LOSE_GAME,
                new LoseGameOutcome());
        outcomeMap.put(EventOutcome.OutcomeType.DESTROY_OBJECT,
                new DestroyObjectOutcome(gameExecutor));
        outcomeMap.put(EventOutcome.OutcomeType.PLATFORM_PASS_THROUGH_BEHAVIOR,
                new PlatformPassThroughOutcome(collisionHandler));
        outcomeMap.put(EventOutcome.OutcomeType.MOVE_LEFT,
            new MoveLeftOutcome());
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
