package oogasalad.editor.model.saver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import oogasalad.editor.view.EditorComponentFactory;
import oogasalad.exceptions.SpriteSheetSaveException;
import oogasalad.fileparser.records.FrameData;
import oogasalad.fileparser.records.SpriteSheetData;
import oogasalad.filesaver.savestrategy.SaverStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Serialises a sprite‑sheet cut into an XML atlas file.
 *
 * @author Jacob You
 */
public final class SpriteSheetSaver {

  private static final Logger LOG = LogManager.getLogger(EditorComponentFactory.class);

  /**
   * Converts editor sprite sheet data into file saving format to be saved into a file of the
   * specified saver strategy type.
   *
   * @param imagePath   The URL path of the sprite sheet
   * @param sheetWidth  The width of the sprite sheet
   * @param sheetHeight The height of the sprite sheet
   * @param frames      The list of {@link oogasalad.editor.model.data.object.sprite.FrameData} to
   *                    save
   * @param outputFile  The file to save the data to
   * @throws SpriteSheetSaveException The exception to throw when something goes wrong with saving
   */
  public static void save(String imagePath, int sheetWidth, int sheetHeight,
      List<oogasalad.editor.model.data.object.sprite.FrameData> frames, File outputFile,
      SaverStrategy saver)
      throws SpriteSheetSaveException {
    List<FrameData> recordFrames = new ArrayList<>();
    for (oogasalad.editor.model.data.object.sprite.FrameData frame : frames) {
      recordFrames.add(convertFrameData(frame));
    }
    LOG.info("Saving {} frames into {}", recordFrames.size(), outputFile);
    saver.saveSpriteSheet(new SpriteSheetData(imagePath, sheetWidth, sheetHeight, recordFrames),
        outputFile);
  }

  private static FrameData convertFrameData(
      oogasalad.editor.model.data.object.sprite.FrameData frameData) {
    return new FrameData(
        frameData.name(),
        frameData.x(),
        frameData.y(),
        frameData.width(),
        frameData.height()
    );
  }
}
