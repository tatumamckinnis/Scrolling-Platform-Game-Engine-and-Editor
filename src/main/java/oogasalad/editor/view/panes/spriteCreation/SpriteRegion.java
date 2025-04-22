package oogasalad.editor.view.panes.spriteCreation;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

/**
 * Represents a named rectangular region within a sprite sheet, optionally holding a cropped preview
 * image for display purposes.
 * <p>
 * Used by the sprite‑sheet processor to manage the bounds and naming of each individual sprite
 * region extracted from the sheet.
 * </p>
 *
 * @author Jacob You
 */
public class SpriteRegion {

  private String name;
  private Rectangle2D bounds;
  private Image preview;

  /**
   * Constructs a new SpriteRegion with the given name and sheet coordinates.
   *
   * @param name   the identifier shown in the regions table
   * @param bounds the rectangular area on the sheet corresponding to this region
   */
  public SpriteRegion(String name, Rectangle2D bounds) {
    this.name = name;
    this.bounds = bounds;
  }

  /**
   * Returns the region's name.
   *
   * @return the current name of this sprite region
   */
  public String getName() {
    return name;
  }

  /**
   * Sets a new name for this region.
   *
   * @param name the new name to assign
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the bounds of this region in the sprite sheet's coordinate space.
   *
   * @return the {@link Rectangle2D} defining the region
   */
  public Rectangle2D getBounds() {
    return bounds;
  }

  /**
   * Returns the X (minimum X) coordinate of the region.
   *
   * @return the X position in sheet pixels
   */
  public double getX() {
    return bounds.getMinX();
  }

  /**
   * Returns the Y (minimum Y) coordinate of the region.
   *
   * @return the Y position in sheet pixels
   */
  public double getY() {
    return bounds.getMinY();
  }

  /**
   * Returns the width of the region.
   *
   * @return the width in pixels
   */
  public double getWidth() {
    return bounds.getWidth();
  }

  /**
   * Returns the height of the region.
   *
   * @return the height in pixels
   */
  public double getHeight() {
    return bounds.getHeight();
  }

  /**
   * Returns the preview image for this region, if one has been set.
   *
   * @return an {@link Image} cropped to this region, or {@code null} if none
   */
  public Image getPreview() {
    return preview;
  }

  /**
   * Assigns a preview image for this region.
   * <p>
   * Typically this would be a subimage of the sprite sheet corresponding to {@link #getBounds()},
   * used for quick visual display.
   * </p>
   *
   * @param preview the cropped preview {@link Image} to assign
   */
  public void setPreview(Image preview) {
    this.preview = preview;
  }
}
