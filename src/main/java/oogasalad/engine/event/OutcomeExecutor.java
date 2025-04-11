package oogasalad.engine.event;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;
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
import oogasalad.engine.event.outcome.RestartLevelOutcome;
import oogasalad.engine.model.object.GameObject;
import oogasalad.exceptions.BlueprintParseException;
import oogasalad.exceptions.EventParseException;
import oogasalad.exceptions.GameObjectParseException;
import oogasalad.exceptions.HitBoxParseException;
import oogasalad.exceptions.LayerParseException;
import oogasalad.exceptions.LevelDataParseException;
import oogasalad.exceptions.PropertyParsingException;
import oogasalad.exceptions.SpriteParseException;

/**
 * Updates game state to reflect event outcome
 *
 * @author Gage Garcia
 */
public class OutcomeExecutor {

  /**
   * Initialize the executor with a game controller
   *
   * @param gameExecutor Initialize mapping of outcome enum to outcome interface
   */
  public OutcomeExecutor(CollisionHandler collisionHandler, GameExecutor gameExecutor) {
    this.outcomeMap = new HashMap<>();
    outcomeMap.put(EventOutcome.OutcomeType.MOVE_RIGHT,
        new MoveRightOutcome());
    outcomeMap.put(EventOutcome.OutcomeType.JUMP,
        new JumpOutcome());
    outcomeMap.put(EventOutcome.OutcomeType.APPLY_GRAVITY,
        new GravityOutcome(collisionHandler));
    outcomeMap.put(EventOutcome.OutcomeType.PATROL,
        new PatrolOutcome(gameExecutor));
    outcomeMap.put(EventOutcome.OutcomeType.LOSE_GAME,
        new LoseGameOutcome(gameExecutor));
    outcomeMap.put(EventOutcome.OutcomeType.DESTROY_OBJECT,
        new DestroyObjectOutcome(gameExecutor));
    outcomeMap.put(EventOutcome.OutcomeType.PLATFORM_PASS_THROUGH_BEHAVIOR,
        new PlatformPassThroughOutcome(collisionHandler));
    outcomeMap.put(EventOutcome.OutcomeType.MOVE_LEFT,
        new MoveLeftOutcome());
    outcomeMap.put(EventOutcome.OutcomeType.RESTART_LEVEL,
        new RestartLevelOutcome(gameExecutor));
  }

  private final Map<EventOutcome.OutcomeType, Outcome> outcomeMap;

  /**
   * executes outcome using parameter map using game controller
   *
   * @param outcomeData
   * @param gameObject
   */
  public void executeOutcome(EventOutcome outcomeData, GameObject gameObject)
      throws LayerParseException, EventParseException, BlueprintParseException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, DataFormatException, LevelDataParseException, PropertyParsingException, SpriteParseException, HitBoxParseException, GameObjectParseException, ClassNotFoundException, InstantiationException {
   Outcome outcome = outcomeMap.get(outcomeData.outcomeType());
    outcome.execute(gameObject, outcomeData.stringProperties(), outcomeData.doubleProperties());
  }


}
