package oogasalad.engine.controller.api;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
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
import oogasalad.fileparser.records.GameObjectData;

/**
 * Interface used for selecting a new Game or new Level of a Game
 *
 * @author Alana Zinkin
 */
public interface LevelAPI {

  /**
   * selects a new Game or Level
   *
   * @throws DataFormatException if the filepath is not properly formatted
   * @throws IOException         if a file cannot be retrieved
   */
  void selectGame(String filePath)
      throws DataFormatException, IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, LevelDataParseException, PropertyParsingException, SpriteParseException, EventParseException, HitBoxParseException, BlueprintParseException, GameObjectParseException, LayerParseException;

  GameObject makeObjectFromData(GameObjectData gameObjectData);
  /**
   * Lists all available levels to play
   *
   * @return list of string representations of "game/level"
   */
  List<String> listLevels();
}
