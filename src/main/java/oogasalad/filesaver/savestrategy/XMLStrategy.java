package oogasalad.filesaver.savestrategy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import oogasalad.fileparser.records.LevelData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Concrete implementation of SaverStrategy for saving data in XML format.
 */
public class XMLStrategy implements SaverStrategy {
  private static final Logger LOG = LogManager.getLogger();

  /**
   * Saves the provided level data to an XML file.
   *
   * @param levelData the data to be saved
   * @param userStage the current stage
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

      writer.write(String.format(
          "<map minX=\"%d\" minY=\"%d\" maxX=\"%d\" maxY=\"%d\">\n",
          levelData.minX(),
          levelData.minY(),
          levelData.maxX(),
          levelData.maxY()
      ));

      writer.write("</map>\n");
    } catch (IOException e) {
      LOG.warn("Could not save level data", e);
    }
  }

  /**
   * Package private method for setting the file path. Can be overrid in testing classes to allow custom file paths.
   * @param userStage user's current stage.
   * @return File to save the XML to.
   */
  File setExportPath(Stage userStage) throws IOException {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Save Level As XML");
    fileChooser.getExtensionFilters()
        .add(new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml"));
    fileChooser.setInitialFileName("exported_level.xml");

    return fileChooser.showSaveDialog(userStage);
  }
}
