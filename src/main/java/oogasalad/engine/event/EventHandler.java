package oogasalad.engine.event;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.DataFormatException;
import oogasalad.exceptions.BlueprintParseException;
import oogasalad.exceptions.EventParseException;
import oogasalad.exceptions.GameObjectParseException;
import oogasalad.exceptions.HitBoxParseException;
import oogasalad.exceptions.LayerParseException;
import oogasalad.exceptions.LevelDataParseException;
import oogasalad.exceptions.PropertyParsingException;
import oogasalad.exceptions.SpriteParseException;

/**
 * API to process events
 *
 * @author Gage Garcia
 */
public interface EventHandler {

  /**
   * handles event by checking its condition/executing its outcome condition checking done [[A OR B]
   * AND [C OR D]  AND [E OR F]]
   *
   * @param event event model to handle
   */
  void handleEvent(Event event)
      throws LayerParseException, EventParseException, BlueprintParseException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, DataFormatException, LevelDataParseException, PropertyParsingException, SpriteParseException, HitBoxParseException, GameObjectParseException, ClassNotFoundException, InstantiationException;
}
