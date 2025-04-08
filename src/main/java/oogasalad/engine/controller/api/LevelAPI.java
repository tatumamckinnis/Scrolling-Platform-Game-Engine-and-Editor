package oogasalad.engine.controller.api;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.zip.DataFormatException;

/**
 * Interface used for selecting a new Game or new Level of a Game
 */
public interface LevelAPI {

  /**
   * selects a new Game or Level
   *
   * @param level    level of the game
   * @throws DataFormatException if the filepath is not properly formatted
   * @throws IOException         if a file cannot be retrieved
   */
  void selectGame(String level)
      throws DataFormatException, IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException;

  /**
   * Lists all available levels to play
   * @return list of string representations of "game/level"
   */
  List<String> listLevels();
}
