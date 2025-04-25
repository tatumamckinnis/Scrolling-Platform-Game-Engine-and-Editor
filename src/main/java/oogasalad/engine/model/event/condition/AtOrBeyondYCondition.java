package oogasalad.engine.model.event.condition;

import java.util.Map;
import oogasalad.engine.model.object.GameObject;

/**
 * A {@link Condition} that is satisfied when a {@link GameObject}'s Y-coordinate
 * is greater than or equal to a specified threshold.
 * <p>
 * The threshold is supplied in {@code doubleParams} under the key {@code "y"}.
 * </p>
 *
 * @author Billy McCune
 */
public class AtOrBeyondYCondition implements Condition {

  /**
   * Evaluates whether the given {@code gameObject} has reached or passed
   * the Y position specified by the parameter {@code "y"}.
   *
   * @param gameObject   the object whose Y position will be checked
   * @param stringParams a map of optional string parameters (unused by this condition)
   * @param doubleParams a map containing numeric parameters;<br>
   *                     must include the key {@code "y"} whose value is the
   *                     target Y-coordinate threshold
   * @return {@code true} if {@code gameObject.getYPosition()} is greater than
   *         or equal to the threshold; {@code false} otherwise
   * @throws NullPointerException      if {@code doubleParams} is null or does not contain "y"
   * @throws IllegalArgumentException  if the value for "y" cannot be converted to an integer
   */
  @Override
  public boolean isMet(GameObject gameObject,
      Map<String, String> stringParams,
      Map<String, Double> doubleParams) {
    int yToMeet = doubleParams.get("y").intValue();
    return gameObject.getYPosition() >= yToMeet;
  }
}
