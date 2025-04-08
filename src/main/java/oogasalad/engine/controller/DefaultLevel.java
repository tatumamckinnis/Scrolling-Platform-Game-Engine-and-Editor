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

  public DefaultLevel(GameControllerAPI gameController) {
    myFileParser = new DefaultFileParser();
    myGameController = gameController;
  }

  @Override
  public void selectGame(String game, String category, String level) {
    //String filePath =  System.getProperty("user.dir") + "/oogasalad_team03/data/gameData/levels/dinosaurgame/DinoLevel1.xml";
    String filePath = System.getProperty("user.dir") + "/oogasalad_team03/data/gameData/levels/" + game + "/" + level;
    LOG.info(filePath);
    LevelData levelData = myFileParser.parseLevelFile(filePath);
    myGameController.setLevelData(levelData);
  }

  @Override
  public List<String> listLevels() {
    List<String> levels = new ArrayList<>();
    String levelsDirPath = System.getProperty("user.dir") + "/oogasalad_team03/data/gameData/levels/dinosaurgame";
    File levelsDir = new File(levelsDirPath);

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
