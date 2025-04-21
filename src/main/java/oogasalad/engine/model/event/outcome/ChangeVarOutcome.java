package oogasalad.engine.model.event.outcome;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.zip.DataFormatException;
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.model.object.Player;
import oogasalad.exceptions.BlueprintParseException;
import oogasalad.exceptions.EventParseException;
import oogasalad.exceptions.GameObjectParseException;
import oogasalad.exceptions.HitBoxParseException;
import oogasalad.exceptions.LayerParseException;
import oogasalad.exceptions.LevelDataParseException;
import oogasalad.exceptions.PropertyParsingException;
import oogasalad.exceptions.SpriteParseException;

/**
 * changes a dynamic user-named double by a delta amount
 *
 * @author Gage Garcia and Alana Zinkin
 */
public class ChangeVarOutcome implements Outcome {

  @Override
  public void execute(GameObject gameObject, Map<String, String> stringParameters,
      Map<String, Double> doubleParameters)
      throws LayerParseException, EventParseException, BlueprintParseException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, DataFormatException, LevelDataParseException, PropertyParsingException, SpriteParseException, HitBoxParseException, GameObjectParseException, ClassNotFoundException, InstantiationException {
    String variable = stringParameters.get("variable");
    double delta = doubleParameters.getOrDefault("delta", 0.0);
    Double curAmount = gameObject.getDoubleParams().getOrDefault(variable, 0.0);
    double newAmount = curAmount + delta;
    gameObject.getDoubleParams().put(variable, newAmount);
    System.out.println("Variable " + variable + " changed to " + newAmount);
  }

  /**
   * updates the displayed stats of the player objects - only updates if the stat is already
   * contained within the map
   *
   * @param gameObject game object to check
   * @param variable   the variable to update
   * @param newAmount  the new amount to update the variable to
   */
//  private static void updateDisplayedStats(GameObject gameObject, String variable,
//      double newAmount) {
//    if (gameObject.getType().equals("player") && ((Player) gameObject).getDisplayedStats()
//        .containsKey(
//            variable)) {
//      ((Player) gameObject).setDisplayedStat(variable, newAmount);
//    }
//  }
}
