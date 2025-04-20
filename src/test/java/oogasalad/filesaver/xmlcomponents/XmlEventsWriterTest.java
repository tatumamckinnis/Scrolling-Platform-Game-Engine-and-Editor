package oogasalad.filesaver.xmlcomponents;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.ConditionData;
import oogasalad.fileparser.records.EventData;
import oogasalad.fileparser.records.LevelData;
import oogasalad.fileparser.records.OutcomeData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class XmlEventsWriterTest {
  private File tempFile;

  @BeforeEach
  void setup() throws Exception {
    tempFile = File.createTempFile("events_test", ".xml");
    tempFile.deleteOnExit();
  }

  @Test
  void write_SingleEvent_WritesEventTag() throws Exception {
    LevelData levelData = getLevelData();

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
      new XmlEventsWriter(writer, levelData).write();
    }

    String content = Files.readString(tempFile.toPath());
    assertTrue(content.contains("<events>"));
    assertTrue(content.contains("<event type=\"input\" id=\"20\">"));
    assertTrue(content.contains("<condition name=\"W_KEY_PRESSED\">"));
    assertTrue(content.contains("<parameter name=\"Sensitivity\" value=\"0.5\"/>"));
    assertTrue(content.contains("<outcome name=\"JUMP\">"));
    assertTrue(content.contains("<parameter name=\"JumpAmount\" value=\"20.0\"/>"));
    assertTrue(content.contains("<parameter name=\"jumpType\" value=\"Floaty\"/>"));
    assertTrue(content.contains("</events>"));
  }

  private static LevelData getLevelData() {
    ConditionData condition = new ConditionData("W_KEY_PRESSED", Map.of(), Map.of("Sensitivity", 0.5));
    OutcomeData outcome = new OutcomeData("JUMP", Map.of("jumpType", "Floaty"), Map.of("JumpAmount", 20.0));

    EventData event = new EventData("input", "20", List.of(List.of(condition)), List.of(outcome));

    BlueprintData blueprint = new BlueprintData(
        1,
        3.5, -2.0, 0.0, false,
        "platformer", "Enemies", "enemy",
        null,
        null,
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
}
