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
 * Outcome that enqueues an animation on the target GameObject.
 * It reads the 'animationName' parameter and uses the DefaultAnimationHandler
 * to add it to the object's animation queue.
 */
public class AddToAnimationsOutcome implements Outcome {
  private final AnimationHandlerApi animationHandler;

  /**
   * @param animationHandler the handler responsible for managing animations
   */
  public AddToAnimationsOutcome(AnimationHandlerApi animationHandler) {
    this.animationHandler = animationHandler;
  }

  /**
   * Executes this outcome by retrieving the 'animationName' from stringParameters
   * and adding it to the GameObject's animation queue.
   * @param gameObject the object to animate
   * @param stringParameters must contain key 'animationName'
   * @param doubleParameters unused by this outcome
   * @throws EventParseException if 'animationName' is missing or empty
   * @throws LayerParseException, BlueprintParseException, etc. if downstream errors occur
   */
  @Override
  public void execute(GameObject gameObject,
      Map<String, String> stringParameters,
      Map<String, Double> doubleParameters)
      throws LayerParseException, EventParseException, BlueprintParseException,
      IOException, InvocationTargetException, NoSuchMethodException,
      IllegalAccessException, DataFormatException, LevelDataParseException,
      PropertyParsingException, SpriteParseException,
      HitBoxParseException, GameObjectParseException, ClassNotFoundException,
      InstantiationException {
    String animationName = stringParameters.get("animationName");
    if (animationName == null || animationName.isEmpty()) {
      throw new EventParseException(
          "Missing or empty 'animationName' parameter for AddToAnimationsOutcome");
    }
    animationHandler.addToAnimations(gameObject, animationName);
  }
}

