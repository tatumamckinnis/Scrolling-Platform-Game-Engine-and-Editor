package oogasalad.game.file.parser.records;

import java.util.List;

/**
 *
 * @author Billy McCune
 */
public record LevelData(
    String name,
    List<GameObjectData> gameObjectBluePrintData,
    List<GameObjectData> gameObjects,
    //Map<Integer,GameObjectData>  or List<List<GameObjectData>> gameObjectsByLayer - cbtm,
    List<EventChainData> eventChains,
    List<SpriteSheetData> spriteSheets
) {}
