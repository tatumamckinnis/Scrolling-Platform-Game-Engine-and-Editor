package oogasalad.fileparser.records;

import java.util.UUID;

/**
 * Represents the data for a game object parsed from an input source.
 * <p>
 * This record encapsulates the blueprint identifier, a unique identifier, the position
 * (x and y coordinates), and the drawing layer (z-layer) to determine background or foreground
 * ordering.
 * </p>
 *
 * @param blueprintId the blueprint identifier associated with the game object.
 * @param uniqueId    the unique identifier for the game object.
 * @param x           the x-coordinate of the game object's position.
 * @param y           the y-coordinate of the game object's position.
 * @param layer       the draw layer or z-layer for background/foreground ordering.
 */
public record GameObjectData(
    int blueprintId,
    UUID uniqueId,
    int x,
    int y,
    int layer,
    String layerName // Purely for editor purposes, not used in engine
) {

}
