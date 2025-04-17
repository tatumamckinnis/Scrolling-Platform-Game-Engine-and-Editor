package oogasalad.filesaver.savestrategy;

import java.io.File;
import java.io.IOException;
import oogasalad.exceptions.SpriteSheetSaveException;
import oogasalad.fileparser.records.LevelData;
import oogasalad.fileparser.records.SpriteSheetData;

/**
 * Strategy interface for saving level data into different formats.
 *
 * @author Aksel Bell
 */
public interface SaverStrategy {

  /**
   * Saves the provided level data to a file in the desired format.
   *
   * @param levelData  the data to be saved
   * @param outputFile the file to write to.
   */
  void save(LevelData levelData, File outputFile) throws IOException;

  /**
   * Saves the provided sprite sheet data into a file in the desired format
   *
   * @param spriteSheetData the data to be stored
   * @param outputFile      the file to write to
   * @throws SpriteSheetSaveException The exception to throw when an error occurs during saving
   */
  void saveSpriteSheet(SpriteSheetData spriteSheetData,
      File outputFile) throws SpriteSheetSaveException;
}
