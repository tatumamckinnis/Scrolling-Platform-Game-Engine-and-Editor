package oogasalad.fileparser;

import oogasalad.exceptions.BlueprintParseException;
import oogasalad.exceptions.EventParseException;
import oogasalad.exceptions.GameObjectParseException;
import oogasalad.exceptions.HitBoxParseException;
import oogasalad.exceptions.LevelDataParseException;
import oogasalad.exceptions.PropertyParsingException;
import oogasalad.exceptions.SpriteParseException;
import oogasalad.fileparser.records.LevelData;

public interface FileParserAPI {

  public LevelData parseLevelFile(String filePath)
      throws BlueprintParseException, SpriteParseException, HitBoxParseException, GameObjectParseException, LevelDataParseException, PropertyParsingException, EventParseException;

}
