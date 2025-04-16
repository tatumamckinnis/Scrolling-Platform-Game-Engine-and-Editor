package oogasalad.fileparser.records;

import java.io.File;
import java.util.List;

/**
 * Represents a single sprite within a sprite sheet, including its name, position, dimensions, base
 * image, animation frames, and animation sequences.
 *
 * <p>This record is used by {@code SpriteSheetData} and is essential for both
 * rendering the sprite and supporting animations in the game engine or editor.
 *
 * <p>This record assumes:
 * <ul>
 * <li>{@code name} uniquely identifies this sprite within the sheet.</li>
 * <li>{@code x}, {@code y}, {@code width}, and {@code height} define the
 * bounding box of the sprite within the overall image.</li>
 * <li>{@code baseImage} defines the default
 * appearance of the sprite (e.g., for use in the editor).</li>
 * <li>{@code frames} contain named images that can be used in animations.</li>
 * <li>{@code animations} define sequences of frames with timing information.</li>
 * </ul>
 *
 * <p>Used during sprite parsing, display setup, and editor visualization.
 *
 * @author Billy McCune
 */
public record SpriteData(
    String name,
    File spriteFile,
    FrameData baseImage,
    List<FrameData> frames,
    List<AnimationData> animations
) {

  public FrameData baseFrame() {
    return baseImage;
  }
}