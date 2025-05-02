package oogasalad.filesaver.xmlcomponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.CameraData;
import oogasalad.fileparser.records.EventData;
import oogasalad.fileparser.records.FrameData;
import oogasalad.fileparser.records.HitBoxData;
import oogasalad.fileparser.records.LevelData;
import oogasalad.fileparser.records.SpriteData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class XmlBlueprintsWriterTest {

  private File tempFile;

  @BeforeEach
  void setup() throws Exception {
    tempFile = File.createTempFile("blueprints_test", ".xml");
    tempFile.deleteOnExit();
  }

  @Test
  void write_SingleBlueprint_WritesBlueprint() throws Exception {
    LevelData levelData = getLevelData();

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
      new XmlBlueprintsWriter(writer, levelData).write();
    }

    String content = Files.readString(tempFile.toPath());
    System.out.println(content);
    assertTrue(content.contains("<game name=\"platformer\">"));
    assertTrue(content.contains("<objectGroup name=\"Enemies\">"));
    assertTrue(content.contains("<object spriteName=\"player\" type=\"enemy\" id=\"1\""));
    assertTrue(content.contains("spriteFile=\"player.png\""));
    assertTrue(content.contains("hitBoxWidth=\"32\""));
    assertTrue(content.contains("hitBoxHeight=\"64\""));
    assertTrue(content.contains("hitBoxShape=\"rectangle\""));
    assertTrue(content.contains("spriteDx=\"5\""));
    assertTrue(content.contains("spriteDy=\"10\""));
    assertTrue(content.contains("eventIDs=\"eventA\""));
    assertTrue(content.contains("<property name=\"team\" value=\"red\"/>"));
    assertTrue(content.contains("<property name=\"health\" value=\"100.0\"/>"));
  }

  // ChatGPT helped generate this testing data.
  private static LevelData getLevelData() {
    SpriteData sprite = new SpriteData("player", new File("player.png"), null, null, null);
    HitBoxData hitbox = new HitBoxData("rectangle", 32, 64, 5, 10);
    EventData event = new EventData("onCollide", "eventA", null, null);

    BlueprintData blueprint = new BlueprintData(
        1,
        3.5, -2.0, 0.0,
        false, "platformer", "Enemies", "enemy",
        sprite,
        hitbox,
        List.of(event),
        Map.of("team", "red"),
        Map.of("health", 100.0),
        List.of("team", "health")
    );

    return new LevelData(
        "Level 1",
        0, 0, 1000, 1000,
        null,
        Map.of(1, blueprint),
        List.of()
    );
  }

  @Test
  public void saveSpriteIfNeeded_DifferentSpriteNames_ReturnsDifferentFileNames() throws Exception {
    BufferedWriter bw = new BufferedWriter(new StringWriter());
    LevelData levelData = new LevelData("lvl", 0, 0, 0, 0, new CameraData("", Map.of(), Map.of()),
        Map.of(), List.of());
    XmlBlueprintsWriter writer = new XmlBlueprintsWriter(bw, levelData);

    Method saveMethod = XmlBlueprintsWriter.class
        .getDeclaredMethod("saveSpriteIfNeeded", String.class, SpriteData.class);
    saveMethod.setAccessible(true);

    FrameData base1 = new FrameData("one", 0, 0, 1, 1);
    SpriteData sprite1 = new SpriteData(
        "", new File("one.png"), base1, List.of(base1), List.of());
    FrameData base2 = new FrameData("two", 0, 0, 1, 1);
    SpriteData sprite2 = new SpriteData(
        "", new File("two.png"), base2, List.of(base2), List.of());

    String file1 = (String) saveMethod.invoke(writer, "GameName", sprite1);
    String file2 = (String) saveMethod.invoke(writer, "GameName", sprite2);

    assertNotEquals(file1, file2);
  }
}

