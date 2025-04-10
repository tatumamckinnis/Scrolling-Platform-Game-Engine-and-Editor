package oogasalad.fileparser.records;

import java.util.List;
import java.util.Map;

/**
 * Represents the level data parsed from an input source.
 * <p>
 * This record encapsulates the basic properties of a level
 * such as its name and boundary coordinates,
 * and includes a mapping of blueprint data
 * (keyed by blueprint ID) as well as a list of game objects
 * present within the level.
 * </p>
 *
 * @param name              the name of the level.
 * @param minX              the minimum x-coordinate for the level boundaries.
 * @param minY              the minimum y-coordinate for the level boundaries.
 * @param maxX              the maximum x-coordinate for the level boundaries.
 * @param maxY              the maximum y-coordinate for the level boundaries.
 * @param gameBluePrintData a map of blueprint data, where each key is a blueprint ID.
 * @param gameObjects       a list of game object data present in the level.
 *
 * @author Billy
 */
public record LevelData(
    String name,
    int minX,
    int minY,
    int maxX,
    int maxY,
    Map<Integer, BlueprintData> gameBluePrintData,
    List<GameObjectData> gameObjects
) {

}
