package oogasalad.engine.controller;

import java.io.IOException;
import java.util.zip.DataFormatException;

/**
 * Interface used for selecting a new Game or new Level of a Game
 */
public interface LevelAPI {

  /**
   * selects a new Game or Level
   *
   * @param game     game folder to search
   * @param category category of game to play
   * @param level    level of the game
   * @throws DataFormatException if the filepath is not properly formatted
   * @throws IOException         if a file cannot be retrieved
   */
  public void selectGame(String game, String category, String level)
      throws DataFormatException, IOException;

}
