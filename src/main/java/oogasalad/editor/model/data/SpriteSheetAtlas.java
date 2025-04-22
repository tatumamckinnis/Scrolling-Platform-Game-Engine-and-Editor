package oogasalad.editor.model.data;

import java.util.List;
import oogasalad.editor.model.data.object.sprite.FrameData;

/**
 * All data extracted from an atlas XML file.
 *
 * @param atlasName The name of the atlas file
 * @param imagePath The URL of the image path
 * @param width     The width of the image sprite
 * @param height    The height of the image sprite
 * @param frames    The list of {@link FrameData}
 * @author Jacob You
 */
public record SpriteSheetAtlas(String atlasName, String imagePath, int width, int height,
                               List<FrameData> frames) {

}
