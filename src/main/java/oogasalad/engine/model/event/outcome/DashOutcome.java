package oogasalad.engine.model.event.outcome;

import java.awt.Point;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.zip.DataFormatException;
import oogasalad.engine.controller.api.InputProvider;
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
 * Propels object in direction of mouse
 *
 * @author Gage Garcia
 */
public class DashOutcome implements Outcome {
  private final InputProvider inputProvider;

  public DashOutcome(InputProvider inputProvider) {
    this.inputProvider = inputProvider;
  }
  @Override
  public void execute(GameObject gameObject, Map<String, String> stringParameters,
      Map<String, Double> doubleParameters)
      throws LayerParseException, EventParseException, BlueprintParseException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, DataFormatException, LevelDataParseException, PropertyParsingException, SpriteParseException, HitBoxParseException, GameObjectParseException, ClassNotFoundException, InstantiationException {
    Point mouse = inputProvider.getMousePosition();
    double targetX = mouse.x;
    double targetY = mouse.y;

    // 2) your desired speed magnitude
    double speed = doubleParameters.getOrDefault("amount", 3.0);

    // 3) compute direction vector from object → mouse
    double dx = targetX - gameObject.getXPosition();
    double dy = targetY - gameObject.getYPosition();

    // 4) normalize (length = √(dx²+dy²))
    double length = Math.hypot(dx, dy);
    if (length > 0) {
      double ux = dx / length;   // unit vector x
      double uy = dy / length;   // unit vector y

      // 5) apply velocity in that direction
      //    (origin top‐left, so positive y goes downwards)
      gameObject.setXVelocity(speed * ux);
      gameObject.setYVelocity(speed * uy);
    }
  }
}
