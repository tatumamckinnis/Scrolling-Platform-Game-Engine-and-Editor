package oogasalad.filesaver.xmlcomponents;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.EventData;
import oogasalad.fileparser.records.HitBoxData;
import oogasalad.fileparser.records.LevelData;
import oogasalad.fileparser.records.SpriteData;

/**
 * Writes the <game>/<objectGroup>/<object> (blueprint) sections of the level XML.
 * <p>
 * One <game> element is emitted for every distinct game-name found in the blueprints map.  Sprite
 * sheets are emitted once-per-game+spriteName combo via {@link XmlSpriteWriter}.
 */
public class XmlBlueprintsWriter implements XmlComponentWriter {

  private static final String INDENT = "  ";
  private static final String INDENT2 = INDENT + INDENT;
  private static final String INDENT3 = INDENT2 + INDENT;
  private static final String INDENT4 = INDENT3 + INDENT;

  private final BufferedWriter writer;
  private final LevelData levelData;
  private final Map<String, String> savedSprites = new HashMap<>();

  public XmlBlueprintsWriter(BufferedWriter writer, LevelData levelData) {
    this.writer = Objects.requireNonNull(writer);
    this.levelData = Objects.requireNonNull(levelData);
  }

  /* ====================================================================== */

  @Override
  public void write() throws IOException {

    Map<Integer, BlueprintData> blueprints = levelData.gameBluePrintData();
    if (blueprints == null || blueprints.isEmpty()) {
      return;
    }

    // gameName -> ( groupName -> List<BlueprintData> )
    Map<String, Map<String, List<BlueprintData>>> grouped =
        groupByGameAndGroup(blueprints);

    for (var gameEntry : grouped.entrySet()) {
      String gameName = gameEntry.getKey();
      Map<String, List<BlueprintData>> groups = gameEntry.getValue();

      writer.write(INDENT + "<game name=\"" + gameName + "\">\n");
      writeGameSection(gameName, groups);
      writer.write(INDENT + "</game>\n");
    }
  }

  /* ====================================================================== */
  /* -----------------------  helper writers  ----------------------------- */

  private void writeGameSection(String gameName,
      Map<String, List<BlueprintData>> groups) throws IOException {

    for (var groupEntry : groups.entrySet()) {
      writeObjectGroupSection(gameName,
          groupEntry.getKey(),
          groupEntry.getValue());
    }
  }

  private void writeObjectGroupSection(String gameName,
      String groupName,
      List<BlueprintData> blueprints) throws IOException {

    writer.write(INDENT2 + "<objectGroup name=\"" + groupName + "\">\n");
    for (BlueprintData blueprint : blueprints) {
      writeBlueprintObject(gameName, blueprint);
    }
    writer.write(INDENT2 + "</objectGroup>\n");
  }

  private void writeBlueprintObject(String gameName,
      BlueprintData blueprint) throws IOException {

    SpriteData sprite = blueprint.spriteData();
    HitBoxData hitbox = blueprint.hitBoxData();

    String spriteFileName = saveSpriteIfNeeded(gameName, sprite);

    writer.write(INDENT3 + String.format("""
            <object
              spriteName="%s"
              type="%s"
              id="%d"
              spriteFile="%s"
              hitBoxWidth="%d"
              hitBoxHeight="%d"
              hitBoxShape="%s"
              spriteDx="%d"
              spriteDy="%d"
              eventIDs="%s"
              velocityX="%.2f"
              velocityY="%.2f"
              rotation="%.2f">
            """,
        sprite.baseFrame().name(),           // %s  spriteName
        blueprint.type(),                    // %s  type
        blueprint.blueprintId(),             // %d  id
        spriteFileName,                      // %s  spriteFile
        hitbox.hitBoxWidth(),                // %d  hitBoxWidth
        hitbox.hitBoxHeight(),               // %d  hitBoxHeight
        hitbox.shape(),                      // %s  hitBoxShape
        hitbox.spriteDx(),                   // %d  spriteDx
        hitbox.spriteDy(),                   // %d  spriteDy
        getEventIdsAsString(blueprint),      // %s  eventIDs
        blueprint.velocityX(),               // %.2f velocityX
        blueprint.velocityY(),               // %.2f velocityY
        blueprint.rotation()                 // %.2f rotation
    ));

    /* ---- custom properties ---- */
    writer.write(INDENT4 + "<properties>\n");
    new XmlPropertiesWriter(
        writer,
        5,
        blueprint.stringProperties(),
        blueprint.doubleProperties(), "Properties",
        "property"
    ).write();
    writer.write(INDENT4 + "</properties>\n");

    // Add displayedProperties element for player objects
    if ("player".equals(blueprint.type()) && !blueprint.displayedProperties().isEmpty()) {
      List<String> displayedProps = blueprint.displayedProperties();
      writer.write(INDENT4 + String.format("<displayedProperties propertyList=\"%s\"/>\n",
          String.join(",", displayedProps)));
    }

    writer.write(INDENT3 + "</object>\n");
  }

  private Map<String, Map<String, List<BlueprintData>>> groupByGameAndGroup(
      Map<Integer, BlueprintData> src) {

    Map<String, Map<String, List<BlueprintData>>> grouped = new HashMap<>();

    for (BlueprintData bp : src.values()) {
      grouped
          .computeIfAbsent(bp.gameName(), g -> new HashMap<>())
          .computeIfAbsent(bp.group(), g -> new ArrayList<>())
          .add(bp);
    }
    return grouped;
  }

  private String getEventIdsAsString(BlueprintData blueprint) {
    return blueprint.eventDataList()
        .stream()
        .map(EventData::eventId)
        .filter(id -> !id.isBlank())
        .collect(Collectors.joining(","));
  }

  /**
   * Writes the sprite-sheet XML for {@code sprite} once per (gameName,spriteName) combination and
   * returns the file-name that should be referenced from the
   * <object> tag.
   */
  private String saveSpriteIfNeeded(String gameName, SpriteData sprite) {
    String key = gameName + "#" + sprite.name();

    if (!savedSprites.containsKey(key)) {
      try {
        XmlSpriteWriter sw = new XmlSpriteWriter(gameName, sprite);
        sw.write();
        savedSprites.put(key, sw.getSpriteFileName());
      } catch (IOException ioe) {
        ioe.printStackTrace();
        savedSprites.put(key, sprite.spriteFile().getName());
      }
    }
    return savedSprites.get(key);
  }
}