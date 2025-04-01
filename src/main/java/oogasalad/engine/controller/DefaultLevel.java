package oogasalad.engine.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.DataFormatException;
import oogasalad.fileparser.FileParserAPI;
import oogasalad.fileparser.records.LevelData;

/**
 * Default implementation of the {@link LevelAPI}.
 *
 * <p>This class is responsible for selecting and loading a specific level from a given game,
 * category, and level name. It uses a {@link FileParserAPI} to parse the level file and delegates
 * to the {@link GameControllerAPI} to update the engine with the parsed data.
 */
public class DefaultLevel implements LevelAPI {

  /**
   * API for parsing level files
   */
  private FileParserAPI myFileParser;

  /**
   * Controller responsible for managing game state
   */
  private GameControllerAPI myGameController;

  /**
   * Selects a game level based on the provided game, category, and level identifiers.
   * <p>
   * This method constructs the file path, parses the corresponding level file using the file
   * parser, and passes the resulting {@link LevelData} to the game controller to initialize the
   * game state.
   *
   * @param game     the name of the game
   * @param category the category or world within the game
   * @param level    the specific level identifier
   * @throws DataFormatException       if the level file has an invalid format
   * @throws IOException               if there is an error reading the file
   * @throws ClassNotFoundException    if a required class in the level data is missing
   * @throws InvocationTargetException if an error occurs during object creation
   * @throws NoSuchMethodException     if the constructor for a class is not found
   * @throws InstantiationException    if an object cannot be instantiated
   * @throws IllegalAccessException    if access to a constructor is denied
   *
   *  @author Alana Zinkin
   */
  @Override
  public void selectGame(String game, String category, String level)
      throws DataFormatException, IOException, ClassNotFoundException, InvocationTargetException,
      NoSuchMethodException, InstantiationException, IllegalAccessException {
    String filePath = game + "/" + category + "/" + level;
    LevelData levelData = myFileParser.parseLevelFile(filePath);
    myGameController.setLevelData(levelData);
  }
}
