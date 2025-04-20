package oogasalad.filesaver.savestrategy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import oogasalad.exceptions.SpriteSheetSaveException;
import oogasalad.fileparser.records.LevelData;
import oogasalad.fileparser.records.SpriteSheetData;
import oogasalad.filesaver.xmlcomponents.XmlBlueprintsWriter;
import oogasalad.filesaver.xmlcomponents.XmlCameraDataWriter;
import oogasalad.filesaver.xmlcomponents.XmlEventsWriter;
import oogasalad.filesaver.xmlcomponents.XmlLayersWriter;
import oogasalad.filesaver.xmlcomponents.XmlMapBoundsWriter;
import oogasalad.filesaver.xmlcomponents.XmlSpriteSheetWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Concrete implementation of SaverStrategy for saving data in XML format.
 *
 * @author Aksel Bell, Jacob You
 */
public class XmlStrategy implements SaverStrategy {
  private static final Logger LOG = LogManager.getLogger();

  @Override
  public void save(LevelData levelData, File outputFile) throws IOException {

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
      writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      new XmlMapBoundsWriter(writer, levelData).write();
      new XmlCameraDataWriter(writer, levelData).write();
      new XmlLayersWriter(writer, levelData).write();
      new XmlBlueprintsWriter(writer, levelData).write();
      new XmlEventsWriter(writer, levelData).write();
      writer.write("</map>\n");
    } catch (IOException e) {
      LOG.warn("Could not save level data.", e);
      throw e;
    }
  }

  @Override
  public void saveSpriteSheet(SpriteSheetData spriteSheetData, File outputFile) throws SpriteSheetSaveException {
    try {
      new XmlSpriteSheetWriter(spriteSheetData, outputFile).write();
    } catch (IOException e) {
      LOG.warn("Could not save sprite sheet data.", e);
      throw new SpriteSheetSaveException("Could not save sprite sheet data:" + e.getMessage(), e);
    }
  }
}
