package oogasalad.filesaver.xmlcomponents;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import oogasalad.fileparser.records.GameObjectData;
import oogasalad.fileparser.records.LevelData;

/**
 * This class writes the layers tags of the XML file.
 *
 * @author Aksel Bell
 */
public class XmlLayersWriter implements XmlComponentWriter {
  private static final String INDENT = "  ";
  private static final String INDENT2 = INDENT + INDENT;
  private static final String INDENT3 = INDENT2 + INDENT;
  private static final String INDENT4 = INDENT3 + INDENT;
  private final BufferedWriter writer;
  private final LevelData data;

  /**
   * Instantiates a writer.
   * @param writer writer to write to.
   * @param data level data containing necessary data.
   */
  public XmlLayersWriter(BufferedWriter writer, LevelData data) {
    this.writer = writer;
    this.data = data;
  }

  /**
   * @see XmlComponentWriter#write()
   */
  public void write() throws IOException {
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
    String name = group.get(0).name();

    String coords = group.stream()
        .map(o -> String.format("(%d,%d)", o.x(), o.y()))
        .collect(Collectors.joining(", "));

    String uids = group.stream()
        .map(o -> o.uniqueId().toString())
        .collect(Collectors.joining(", "));

    writer.write(String.format(INDENT4 + "<object name = \"%s\" id=\"%d\" coordinates=\"%s\" uid=\"%s\" />\n", name, blueprintId, coords, uids));
  }
}

