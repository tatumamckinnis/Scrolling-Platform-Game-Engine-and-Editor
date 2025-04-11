package oogasalad.fileparser.records;

/**
 * Represents the hitbox data parsed from an input source.
 * <p>
 * This record encapsulates the hitbox shape, dimensions (width and height),
 * and sprite offsets (dx and dy) used to position the hitbox relative to its associated sprite.
 * </p>
 *
 * @param shape         the shape of the hitbox.
 * @param hitBoxWidth  the width of the hitbox.
 * @param hitBoxHeight the height of the hitbox.
 * @param spriteDx     the horizontal offset of the sprite relative to the hitbox.
 * @param spriteDy     the vertical offset of the sprite relative to the hitbox.
 */
public record HitBoxData(
    String shape,
    int hitBoxWidth,
    int hitBoxHeight,
    int spriteDx,
    int spriteDy
) {

}

