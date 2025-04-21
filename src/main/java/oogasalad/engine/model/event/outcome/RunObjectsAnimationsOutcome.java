package oogasalad.engine.model.event.outcome;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.zip.DataFormatException;
import oogasalad.engine.model.animation.AnimationHandlerApi;
import oogasalad.engine.model.animation.DefaultAnimationHandler;
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
 * Outcome that advances the animation for the target GameObject by one tick
 * and sets its current frame accordingly.
 */
public class RunObjectsAnimationsOutcome implements Outcome {

  private final AnimationHandlerApi animationHandler;

  /**
   * @param animationHandler the handler responsible for managing animations
   */
  public RunObjectsAnimationsOutcome(AnimationHandlerApi animationHandler) {
    this.animationHandler = animationHandler;
  }

  /**
   * Executes this outcome by advancing the animation state for the given GameObject and updating
   * its current frame.
   *
   * @param gameObject       the object whose animation to advance
   * @param stringParameters unused by this outcome
   * @param doubleParameters unused by this outcome
   * @throws LayerParseException if animation advancement fails
   */
  @Override
  public void execute(GameObject gameObject, Map<String, String> stringParameters,
      Map<String, Double> doubleParameters)
      throws LayerParseException, EventParseException, BlueprintParseException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, DataFormatException, LevelDataParseException, PropertyParsingException, SpriteParseException, HitBoxParseException, GameObjectParseException, ClassNotFoundException, InstantiationException {
   gameObject.setCurrentFrame(animationHandler.getCurrentFrameInAnimation(gameObject));
  }
}

