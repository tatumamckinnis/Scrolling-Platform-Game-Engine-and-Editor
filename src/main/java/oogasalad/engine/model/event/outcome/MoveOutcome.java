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
 * Move outcome moves the object
 *
 * @author Billy McCune
 */
public class MoveOutcome implements Outcome {

  @Override
  public void execute(GameObject gameObject, Map<String, String> stringParameters,
      Map<String, Double> doubleParameters)
      throws LayerParseException, EventParseException, BlueprintParseException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, DataFormatException, LevelDataParseException, PropertyParsingException, SpriteParseException, HitBoxParseException, GameObjectParseException, ClassNotFoundException, InstantiationException {
    double distance = doubleParameters.getOrDefault("amount", 4.0);
    double angle = doubleParameters.getOrDefault("angle", 0.0);
    double angleInRadians = Math.toRadians(angle);
    double dx  = distance * Math.cos(angleInRadians);
    double dy  = distance * Math.sin(angleInRadians);
    gameObject.setXPosition((int) (gameObject.getXPosition() + dx));
    gameObject.setYPosition((int) (gameObject.getYPosition() + dy));
  }
}
