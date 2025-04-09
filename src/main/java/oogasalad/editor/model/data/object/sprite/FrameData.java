package oogasalad.editor.model.data.object.sprite;

/**
 * Represents data for a single frame of a sprite animation. This record contains the name of the
 * frame along with its position (x, y) and dimensions (width, height) as defined within the sprite
 * sheet.
 *
 * @param name   the identifier or name of the frame
 * @param x      the x-coordinate of the frame within the sprite sheet
 * @param y      the y-coordinate of the frame within the sprite sheet
 * @param width  the width of the frame in pixels
 * @param height the height of the frame in pixels
 * @author Jacob You
 */
public record FrameData(String name, int x, int y, int width, int height) {

}
