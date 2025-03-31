package oogasalad.engine.controller;

import java.io.IOException;
import java.util.zip.DataFormatException;
import oogasalad.fileparser.FileParserAPI;
import oogasalad.fileparser.records.LevelData;

public class DefaultLevel implements LevelAPI {

  private FileParserAPI myFileParser;
  private GameControllerAPI myGameController;

  @Override
  public void selectGame(String game, String category, String level)
      throws DataFormatException, IOException {
    String filePath = game + "/" + category + "/" + level;
    LevelData levelData = myFileParser.parseLevelFile(filePath);
    myGameController.setLevelData(levelData);
  }
}
