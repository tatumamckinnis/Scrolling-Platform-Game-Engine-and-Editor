package oogasalad.engine.model.event.outcome;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.zip.DataFormatException;
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
 * An {@code Outcome} implementation that teleports a {@link GameObject}
 * to a specific point defined by numeric parameters.
 * <p>
 * Reads optional keys "x" and "y" from the provided {@code doubleParameters} map:
 * <ul>
 *   <li>If "x" is present, the GameObject’s X position is set to that value.</li>
 *   <li>If "y" is present, the GameObject’s Y position is set to that value.</li>
 * </ul>
 * Any axis without a corresponding key remains at its current coordinate.
 */
public class TeleportObjectToPointOutcome implements Outcome {

  /**
   * Teleports the given {@code gameObject} to the coordinates specified
   * in {@code doubleParameters}.
   *
   * @param gameObject       the object whose position will be updated
   * @param stringParameters a map of string parameters (unused by this outcome)
   * @param doubleParameters a map of numeric parameters; supported keys:
   *                         <ul>
   *                           <li>{@code "x"} – the new X coordinate</li>
   *                           <li>{@code "y"} – the new Y coordinate</li>
   *                         </ul>
   *                         If a key is absent, that axis is not modified.
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
      Map<String, String> stringParameters,
      Map<String, Double> doubleParameters)
      throws LayerParseException,
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

    if (doubleParameters.containsKey("x")) {
      gameObject.setXPosition(doubleParameters.get("x").intValue());
    }

    if (doubleParameters.containsKey("y")) {
      gameObject.setYPosition(doubleParameters.get("y").intValue());
    }
  }
}
