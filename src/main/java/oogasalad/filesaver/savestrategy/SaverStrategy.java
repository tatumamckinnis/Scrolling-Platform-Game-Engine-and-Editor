package oogasalad.filesaver.savestrategy;

import oogasalad.fileparser.records.LevelData;

/**
 * Strategy interface for saving level data into different formats.
 */
public interface SaverStrategy {

  /**
   * Saves the provided level data to a file in the desired format.
   *
   * @param levelData the data to be saved
   */
  void save(LevelData levelData);
}
