package oogasalad.fileparser;

import oogasalad.exceptions.BlueprintParseException;
import oogasalad.exceptions.SpriteParseException;
import oogasalad.fileparser.records.LevelData;

public interface FileParserAPI {

  public LevelData parseLevelFile(String filePath)
      throws BlueprintParseException, SpriteParseException;

}
