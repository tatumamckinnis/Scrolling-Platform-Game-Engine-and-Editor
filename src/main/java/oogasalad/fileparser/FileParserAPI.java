package oogasalad.fileparser;

import oogasalad.fileparser.records.LevelData;

public interface FileParserAPI {
   public LevelData parseLevelFile(String filePath, String fileName);

}
