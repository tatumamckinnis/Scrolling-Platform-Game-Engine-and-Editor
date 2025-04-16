package oogasalad.fileparser.records;

import java.util.List;

/**
 * Represents a named animation composed of a sequence of frame names with a uniform frame
 * duration.
 *
 * <p>This record defines how a sprite is animated by specifying the frame to frame speed and
 * the ordered list of frame identifiers to display.
 *
 * <p>This record assumes:
 * <ul>
 * <li>{@code name} uniquely identifies the animation (e.g., "walk", "jump").</li>
 * <li>{@code frameLen} represents the duration of each frame in ticks.</li>
 * <li>{@code frameNames} refers to valid
 * {@code FrameData.name} values defined in the sprite.</li>
 * </ul>
 *
 * <p>Used during sprite animation playback and when exporting animation data to the engine.
 *
 * @author Billy McCune
 */
public record AnimationData(
    String name,
    double frameLen,
    List<String> frameNames
) {

  public double frameLength() {
    return frameLen;
  }
}
