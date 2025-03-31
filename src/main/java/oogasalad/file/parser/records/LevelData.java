package oogasalad.file.parser.records;

import java.util.List;

public record LevelData(
    String name,
    List<GameObjectData> gameObjects,
    List<EventChainData> eventChains,
    List<SpriteSheetData> spriteSheets
) {}
