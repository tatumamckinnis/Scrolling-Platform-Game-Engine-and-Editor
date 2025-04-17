package oogasalad.fileparser.records;

import java.util.List;

/**
 * Stores the data for a single sprite sheet with all the information necessary to store and save a
 * sprite sheet atlas XML file.
 *
 * @param imagePath   The URL path of the image
 * @param sheetWidth  The width of the sprite sheet in pixels
 * @param sheetHeight The height of the sprite sheet in pixels
 * @param frames      The {@link List} of {@link FrameData} objects holding each frame of the
 *                    object.
 * @author Jacob You
 */
public record SpriteSheetData(String imagePath, int sheetWidth, int sheetHeight,
                              List<FrameData> frames) {
}
