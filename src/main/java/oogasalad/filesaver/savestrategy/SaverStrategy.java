package oogasalad.filesaver.savestrategy;

import java.io.IOException;
import javafx.stage.Stage;
import oogasalad.fileparser.records.LevelData;

/**
 * Strategy interface for saving level data into different formats.
 */
public interface SaverStrategy {

  /**
   * Saves the provided level data to a file in the desired format.
   *
   * @param levelData the data to be saved
   * @param stage the current user's stage.
   */
  void save(LevelData levelData, Stage stage) throws IOException;
}
