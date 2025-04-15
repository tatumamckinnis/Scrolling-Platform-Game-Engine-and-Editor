package oogasalad.filesaver.savestrategy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.CameraData;
import oogasalad.fileparser.records.GameObjectData;
import oogasalad.fileparser.records.HitBoxData;
import oogasalad.fileparser.records.LevelData;
import oogasalad.fileparser.records.SpriteData;
import oogasalad.filesaver.savestrategy.xmlcomponents.XmlBlueprintsWriter;
import oogasalad.filesaver.savestrategy.xmlcomponents.XmlCameraDataWriter;
import oogasalad.filesaver.savestrategy.xmlcomponents.XmlLayersWriter;
import oogasalad.filesaver.savestrategy.xmlcomponents.XmlMapBoundsWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Concrete implementation of SaverStrategy for saving data in XML format.
 *
 * @author Aksel Bell
 */
public class XmlStrategy implements SaverStrategy {
  private static final Logger LOG = LogManager.getLogger();
  private static final String INDENT = "  ";
  private static final String INDENT2 = INDENT + INDENT;
  private static final String INDENT3 = INDENT2 + INDENT;
  private static final String INDENT4 = INDENT3 + INDENT;

  /**
   * @see SaverStrategy#save(LevelData, Stage)
   */
  @Override
  public void save(LevelData levelData, Stage userStage) throws IOException {
    File file = setExportPath(userStage);

    if (file == null) {
      LOG.info("Save cancelled by user.");
      return;
    }

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
      writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      new XmlMapBoundsWriter(writer, levelData).write();
      new XmlCameraDataWriter(writer, levelData).write();
      new XmlLayersWriter(writer, levelData).write();
      new XmlBlueprintsWriter(writer, levelData).write();
      writer.write("</map>\n");
    } catch (IOException e) {
      LOG.warn("Could not save level data", e);
      throw e;
    }
  }

  File setExportPath(Stage userStage) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Save Level As XML");
    fileChooser.getExtensionFilters().add(
        new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml")
    );
    fileChooser.setInitialFileName("exported_level.xml");
    return fileChooser.showSaveDialog(userStage);
  }
}
