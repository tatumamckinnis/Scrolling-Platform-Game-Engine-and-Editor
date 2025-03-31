package oogasalad.game.file.parser.records;

import java.util.List;

/**
 * Represents a sprite sheet which defines the image source and its associated sprites.
 *
 * <p>This record assumes:
 * <ul>
 *   <li>The image file at {@code imagePath} exists and is valid.</li>
 *   <li>Width and height define the full resolution of the sprite sheet image.</li>
 *   <li>Each {@code SpriteData} object in the list references a subsection of the image.</li>
 * </ul>
 *
 * <p>Used primarily during level file parsing and rendering setup.
 *
 * @author Billy McCune
 */
public record SpriteSheetData(
    String imagePath,
    int width,
    int height,
    List<SpriteData> sprites
) {}
