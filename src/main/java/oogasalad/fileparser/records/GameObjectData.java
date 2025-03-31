package oogasalad.fileparser.records;

import java.util.Map;

/**
 *
 * @author Billy McCune
 */
public record GameObjectData(
    int id,
    String gameName,
    String type,
    String group, //entities, blocks, backgrounds
    String spriteName,
    String spriteFile,
    int x,
    int y,
    int layer, //z-layer or draw layer for background/foreground ordering
    Map <String, Map<String, String>> propertiesForObjectHandlersAndVariables
) {

}