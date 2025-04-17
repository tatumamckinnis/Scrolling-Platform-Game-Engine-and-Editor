package oogasalad.filesaver;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import oogasalad.fileparser.records.LevelData;
import oogasalad.filesaver.savestrategy.SaverStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class will export level data into a file of the user's desired type ie XML, JSON, etc.
 *
 * @author Aksel Bell
 */
public class FileSaver {
  private static final Logger LOG = LogManager.getLogger();
  private SaverStrategy mySaverStrategy;

  /**
   * Chooses a strategy for exporting based on file type.
   *
   * @param fileType the desired export format (e.g., "XML")
   */
  public void chooseExportType(String fileType) {
    try {
      String className = "oogasalad.filesaver.savestrategy."
          + fileType.substring(0,1).toUpperCase() + fileType.substring(1).toLowerCase()
          + "Strategy";
      Class<?> clazz = Class.forName(className);
      mySaverStrategy = (SaverStrategy) clazz.getDeclaredConstructor().newInstance();
    } catch (RuntimeException | ClassNotFoundException | InvocationTargetException |
             InstantiationException | IllegalAccessException | NoSuchMethodException e) {
      LOG.warn("Issue with reflection when choosing export type");
      throw new RuntimeException(e);
    }
  }

  /**
   * Saves the level data using the selected saving strategy.
   *
   * @param levelData the level data to be exported.
   * @param filePath the file path to write to.
   */
  public void saveLevelData(LevelData levelData, String filePath) throws IOException {
    if (mySaverStrategy == null) {
      LOG.warn("Export type not selected.");
      throw new IllegalStateException("Export type not selected. Call chooseExportType first.");
    }
    mySaverStrategy.save(levelData, new File(filePath));
  }
}
