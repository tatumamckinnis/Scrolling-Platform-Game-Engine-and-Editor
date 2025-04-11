package oogasalad.engine.event.outcome;

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
 * Interface representing outcome execution logic.
 *
 * Implementers need only override the one-parameter execute() method.
 * The three-parameter version is provided as a default method that
 * simply calls the one-parameter version.
 *
 *  @author Gage Garcia
 * @author Billy McCune
 */
public interface Outcome {
  /**
   * A default method that takes additional properties.
   * Its default behavior is to ignore the additional properties and
   * delegate to the one-parameter method.
   *
   * @param gameObject the specified game object
   * @param stringParameters extra string properties
   * @param doubleParameters extra double properties
   * @throws LayerParseException
   * @throws EventParseException
   * @throws BlueprintParseException
   * @throws IOException
   * @throws InvocationTargetException
   * @throws NoSuchMethodException
   * @throws IllegalAccessException
   * @throws DataFormatException
   * @throws LevelDataParseException
   * @throws PropertyParsingException
   * @throws SpriteParseException
   * @throws HitBoxParseException
   * @throws GameObjectParseException
   * @throws ClassNotFoundException
   * @throws InstantiationException
   */
  void execute(GameObject gameObject,
      Map<String, String> stringParameters,
      Map<String, Double> doubleParameters)
      throws LayerParseException, EventParseException, BlueprintParseException, IOException,
      InvocationTargetException, NoSuchMethodException, IllegalAccessException,
      DataFormatException, LevelDataParseException, PropertyParsingException,
      SpriteParseException, HitBoxParseException, GameObjectParseException,
      ClassNotFoundException, InstantiationException;
}