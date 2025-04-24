package oogasalad.engine.model.event.outcome;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.zip.DataFormatException;
import oogasalad.engine.model.object.GameObject;
import java.util.SplittableRandom;
import oogasalad.exceptions.BlueprintParseException;
import oogasalad.exceptions.EventParseException;
import oogasalad.exceptions.GameObjectParseException;
import oogasalad.exceptions.HitBoxParseException;
import oogasalad.exceptions.LayerParseException;
import oogasalad.exceptions.LevelDataParseException;
import oogasalad.exceptions.PropertyParsingException;
import oogasalad.exceptions.SpriteParseException;

/**
 * An {@code Outcome} that teleports a {@link GameObject} to a random point
 * within a rectangular area defined by minimum and maximum x/y coordinates.
 * <p>
 * The bounds are provided via the {@code doubleParams} map under the keys:
 * <ul>
 *   <li>{@code "xMin"} – minimum x coordinate (inclusive)</li>
 *   <li>{@code "xMax"} – maximum x coordinate (inclusive)</li>
 *   <li>{@code "yMin"} – minimum y coordinate (inclusive)</li>
 *   <li>{@code "yMax"} – maximum y coordinate (inclusive)</li>
 * </ul>
 * If any of these keys is missing, the corresponding min defaults to 0
 * and max defaults to the same as min, resulting in no movement along that axis.
 * <p>
 * If {@code xMin > xMax} or {@code yMin > yMax}, the values will be swapped
 * to ensure valid ranges.
 */
public class TeleportObjectToRandomPointOutcome implements Outcome {

  /**
   * Shared pseudo‑random number generator used to compute the new coordinates.
   */
  private static final SplittableRandom randomGenerator = new SplittableRandom();

  /**
   * Teleports the given {@code gameObject} to a uniformly random position
   * within the rectangle [xMin, xMax] × [yMin, yMax].
   *
   * @param gameObject   the object whose position will be updated
   * @param stringParams an optional map of string parameters (not used)
   * @param doubleParams a map supplying numeric bounds:
   *                     <ul>
   *                       <li>{@code "xMin"} – lower bound for x (inclusive)</li>
   *                       <li>{@code "xMax"} – upper bound for x (inclusive)</li>
   *                       <li>{@code "yMin"} – lower bound for y (inclusive)</li>
   *                       <li>{@code "yMax"} – upper bound for y (inclusive)</li>
   *                     </ul>
   *
   * @throws LayerParseException       if layer parsing fails
   * @throws EventParseException       if event parsing fails
   * @throws BlueprintParseException   if blueprint parsing fails
   * @throws IOException               if an I/O error occurs
   * @throws InvocationTargetException if a reflective invocation fails
   * @throws NoSuchMethodException     if a required reflective method is missing
   * @throws IllegalAccessException    if a reflective method cannot be accessed
   * @throws DataFormatException       if compressed data is invalid
   * @throws LevelDataParseException   if level data parsing fails
   * @throws PropertyParsingException  if property parsing fails
   * @throws SpriteParseException      if sprite parsing fails
   * @throws HitBoxParseException      if hit‑box parsing fails
   * @throws GameObjectParseException  if game‑object parsing fails
   * @throws ClassNotFoundException    if a referenced class cannot be found
   * @throws InstantiationException    if an instance cannot be created reflectively
   */
  @Override
  public void execute(GameObject gameObject,
      Map<String,String> stringParams,
      Map<String,Double> doubleParams)  throws LayerParseException,
      EventParseException,
      BlueprintParseException,
      IOException,
      InvocationTargetException,
      NoSuchMethodException,
      IllegalAccessException,
      DataFormatException,
      LevelDataParseException,
      PropertyParsingException,
      SpriteParseException,
      HitBoxParseException,
      GameObjectParseException,
      ClassNotFoundException,
      InstantiationException {

    int xMin = doubleParams.getOrDefault("xMin", 0.0).intValue();
    int xMax = doubleParams.getOrDefault("xMax", (double) xMin).intValue();
    int yMin = doubleParams.getOrDefault("yMin", 0.0).intValue();
    int yMax = doubleParams.getOrDefault("yMax", (double) yMin).intValue();

    // Swap if bounds are inverted
    if (xMin > xMax) { int tmp = xMin; xMin = xMax; xMax = tmp; }
    if (yMin > yMax) { int tmp = yMin; yMin = yMax; yMax = tmp; }

    // Compute random coordinates within the inclusive range
    int newX = randomGenerator.nextInt(xMin, xMax + 1);
    int newY = randomGenerator.nextInt(yMin, yMax + 1);
    System.out.println("newX: " + newX + " newY: " + newY);
    // Apply teleportation
    gameObject.setXPosition(newX);
    gameObject.setYPosition(newY);
  }
}