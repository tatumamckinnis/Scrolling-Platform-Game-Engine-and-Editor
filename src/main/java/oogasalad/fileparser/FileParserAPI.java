package oogasalad.fileparser;

import oogasalad.fileparser.records.LevelData;

/**
 * Interface for parsing a data file
 *
 * @author Billy McCune
 */
public interface FileParserAPI {

  /**
   * parses a level file given a file path
   *
   * @param filePath the file path to retrieve
   * @return a new LevelData object
   */
  public LevelData parseLevelFile(String filePath);

}
