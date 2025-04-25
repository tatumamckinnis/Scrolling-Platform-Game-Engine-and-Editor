package oogasalad.engine.model.event.condition;

import java.util.Map;
import oogasalad.engine.model.object.GameObject;

/**
 * A {@link Condition} that is satisfied when a {@link GameObject}'s X-coordinate is greater than or
 * equal to a specified threshold.
 * <p>
 * The threshold value is provided in the {@code doubleParams} map under the key {@code "x"}.
 * </p>
 *
 * @author Billy McCune
 */
public class AtOrBeyondXCondition implements Condition {

  /**
   * Determines whether the given {@code gameObject} has reached or passed the X position specified
   * by the parameter {@code "x"}.
   *
   * @param gameObject   the object whose X position will be evaluated
   * @param stringParams a map of optional string parameters (unused by this condition)
   * @param doubleParams a map containing numeric parameters; must include the key {@code "x"} whose
   *                     value is the target X-coordinate threshold
   * @return {@code true} if {@code gameObject.getXPosition()} is greater than or equal to the
   * threshold; {@code false} otherwise
   * @throws NullPointerException     if {@code doubleParams} is null or does not contain "x"
   * @throws IllegalArgumentException if the value for "x" cannot be converted to an integer
   */
  @Override
  public boolean isMet(GameObject gameObject,
      Map<String, String> stringParams,
      Map<String, Double> doubleParams) {
    int xToMeet = doubleParams.get("x").intValue();
    return gameObject.getXPosition() >= xToMeet;
  }
}
