package oogasalad.filesaver.savestrategy;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javafx.stage.Stage;
import oogasalad.fileparser.records.LevelData;
import oogasalad.filesaver.FileSaver;
import org.junit.jupiter.api.Test;

public class XMLStrategyTest {
  @Test
  public void save_MapSize_XMLContainsMapSize() throws IOException {
    LevelData testData = new LevelData("", -500, -700, 4000, 500, null, null, null);

    FileSaver saver = new FileSaver(testData, null);
    File tempFile = File.createTempFile("test_level", ".xml");
    tempFile.deleteOnExit();

    saver.setSaverStrategy(new MockXMLStrategy(tempFile));
    saver.saveLevelData();

    String content = Files.readString(tempFile.toPath());
    assertTrue(content.contains("<map minX=\"-500\" minY=\"-700\" maxX=\"4000\" maxY=\"500\">"));
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
