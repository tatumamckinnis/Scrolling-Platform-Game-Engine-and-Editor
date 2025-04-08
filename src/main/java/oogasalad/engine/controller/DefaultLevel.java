package oogasalad.engine.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import oogasalad.engine.controller.api.GameControllerAPI;
import oogasalad.engine.controller.api.LevelAPI;
import oogasalad.fileparser.DefaultFileParser;
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
  Logger LOG = Logger.getLogger(DefaultLevel.class.getName());
  private FileParserAPI myFileParser;
  private GameControllerAPI myGameController;
  private static final String LEVEL_FILE_PATH = System.getProperty("user.dir") + "/oogasalad_team03/data/gameData/levels/";

  public DefaultLevel(GameControllerAPI gameController) {
    myFileParser = new DefaultFileParser();
    myGameController = gameController;
  }

  /**
   * Select game to load, updating game controller data
   * @param level    String level name of the game(requires .xml)
   */
  @Override
  public void selectGame(String level) {
    LOG.info(LEVEL_FILE_PATH);
    String filePath = LEVEL_FILE_PATH + level;
    LevelData levelData = myFileParser.parseLevelFile(filePath);
    myGameController.setLevelData(levelData);
  }

  /**
   * List all saved level files
   * @return
   */
  @Override
  public List<String> listLevels() {
    List<String> levels = new ArrayList<>();
    File levelsDir = new File(LEVEL_FILE_PATH);

    if (levelsDir.exists() && levelsDir.isDirectory()) {
      File[] files = levelsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));
      if (files != null) {
        for (File file : files) {
          levels.add(file.getName()); // or file.getName().replace(".xml", "") if you want cleaner names
        }
      }
    }
    return levels;
  }

}
