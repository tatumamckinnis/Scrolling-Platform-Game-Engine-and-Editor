package oogasalad.engine.model.event;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.zip.DataFormatException;
import oogasalad.engine.controller.api.GameExecutor;
import oogasalad.engine.controller.api.InputProvider;
import oogasalad.engine.model.animation.AnimationHandlerApi;
import oogasalad.engine.model.animation.DefaultAnimationHandler;
import oogasalad.engine.model.event.condition.EventCondition;
import oogasalad.engine.model.event.outcome.EventOutcome;
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
 * Event handling class that implements event EventHandler interface
 *
 * @author Gage Garcia
 */
public class DefaultEventHandler implements EventHandler {

  private final ConditionChecker conditionChecker;
  private final OutcomeExecutor outcomeExecutor;

  /**
   * Initializes event handler
   *
   * @param gameExecutor interface that allows outcome updates to game state
   */
  public DefaultEventHandler(InputProvider inputProvider, CollisionHandler collisionHandler,
      GameExecutor gameExecutor, DefaultAnimationHandler animationHandlerApi) {
    outcomeExecutor = new OutcomeExecutor(collisionHandler, gameExecutor, animationHandlerApi, inputProvider);
    conditionChecker = new ConditionChecker(inputProvider, collisionHandler);
  }

  /**
   * Processes the event object
   *
   * @param event event model to handle
   */
  public void handleEvent(Event event)
      throws LayerParseException, EventParseException, BlueprintParseException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, DataFormatException, LevelDataParseException, PropertyParsingException, SpriteParseException, HitBoxParseException, GameObjectParseException, ClassNotFoundException, InstantiationException {

    if (isValidEvent(event)) {
      for (EventOutcome outcome : event.getOutcomes()) {
        outcomeExecutor.executeOutcome(outcome, event.getGameObject());
      }
    }
  }

  private boolean isValidEvent(Event event) {
    boolean validEvent = true;
    GameObject gameObject = event.getGameObject();
    List<List<EventCondition>> conditionGroups = event.getConditions();

    for (List<EventCondition> conditionGroup : conditionGroups) {
      boolean validGroup = false; // This group is false until proven true

      for (EventCondition eventCondition : conditionGroup) {
        if (conditionChecker.checkCondition(eventCondition, gameObject)) {
          validGroup = true; // One condition in this OR-group is true
          break; // No need to check further in this OR-group
        }
      }

      if (!validGroup) { // If the OR-group never became true, entire event is invalid
        return false;
      }
    }
    return validEvent;
  }
}
