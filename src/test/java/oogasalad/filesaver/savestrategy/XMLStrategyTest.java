package oogasalad.filesaver.savestrategy;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import javafx.stage.Stage;
import oogasalad.fileparser.records.CameraData;
import oogasalad.fileparser.records.LevelData;
import oogasalad.filesaver.FileSaver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class XMLStrategyTest {
  private FileSaver saver;
  private File tempFile;

  @BeforeEach
  void setup() throws IOException {
    tempFile = File.createTempFile("test_level", ".xml");
    tempFile.deleteOnExit();
  }

  @Test
  void save_MapSize_XMLContainsMapSize() throws IOException {
    LevelData levelData = new LevelData("", -500, -700, 4000, 500, null, null, null);
    saver = new FileSaver(levelData, null);

    saver.setSaverStrategy(new MockXMLStrategy(tempFile));
    saver.saveLevelData();

    String content = Files.readString(tempFile.toPath());
    assertTrue(content.contains("<map minX=\"-500\" minY=\"-700\" maxX=\"4000\" maxY=\"500\">"));
    assertTrue(content.contains("</map>"));
  }

  @Test
  void save_CameraData_XMLContainCameraData() throws IOException {
    CameraData cameraData = new CameraData("SideScrollingCamera",
        Map.of("followTarget", "player", "mode", "smooth"),
        Map.of("offsetX", 100.0, "offsetY", 50.0)
    );

    LevelData levelData = new LevelData("", -500, -700, 4000, 500, cameraData, null, null);

    saver = new FileSaver(levelData, null);
    saver.setSaverStrategy(new MockXMLStrategy(tempFile));
    saver.saveLevelData();

    String content = Files.readString(tempFile.toPath());

    assertTrue(content.contains("<cameraData type=\"SideScrollingCamera\">"));
    assertTrue(content.contains("<stringProperties>"));
    assertTrue(content.contains("<property name=\"followTarget\" value=\"player\"/>"));
    assertTrue(content.contains("<property name=\"mode\" value=\"smooth\"/>"));
    assertTrue(content.contains("<doubleProperties>"));
    assertTrue(content.contains("<property name=\"offsetX\" value=\"100.0\"/>"));
    assertTrue(content.contains("<property name=\"offsetY\" value=\"50.0\"/>"));
    assertTrue(content.contains("</doubleProperties>"));
    assertTrue(content.contains("</cameraData>"));
  }

  private static class MockXMLStrategy extends XMLStrategy {
    private final File mockFile;

    public MockXMLStrategy(File mockFile) {
      this.mockFile = mockFile;
    }

    @Override
    File setExportPath(Stage userStage) {
      return mockFile;
    }
  }
}
