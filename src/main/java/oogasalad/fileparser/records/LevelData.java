package oogasalad.fileparser.records;

import java.util.List;
import java.util.Map;
/**
 *
 * @author Billy McCune
 */
public record LevelData(
    String name,
    int minX,
    int minY,
    int maxX,
    int maxY,
    Map<Integer,BlueprintData> gameBluePrintData,
    List<GameObjectData> gameObjects
) {}
