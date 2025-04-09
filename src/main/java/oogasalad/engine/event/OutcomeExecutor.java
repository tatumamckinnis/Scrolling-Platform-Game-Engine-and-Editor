/**
 * Updates game state to reflect event outcome
 *
 * @author Gage Garcia
 */
package oogasalad.engine.event;

import java.util.HashMap;
import java.util.Map;
import oogasalad.engine.controller.api.GameExecutor;
import oogasalad.engine.event.outcome.DestroyObjectOutcome;
import oogasalad.engine.event.outcome.EventOutcome;
import oogasalad.engine.event.outcome.GravityOutcome;
import oogasalad.engine.event.outcome.JumpOutcome;
import oogasalad.engine.event.outcome.LoseGameOutcome;
import oogasalad.engine.event.outcome.MoveLeftOutcome;
import oogasalad.engine.event.outcome.MoveRightOutcome;
import oogasalad.engine.event.outcome.Outcome;
import oogasalad.engine.event.outcome.PatrolOutcome;
import oogasalad.engine.event.outcome.PlatformPassThroughOutcome;
import oogasalad.engine.model.object.GameObject;

public class OutcomeExecutor {

  private final Map<EventOutcome.OutcomeType, Outcome> outcomeMap;

  /**
   * Initialize the executor with a game controller
   *
   * @param gameExecutor api that allows updates to game state Initialize mapping of outcome enum to
   *                     outcome interface
   */
  public OutcomeExecutor(CollisionHandler collisionHandler, GameExecutor gameExecutor) {
    this.outcomeMap = new HashMap<>();
    outcomeMap.put(EventOutcome.OutcomeType.MOVE_RIGHT,
        new MoveRightOutcome());
    outcomeMap.put(EventOutcome.OutcomeType.MOVE_LEFT,
        new MoveLeftOutcome());
    outcomeMap.put(EventOutcome.OutcomeType.JUMP,
        new JumpOutcome());
    outcomeMap.put(EventOutcome.OutcomeType.APPLY_GRAVITY,
        new GravityOutcome(collisionHandler));
    outcomeMap.put(EventOutcome.OutcomeType.PATROL,
        new PatrolOutcome(gameExecutor));
    outcomeMap.put(EventOutcome.OutcomeType.LOSE_GAME,
        new LoseGameOutcome());
    outcomeMap.put(EventOutcome.OutcomeType.DESTROY_OBJECT,
        new DestroyObjectOutcome(gameExecutor));
    outcomeMap.put(EventOutcome.OutcomeType.PLATFORM_PASS_THROUGH_BEHAVIOR,
        new PlatformPassThroughOutcome(collisionHandler));
  }


  /**
   * executes outcome using parameter map using game controller
   *
   * @param outcomeType
   * @param gameObject
   */
  public void executeOutcome(EventOutcome.OutcomeType outcomeType, GameObject gameObject) {
    Outcome outcome = outcomeMap.get(outcomeType);
    outcome.execute(gameObject);
  }


}
