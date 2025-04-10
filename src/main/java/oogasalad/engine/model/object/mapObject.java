package oogasalad.engine.model.object;

/**
 * Object representing the game scene width and height
 * @param minX smallest x coordinate
 * @param minY smallest y coordinate
 * @param maxX largest x coordinate
 * @param maxY largest y coordinate
 */
public record mapObject(
    int minX,
    int minY,
    int maxX,
    int maxY
) {

}