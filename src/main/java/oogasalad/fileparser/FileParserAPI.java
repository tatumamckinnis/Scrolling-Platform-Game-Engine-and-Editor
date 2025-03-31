package oogasalad.fileparser;

import java.util.logging.Level;
import oogasalad.fileparser.records.LevelData;

public interface FileParserAPI {
   public LevelData parseLevelFile(String fileName);

}
