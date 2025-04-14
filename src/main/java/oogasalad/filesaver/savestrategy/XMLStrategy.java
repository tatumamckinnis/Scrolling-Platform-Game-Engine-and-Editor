package oogasalad.filesaver.savestrategy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import oogasalad.fileparser.records.CameraData;
import oogasalad.fileparser.records.LevelData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Concrete implementation of SaverStrategy for saving data in XML format.
 */
public class XMLStrategy implements SaverStrategy {
  private static final Logger LOG = LogManager.getLogger();
  private static final String INDENT = "  ";
  private static final String INDENT2 = INDENT + INDENT;
  private static final String INDENT3 = INDENT + INDENT + INDENT;

  @Override
  public void save(LevelData levelData, Stage userStage) throws IOException {
    File file = setExportPath(userStage);

    if (file == null) {
      LOG.info("Save cancelled by user.");
      return;
    }

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
      writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      writeMapBounds(writer, levelData);
      writeCameraData(writer, levelData);
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

  private void writeMapBounds(Writer writer, LevelData data) throws IOException {
    writer.write(String.format(
        "<map minX=\"%d\" minY=\"%d\" maxX=\"%d\" maxY=\"%d\">\n",
        data.minX(), data.minY(), data.maxX(), data.maxY()
    ));
  }

  private void writeCameraData(BufferedWriter writer, LevelData data) throws IOException {
    CameraData camera = data.cameraData();
    if (camera == null) return;

    writer.write(String.format(INDENT + "<cameraData type=\"%s\">\n", camera.type()));

    writer.write(INDENT2 + "<stringProperties>\n");
    if (camera.stringProperties() != null) {
      for (Map.Entry<String, String> entry : camera.stringProperties().entrySet()) {
        writer.write(String.format(INDENT3 + "<property name=\"%s\" value=\"%s\"/>\n",
            entry.getKey(), entry.getValue()));
      }
    }
    writer.write(INDENT2 + "</stringProperties>\n");

    writer.write(INDENT2 + "<doubleProperties>\n");
    if (camera.doubleProperties() != null) {
      for (Map.Entry<String, Double> entry : camera.doubleProperties().entrySet()) {
        writer.write(String.format(INDENT3 + "<property name=\"%s\" value=\"%s\"/>\n",
            entry.getKey(), entry.getValue()));
      }
    }
    writer.write(INDENT2 + "</doubleProperties>\n");

    writer.write(INDENT + "</cameraData>\n");
  }
}
