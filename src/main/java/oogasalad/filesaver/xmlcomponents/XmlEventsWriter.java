package oogasalad.filesaver.xmlcomponents;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import oogasalad.fileparser.records.ConditionData;
import oogasalad.fileparser.records.EventData;
import oogasalad.fileparser.records.LevelData;
import oogasalad.fileparser.records.OutcomeData;

/**
 * This class writes the XML event tags filling in the conditions and outcomes for each event.
 *
 * @author Aksel Bell
 */
public class XmlEventsWriter implements XmlComponentWriter{
  private static final String INDENT = "  ";
  private static final String INDENT2 = INDENT + INDENT;
  private static final String INDENT3 = INDENT2 + INDENT;
  private static final String INDENT4 = INDENT3 + INDENT;
  private static final String INDENT5 = INDENT4 + INDENT;
  private final BufferedWriter writer;
  private final LevelData levelData;

  /**
   * Instantiates a writer.
   * @param writer writer to write to.
   * @param levelData level data containing necessary data.
   */
  public XmlEventsWriter(BufferedWriter writer, LevelData levelData) {
    this.writer = writer;
    this.levelData = levelData;
  }

  /**
   * @see XmlComponentWriter#write()
   */
  public void write() throws IOException {
    if (levelData.gameBluePrintData() == null) {
      return;
    }

    writer.write(INDENT + "<events>\n");
    for (var blueprint : levelData.gameBluePrintData().values()) {
      for (EventData event : blueprint.eventDataList()) {
        writeEvent(event);
      }
    }
    writer.write(INDENT + "</events>\n");
  }

  private void writeEvent(EventData event) throws IOException {
    writer.write(String.format(INDENT2 + "<event type=\"%s\" id=\"%s\">\n", event.type(), event.eventId()));
    writeConditions(event.conditions());
    writeOutcomes(event.outcomes());
    writer.write(INDENT2 + "</event>\n");
  }

  private void writeConditions(List<List<ConditionData>> nestedConditions) throws IOException {
    writer.write(INDENT3 + "<conditions>\n");
    for (List<ConditionData> conditionSet : nestedConditions) {
      writer.write(INDENT4 + "<conditionSet>\n");
      for (ConditionData condition : conditionSet) {
        writer.write(String.format(INDENT5 + "<condition name=\"%s\">\n", condition.name()));
        new XmlPropertiesWriter(writer, 6, condition.stringProperties(), condition.doubleProperties(),"Parameters" ,"parameter").write();
        writer.write(INDENT5 + "</condition>\n");
      }
      writer.write(INDENT4 + "</conditionSet>\n");
    }
    writer.write(INDENT3 + "</conditions>\n");
  }

  private void writeOutcomes(List<OutcomeData> outcomes) throws IOException {
    writer.write(INDENT3 + "<outcomes>\n");
    for (OutcomeData outcome : outcomes) {
      writer.write(String.format(INDENT4 + "<outcome name=\"%s\">\n", outcome.name()));
      new XmlPropertiesWriter(writer, 5, outcome.stringProperties(), outcome.doubleProperties(),"Parameters", "parameter").write();
      writer.write(INDENT4 + "</outcome>\n");
    }
    writer.write(INDENT3 + "</outcomes>\n");
  }
}