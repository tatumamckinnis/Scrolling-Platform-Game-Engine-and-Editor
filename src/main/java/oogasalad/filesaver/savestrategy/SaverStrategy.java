package oogasalad.filesaver.savestrategy;

import java.io.IOException;
import oogasalad.fileparser.records.LevelData;

/**
 * Strategy interface for saving level data into different formats.
 *
 * @author Aksel Bell
 */
public interface SaverStrategy {

  /**
   * Saves the provided level data to a file in the desired format.
   *
   * @param levelData the data to be saved
   * @param filePath the file to write to.
   */
  void save(LevelData levelData, String filePath) throws IOException;
}
