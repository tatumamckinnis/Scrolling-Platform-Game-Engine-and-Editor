package oogasalad.filesaver.savestrategy;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import oogasalad.fileparser.records.CameraData;
import oogasalad.fileparser.records.FrameData;
import oogasalad.fileparser.records.GameObjectData;
import oogasalad.fileparser.records.LevelData;
import oogasalad.fileparser.records.SpriteSheetData;
import oogasalad.filesaver.FileSaver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class XMLStrategyTest {

  private FileSaver saver;
  private File tempFile;

  @BeforeEach
  void setup() throws IOException {
    tempFile = File.createTempFile("xmlstrategy_test", ".xml");
    tempFile.deleteOnExit();
  }

  @Test
  void save_MapSize_XMLContainsMapSize() throws IOException {
    LevelData levelData = new LevelData("", -500, -700, 4000, 500, null, null, null);
    saver = new FileSaver();

    saver.chooseExportType("XML");
    saver.saveLevelData(levelData, tempFile.getAbsolutePath());

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

    saver = new FileSaver();
    saver.chooseExportType("XML");
    saver.saveLevelData(levelData, tempFile.getAbsolutePath());

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

  @Test
  void save_Layers_XMLContainsLayerData() throws IOException {
    List<GameObjectData> gameObjects = List.of(
        new GameObjectData(60, UUID.fromString("129acc3a-3dc4-49c9-861e-a86cfc67c605"), -500, -700,
            10, ""),
        new GameObjectData(60, UUID.fromString("6fd71b4d-f513-4109-982d-63c229489ac7"), 140, -700,
            10, ""),
        new GameObjectData(1, UUID.fromString("d3fa8312-f7db-4fa3-b0d9-0b4a016bc2a5"), 0, 500, 1,
            ""),
        new GameObjectData(2, UUID.fromString("3f780051-ec51-4c02-bc98-82d450d12397"), 480, 500, 1,
            ""),
        new GameObjectData(111, UUID.fromString("e816f04c-3047-4e30-9e20-2e601a99dde8"), 100, 400,
            1, "")
    );

    LevelData levelData = new LevelData("", -500, -700, 4000, 500, null, null, gameObjects);

    saver = new FileSaver();
    saver.chooseExportType("XML");
    saver.saveLevelData(levelData, tempFile.getAbsolutePath());

    String content = Files.readString(tempFile.toPath());

    assertTrue(content.contains("<layers>"));
    assertTrue(content.contains("</layers>"));

    assertTrue(
        content.contains("<layer name=\"layer_10\" width=\"4500\" height=\"1200\" z=\"10\">"));
    assertTrue(content.contains(
        "<object id=\"60\" coordinates=\"(-500,-700), (140,-700)\" uid=\"129acc3a-3dc4-49c9-861e-a86cfc67c605, 6fd71b4d-f513-4109-982d-63c229489ac7\" />"));

    assertTrue(content.contains("<layer name=\"layer_1\" width=\"4500\" height=\"1200\" z=\"1\">"));
    assertTrue(content.contains(
        "<object id=\"1\" coordinates=\"(0,500)\" uid=\"d3fa8312-f7db-4fa3-b0d9-0b4a016bc2a5\" />"));
    assertTrue(content.contains(
        "<object id=\"2\" coordinates=\"(480,500)\" uid=\"3f780051-ec51-4c02-bc98-82d450d12397\" />"));
    assertTrue(content.contains(
        "<object id=\"111\" coordinates=\"(100,400)\" uid=\"e816f04c-3047-4e30-9e20-2e601a99dde8\" />"));
  }

  @Test
  void saveSpriteSheet_MinimalSheet_XMLContainsSpriteSheetTag() throws Exception {
    FrameData frame = new FrameData("frame0", 0, 0, 64, 64);
    SpriteSheetData sheet = new SpriteSheetData(
        "atlas.png",
        1024,
        768,
        List.of(frame)
    );

    File sheetFile = File.createTempFile("spritesheet_test", ".xml");
    sheetFile.deleteOnExit();
    new XmlStrategy().saveSpriteSheet(sheet, sheetFile);

    String xml = Files.readString(sheetFile.toPath());
    System.out.printf("xml: %s\n", xml);
    assertTrue(xml.contains("<TextureAtlas"));
    assertTrue(xml.contains("imagePath=\"atlas.png\""));
    assertTrue(xml.contains("name=\"frame0\""));
  }

}
