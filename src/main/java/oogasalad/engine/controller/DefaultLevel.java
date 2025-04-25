package oogasalad.engine.controller;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import oogasalad.engine.controller.api.EngineFileConverterAPI;
import oogasalad.engine.controller.api.GameControllerAPI;
import oogasalad.engine.controller.api.LevelAPI;
import oogasalad.engine.model.object.GameObject;
import oogasalad.exceptions.BlueprintParseException;
import oogasalad.exceptions.EventParseException;
import oogasalad.exceptions.GameObjectParseException;
import oogasalad.exceptions.HitBoxParseException;
import oogasalad.exceptions.LayerParseException;
import oogasalad.exceptions.LevelDataParseException;
import oogasalad.exceptions.PropertyParsingException;
import oogasalad.exceptions.SpriteParseException;
import oogasalad.fileparser.DefaultFileParser;
import oogasalad.fileparser.FileParserApi;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.GameObjectData;
import oogasalad.fileparser.records.LevelData;

/**
 * Default implementation of the {@link LevelAPI}.
 *
 * <p>This class is responsible for selecting and loading a specific level from a given game,
 * category, and level name. It uses a {@link FileParserApi} to parse the level file and delegates
 * to the {@link GameControllerAPI} to update the engine with the parsed data.
 *
 * @author Gage Garcia, Billy McCune
 */
public class DefaultLevel implements LevelAPI {

  private static final Logger LOG = Logger.getLogger(DefaultLevel.class.getName());
  private final FileParserApi myFileParser;
  private final GameControllerAPI myGameController;
  private static final String LEVEL_FILE_PATH =
      System.getProperty("user.dir") + "/data/gameData/levels/";
  private static LevelData levelData;

  /**
   * Default level constructor
   *
   * @param gameController the game controller manages the back-end of the game
   */
  public DefaultLevel(GameControllerAPI gameController) {
    myFileParser = new DefaultFileParser();
    myGameController = gameController;
  }

  /**
   * Select game to load, updating game controller data
   *
   * @param filePath String level name of the game(requires .xml)
   */
  @Override
  public void selectGame(String filePath)
      throws LevelDataParseException, PropertyParsingException, SpriteParseException, EventParseException, HitBoxParseException, BlueprintParseException, GameObjectParseException, LayerParseException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    LOG.info("Selecting game " + filePath);
    levelData = myFileParser.parseLevelFile(filePath);
    myGameController.setLevelData(levelData);
  }

  /**
   * Converts GameObjectData to GameObject
   */
  @Override
  public GameObject makeObjectFromData(GameObjectData gameObjectData) {
    Map<Integer, BlueprintData> gameBluePrintData = levelData.gameBluePrintData();
    EngineFileConverterAPI fileConverter = new DefaultEngineFileConverter();
    return fileConverter.makeGameObject(gameObjectData, gameBluePrintData);
  }

  /**
   * List all saved level files
   *
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
          levels.add(
              file.getName()); // or file.getName().replace(".xml", "") if you want cleaner names
        }
      }
    }
    return levels;
  }

}
