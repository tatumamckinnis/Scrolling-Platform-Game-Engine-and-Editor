package oogasalad.filesaver.savestrategy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.CameraData;
import oogasalad.fileparser.records.GameObjectData;
import oogasalad.fileparser.records.HitBoxData;
import oogasalad.fileparser.records.LevelData;
import oogasalad.fileparser.records.SpriteData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Concrete implementation of SaverStrategy for saving data in XML format.
 */
public class XmlStrategy implements SaverStrategy {
  private static final Logger LOG = LogManager.getLogger();
  private static final String INDENT = "  ";
  private static final String INDENT2 = INDENT + INDENT;
  private static final String INDENT3 = INDENT2 + INDENT;
  private static final String INDENT4 = INDENT3 + INDENT;

  /**
   * @see SaverStrategy#save(LevelData, Stage)
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
      writeMapBounds(writer, levelData);
      writeCameraData(writer, levelData);
      writeLayers(writer, levelData);
      writeBlueprints(writer, levelData);
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

    writeProperties(writer, 2, camera.stringProperties(), camera.doubleProperties());

    writer.write(INDENT + "</cameraData>\n");
  }

  private void writeLayers(BufferedWriter writer, LevelData data) throws IOException {
    List<GameObjectData> objects = data.gameObjects();
    if (objects == null || objects.isEmpty()) {
      return;
    }

    Map<Integer, List<GameObjectData>> layerMap = groupObjectsByLayer(objects);

    writer.write(INDENT + "<layers>\n");

    for (Map.Entry<Integer, List<GameObjectData>> entry : layerMap.entrySet()) {
      writeSingleLayer(writer, entry.getKey(), entry.getValue(), data);
    }

    writer.write(INDENT + "</layers>\n");
  }

  private Map<Integer, List<GameObjectData>> groupObjectsByLayer(List<GameObjectData> objects) {
    Map<Integer, List<GameObjectData>> layerMap = new TreeMap<>();
    for (GameObjectData obj : objects) {
      layerMap.computeIfAbsent(obj.layer(), k -> new ArrayList<>()).add(obj);
    }
    return layerMap;
  }

  private void writeSingleLayer(BufferedWriter writer, int layerZ, List<GameObjectData> layerObjects, LevelData data) throws IOException {
    int width = data.maxX() - data.minX();
    int height = data.maxY() - data.minY();

    writer.write(String.format(INDENT2 + "<layer name=\"layer_%d\" width=\"%d\" height=\"%d\" z=\"%d\">\n", layerZ, width, height, layerZ));
    writer.write(INDENT3 + "<data>\n");

    Map<Integer, List<GameObjectData>> groupedById = groupObjectsByBlueprintId(layerObjects);

    for (Map.Entry<Integer, List<GameObjectData>> objGroup : groupedById.entrySet()) {
      writeObjectTag(writer, objGroup.getKey(), objGroup.getValue());
    }

    writer.write(INDENT3 + "</data>\n");
    writer.write(INDENT2 + "</layer>\n");
  }

  private Map<Integer, List<GameObjectData>> groupObjectsByBlueprintId(List<GameObjectData> objects) {
    Map<Integer, List<GameObjectData>> groupedById = new LinkedHashMap<>();
    for (GameObjectData obj : objects) {
      groupedById.computeIfAbsent(obj.blueprintId(), k -> new ArrayList<>()).add(obj);
    }
    return groupedById;
  }

  private void writeObjectTag(BufferedWriter writer, int blueprintId, List<GameObjectData> group) throws IOException {
    String coords = group.stream()
        .map(o -> String.format("(%d,%d)", o.x(), o.y()))
        .collect(Collectors.joining(", "));

    String uids = group.stream()
        .map(o -> o.uniqueId().toString())
        .collect(Collectors.joining(", "));

    writer.write(String.format(INDENT4 + "<object id=\"%d\" coordinates=\"%s\" uid=\"%s\" />\n", blueprintId, coords, uids));
  }


  private void writeBlueprints(BufferedWriter writer, LevelData levelData) throws IOException {
    Map<Integer, BlueprintData> blueprintsMap = levelData.gameBluePrintData();
    if (blueprintsMap == null || blueprintsMap.isEmpty()) {
      return;
    }

    Map<String, Map<String, List<BlueprintData>>> grouped = new HashMap<>();
    for (BlueprintData blueprint : blueprintsMap.values()) {
      grouped
          .computeIfAbsent(blueprint.gameName(), k -> new HashMap<>())
          .computeIfAbsent(blueprint.group(), k -> new ArrayList<>())
          .add(blueprint);
    }

    for (var gameEntry : grouped.entrySet()) {
      String gameName = gameEntry.getKey();
      writer.write(INDENT + "<game name=\"" + gameName + "\">\n");

      for (var groupEntry : gameEntry.getValue().entrySet()) {
        String groupName = groupEntry.getKey();
        writer.write(INDENT2 + "<objectGroup name=\"" + groupName + "\">\n");

        for (BlueprintData blueprint : groupEntry.getValue()) {
          SpriteData sprite = blueprint.spriteData();
          HitBoxData hitbox = blueprint.hitBoxData();

          writer.write(INDENT3 + String.format(
              "<object spriteName=\"%s\" type=\"%s\" id=\"%d\" spriteFile=\"%s\" hitBoxWidth=\"%d\"%n" +
                  INDENT4 + "hitBoxHeight=\"%d\" hitBoxShape=\"%s\" spriteDx=\"%d\" spriteDy=\"%d\" eventIDs=\"%s\"%n" +
                  INDENT4 + "velocityX=\"%.2f\" velocityY=\"%.2f\" rotation=\"%.2f\">%n",
              sprite.name(), blueprint.type(), blueprint.blueprintId(), sprite.spriteFile().getName(),
              hitbox.hitBoxWidth(), hitbox.hitBoxHeight(), hitbox.shape(),
              hitbox.spriteDx(), hitbox.spriteDy(),
              blueprint.eventDataList().stream()
                  .map(e -> e.eventId())
                  .filter(id -> !id.isEmpty())
                  .collect(Collectors.joining(",")),
              blueprint.velocityX(), blueprint.velocityY(), blueprint.rotation()
          ));

          writer.write(INDENT4 + "<properties>\n");

          writeProperties(writer, 5, blueprint.stringProperties(), blueprint.doubleProperties());

          writer.write(INDENT4 + "</properties>\n");
          writer.write(INDENT3 + "</object>\n");
        }

        writer.write(INDENT2 + "</objectGroup>\n");
      }

      writer.write(INDENT + "</game>\n");
    }
  }

  private void writeProperties(BufferedWriter writer, int indentLevel,
      Map<String, String> stringProps,
      Map<String, Double> doubleProps) throws IOException {
    String indent = INDENT.repeat(indentLevel);
    String indentInner = INDENT.repeat(indentLevel + 1);

    writer.write(indent + "<stringProperties>\n");
    if (stringProps != null) {
      for (Map.Entry<String, String> entry : stringProps.entrySet()) {
        writer.write(String.format(indentInner + "<property name=\"%s\" value=\"%s\"/>\n",
            entry.getKey(), entry.getValue()));
      }
    }
    writer.write(indent + "</stringProperties>\n");

    writer.write(indent + "<doubleProperties>\n");
    if (doubleProps != null) {
      for (Map.Entry<String, Double> entry : doubleProps.entrySet()) {
        writer.write(String.format(indentInner + "<property name=\"%s\" value=\"%s\"/>\n",
            entry.getKey(), entry.getValue()));
      }
    }
    writer.write(indent + "</doubleProperties>\n");
  }


}
