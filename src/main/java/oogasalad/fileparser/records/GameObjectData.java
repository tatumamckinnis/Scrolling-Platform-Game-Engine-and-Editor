package oogasalad.fileparser.records;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author Billy McCune
 */
public record GameObjectData(
    int blueprintId,
    UUID uniqueId,
    String gameName,
    String group, //entities, blocks, backgrounds
    String type,
    SpriteData spriteData,
    int x,
    int y,
    int layer, //z-layer or draw layer for background/foreground ordering
    List<EventData> eventDataList
) { }