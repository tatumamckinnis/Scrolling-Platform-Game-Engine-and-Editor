package oogasalad.fileparser.records;

import java.io.File;

/**
 * Represents a single frame of a sprite animation or a static image used in the editor.
 *
 * <p>This frame is defined by its name and rectangular region within a sprite sheet image.
 * It can be used either as part of a sprite's default appearance or as an element in an animation.
 *
 * <p>This record assumes:
 * <ul>
 *   <li>{@code name} uniquely identifies the frame (e.g., \"DinoJump\", \"DinoDuck1\").</li>
 *   <li>{@code x}, {@code y}, {@code width}, and {@code height} define the region of the sprite sheet image this frame covers.</li>
 * </ul>
 *
 * <p>Used during sprite parsing and animation rendering.
 *
 * @author Billy McCune
 */
public record FrameData(
    String name,
    int x,
    int y,
    int width,
    int height,
    File spriteFile
) {}
