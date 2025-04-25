package oogasalad.filesaver.xmlcomponents;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import oogasalad.fileparser.records.*;

/**
 * This class writes the Object tags (which contain the blueprints) in the XML file.
 *
 * @author Aksel Bell
 */
public class XmlBlueprintsWriter implements XmlComponentWriter{
  private static final String INDENT = "  ";
  private static final String INDENT2 = INDENT + INDENT;
  private static final String INDENT3 = INDENT2 + INDENT;
  private static final String INDENT4 = INDENT3 + INDENT;
  private final BufferedWriter writer;
  private final LevelData levelData;
  private final Map<String, String> savedSprites;
  private String gameName;

  /**
   * Instantiates a writer.
   * @param writer writer to write to.
   * @param levelData level data containing necessary data.
   */
  public XmlBlueprintsWriter(BufferedWriter writer, LevelData levelData) {
    this.writer = writer;
    this.levelData = levelData;
    this.savedSprites = new HashMap<>();
  }

  /**
   * @see XmlComponentWriter#write()
   */
  public void write() throws IOException {
    Map<Integer, BlueprintData> blueprintsMap = levelData.gameBluePrintData();
    if (blueprintsMap == null || blueprintsMap.isEmpty()) {
      return;
    }

    Map<String, Map<String, List<BlueprintData>>> groupedBlueprints = groupBlueprintsByGameAndGroup(blueprintsMap);

    // Each gameEntry is a string to a map
    if (groupedBlueprints.size() == 1) {
      gameName = groupedBlueprints.keySet().iterator().next();
    } else {
      gameName = "Untitled";
    }
    writer.write(INDENT + "<game name=\"" + gameName + "\">\n");
    for (var gameEntry : groupedBlueprints.entrySet()) {
      writeGameSection(gameEntry.getValue());
    }
    writer.write(INDENT + "</game>\n");
  }

  private Map<String, Map<String, List<BlueprintData>>> groupBlueprintsByGameAndGroup(Map<Integer, BlueprintData> blueprintsMap) {
    Map<String, Map<String, List<BlueprintData>>> grouped = new HashMap<>();
    for (BlueprintData blueprint : blueprintsMap.values()) {
      grouped
          .computeIfAbsent(blueprint.gameName(), k -> new HashMap<>())
          .computeIfAbsent(blueprint.group(), k -> new ArrayList<>())
          .add(blueprint);
    }
    return grouped;
  }

  private void writeGameSection(Map<String, List<BlueprintData>> groups) throws IOException {
    for (var groupEntry : groups.entrySet()) {
      writeObjectGroupSection(groupEntry.getKey(), groupEntry.getValue());
    }
  }

  private void writeObjectGroupSection(String groupName, List<BlueprintData> blueprints) throws IOException {
    writer.write(INDENT2 + "<objectGroup name=\"" + groupName + "\">\n");
    for (BlueprintData blueprint : blueprints) {
      writeBlueprintObject(blueprint);
    }
    writer.write(INDENT2 + "</objectGroup>\n");
  }

  private void writeBlueprintObject(BlueprintData blueprint) throws IOException {
    SpriteData sprite = blueprint.spriteData();
    HitBoxData hitbox = blueprint.hitBoxData();
    String spriteFileName = saveSpriteIfNeeded(gameName, sprite);

    writer.write(INDENT3 + String.format(
        "<object spriteName=\"%s\" type=\"%s\" id=\"%d\" spriteFile=\"%s\" hitBoxWidth=\"%d\"%n" +
            INDENT4 + "hitBoxHeight=\"%d\" hitBoxShape=\"%s\" spriteDx=\"%d\" spriteDy=\"%d\" eventIDs=\"%s\"%n" +
            INDENT4 + "velocityX=\"%.2f\" velocityY=\"%.2f\" rotation=\"%.2f\">%n",
        sprite.name(), blueprint.type(), blueprint.blueprintId(), spriteFileName,
        hitbox.hitBoxWidth(), hitbox.hitBoxHeight(), hitbox.shape(),
        hitbox.spriteDx(), hitbox.spriteDy(),
        getEventIdsAsString(blueprint),
        blueprint.velocityX(), blueprint.velocityY(), blueprint.rotation()
    ));

    writer.write(INDENT4 + "<properties>\n");
    new XmlPropertiesWriter(writer, 5, blueprint.stringProperties(), blueprint.doubleProperties(), "property").write();
    writer.write(INDENT4 + "</properties>\n");

    writer.write(INDENT3 + "</object>\n");
  }

  private String getEventIdsAsString(BlueprintData blueprint) {
    return blueprint.eventDataList().stream()
        // Grab the eventIDs
        .map(EventData::eventId)
        .filter(id -> !id.isEmpty())
        .collect(Collectors.joining(","));
  }

  private String saveSpriteIfNeeded(String gameName, SpriteData sprite) {
    String key = gameName + "#" + sprite.name();
    if (!savedSprites.containsKey(key)) {
      try {
        XmlSpriteWriter writer = new XmlSpriteWriter(gameName, sprite);
        writer.write();
        String fileName = writer.getSpriteFileName();
        savedSprites.put(key, fileName);
        return fileName;
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
    return savedSprites.get(key);
  }
}
