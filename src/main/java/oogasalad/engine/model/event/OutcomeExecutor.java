package oogasalad.engine.model.event;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;
import oogasalad.engine.controller.api.GameExecutor;
import oogasalad.engine.model.animation.AnimationHandlerApi;
import oogasalad.engine.model.animation.DefaultAnimationHandler;
import oogasalad.engine.model.event.outcome.AddToAnimationsOutcome;
import oogasalad.engine.model.event.outcome.ChangeObjectOutcome;
import oogasalad.engine.model.event.outcome.ChangeVarOutcome;
import oogasalad.engine.model.event.outcome.DestroyObjectOutcome;
import oogasalad.engine.model.event.outcome.EventOutcome;
import oogasalad.engine.model.event.outcome.EventOutcome.OutcomeType;
import oogasalad.engine.model.event.outcome.GravityOutcome;
import oogasalad.engine.model.event.outcome.JumpOutcome;
import oogasalad.engine.model.event.outcome.LoseGameOutcome;
import oogasalad.engine.model.event.outcome.MoveLeftOutcome;
import oogasalad.engine.model.event.outcome.MoveOutcome;
import oogasalad.engine.model.event.outcome.MoveRightOutcome;
import oogasalad.engine.model.event.outcome.Outcome;
import oogasalad.engine.model.event.outcome.PatrolOutcome;
import oogasalad.engine.model.event.outcome.PlatformPassThroughOutcome;
import oogasalad.engine.model.event.outcome.RestartLevelOutcome;
import oogasalad.engine.model.event.outcome.RocketOutcome;
import oogasalad.engine.model.event.outcome.RunObjectsAnimationsOutcome;
import oogasalad.engine.model.event.outcome.SelectLevelOutcome;
import oogasalad.engine.model.event.outcome.SetBaseFrameOutcome;
import oogasalad.engine.model.event.outcome.SetVarOutcome;
import oogasalad.engine.model.event.outcome.SpawnNewObjectOutcome;
import oogasalad.engine.model.event.outcome.SpawnOnObjectOutcome;
import oogasalad.engine.model.event.outcome.TeleportObjectToPointOutcome;
import oogasalad.engine.model.event.outcome.TeleportObjectToRandomPointOutcome;
import oogasalad.engine.model.event.outcome.stopObjectAnimationsOutcome;
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
  public OutcomeExecutor(CollisionHandler collisionHandler, GameExecutor gameExecutor, AnimationHandlerApi animationHandler) {
    this.outcomeMap = new HashMap<>();
    outcomeMap.put(EventOutcome.OutcomeType.MOVE_RIGHT, new MoveRightOutcome());
    outcomeMap.put(EventOutcome.OutcomeType.JUMP, new JumpOutcome());
    outcomeMap.put(EventOutcome.OutcomeType.ROCKET, new RocketOutcome());
    outcomeMap.put(EventOutcome.OutcomeType.APPLY_GRAVITY, new GravityOutcome(collisionHandler));
    outcomeMap.put(EventOutcome.OutcomeType.PATROL, new PatrolOutcome(gameExecutor));
    outcomeMap.put(EventOutcome.OutcomeType.LOSE_GAME, new LoseGameOutcome(gameExecutor));
    outcomeMap.put(EventOutcome.OutcomeType.DESTROY_OBJECT, new DestroyObjectOutcome(gameExecutor));
    outcomeMap.put(EventOutcome.OutcomeType.PLATFORM_PASS_THROUGH_BEHAVIOR, new PlatformPassThroughOutcome(collisionHandler));
    outcomeMap.put(EventOutcome.OutcomeType.MOVE_LEFT, new MoveLeftOutcome());
    outcomeMap.put(EventOutcome.OutcomeType.RESTART_LEVEL, new RestartLevelOutcome(gameExecutor));
    outcomeMap.put(EventOutcome.OutcomeType.SELECT_LEVEL, new SelectLevelOutcome(gameExecutor));
    outcomeMap.put(OutcomeType.CHANGE_VAR, new ChangeVarOutcome());
    outcomeMap.put(OutcomeType.SET_VAR, new SetVarOutcome());
    outcomeMap.put(OutcomeType.ADD_ANIMATION, new AddToAnimationsOutcome(animationHandler));
    outcomeMap.put(OutcomeType.RUN_OBJECT_ANIMATIONS, new RunObjectsAnimationsOutcome(animationHandler));
    outcomeMap.put(OutcomeType.STOP_OBJECT_ANIMATIONS, new stopObjectAnimationsOutcome(animationHandler));
    outcomeMap.put(OutcomeType.MOVE, new MoveOutcome());
    outcomeMap.put(OutcomeType.SET_BASE_FRAME, new SetBaseFrameOutcome(animationHandler));
    outcomeMap.put(OutcomeType.TELEPORT_TO_POINT, new TeleportObjectToPointOutcome());
    outcomeMap.put(OutcomeType.TELEPORT_TO_RANDOM_POINT, new TeleportObjectToRandomPointOutcome());
    outcomeMap.put(OutcomeType.SPAWN_NEW_OBJECT, new SpawnNewObjectOutcome(gameExecutor));
    outcomeMap.put(OutcomeType.SPAWN_ON_OBJECT, new SpawnOnObjectOutcome(gameExecutor));
    outcomeMap.put(OutcomeType.CHANGE_OBJECT, new ChangeObjectOutcome(gameExecutor));
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
