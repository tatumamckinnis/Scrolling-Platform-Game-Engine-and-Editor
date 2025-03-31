package oogasalad.file.parser.records;

import java.util.Map;

public record GameObjectData(
    int id,
    String type,
    String group, //entities, blocks, backgrounds
    String spriteName,
    String spriteFile,
    int x,
    int y,
    int layer, //z-layer or draw layer for background/foreground ordering
    Map<String, String> physicsProperties,
    Map<String, String> inputProperties,
    Map<String, String> collisionProperties,
    Map<String, String> variables
) {}