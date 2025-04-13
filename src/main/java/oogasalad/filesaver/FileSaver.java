package oogasalad.filesaver;

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

  /**
   * Instantiate a new file saver object.
   * @param levelData the level data to be exported.
   */
  public FileSaver(LevelData levelData) {
    myLevelData = levelData;
  }

  /**
   * Chooses a strategy for exporting based on file type.
   *
   * @param fileType the desired export format (e.g., "XML")
   */
  public void chooseExportType(String fileType) {
    try {
      String className = "oogasalad.filesaver.savestrategy." + fileType.toUpperCase() + "Strategy";
      Class<?> clazz = Class.forName(className);
      mySaverStrategy = (SaverStrategy) clazz.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Unsupported export type: " + fileType, e);
    }
  }

  /**
   * Saves the level data using the selected saving strategy.
   */
  public void saveLevelData() {
    if (mySaverStrategy == null) {
      throw new IllegalStateException("Export type not selected. Call chooseExportType first.");
    }
    mySaverStrategy.save(myLevelData);
  }
}
