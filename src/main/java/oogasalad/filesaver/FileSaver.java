package oogasalad.filesaver;

import java.io.IOException;
import javafx.stage.Stage;
import oogasalad.fileparser.records.LevelData;
import oogasalad.filesaver.savestrategy.SaverStrategy;

/**
 * This class will export level data into a file of the user's desired type ie XML, JSON, etc.
 *
 * @author Aksel Bell
 */
public class FileSaver {
  private LevelData myLevelData;
  private SaverStrategy mySaverStrategy;
  private Stage userStage;

  /**
   * Instantiate a new file saver object.
   * @param levelData the level data to be exported.
   * @param userStage the current stage
   */
  public FileSaver(LevelData levelData, Stage userStage) {
    myLevelData = levelData;
    this.userStage = userStage;
  }

  /**
   * Chooses a strategy for exporting based on file type.
   *
   * @param fileType the desired export format (e.g., "XML")
   */
  public void chooseExportType(String fileType) {
    try {
      String className = "oogasalad.filesaver.savestrategy." + fileType + "Strategy";
      Class<?> clazz = Class.forName(className);
      mySaverStrategy = (SaverStrategy) clazz.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Unsupported export type: " + fileType, e);
    }
  }

  /**
   * Saves the level data using the selected saving strategy.
   */
  public void saveLevelData() throws IOException {
    if (mySaverStrategy == null) {
      throw new IllegalStateException("Export type not selected. Call chooseExportType first.");
    }
    mySaverStrategy.save(myLevelData, userStage);
  }

  /**
   * Package protected setter method used for testing.
   * @param strategy desired export strategy.
   */
  public void setSaverStrategy(SaverStrategy strategy) {
    this.mySaverStrategy = strategy;
  }
}
