package oogasalad.fileparser.records;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Billy McCune
 */
public record LevelData(
    String name,
    int levelWidth,
    int levelHeight,
    Map<Integer,BlueprintData> gameBluePrintData,
    Map<Integer,List<GameObjectData>> gameObjectsByLayer
) {}
