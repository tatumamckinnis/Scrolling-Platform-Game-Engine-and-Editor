package oogasalad.game.file.parser.records;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Billy McCune
 */
public record LevelData(
    String name,
    List<GameObjectData> gameObjectBluePrintData,
    Map<Integer,GameObjectData> gameObjectsByLayer,
    List<EventChainData> eventChains,
    List<SpriteSheetData> spriteSheets
) {}
