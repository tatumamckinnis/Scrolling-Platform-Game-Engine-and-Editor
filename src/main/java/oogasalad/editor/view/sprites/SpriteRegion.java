package oogasalad.editor.view.sprites;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

/**
 * Simple data holder for a single sprite region that will be cut from a sprite‑sheet.
 */
public class SpriteRegion {

  private String name;
  private Rectangle2D bounds;
  private Image preview;

  /**
   * Constructs a new {@code SpriteRegion}.
   *
   * @param name   initial name shown in the table
   * @param bounds rectangular region in sheet coordinates
   */
  public SpriteRegion(String name, Rectangle2D bounds) {
    this.name = name;
    this.bounds = bounds;
  }

  public String getName() {
    return name;
  }

  public void setName(String n) {
    name = n;
  }

  public Rectangle2D getBounds() {
    return bounds;
  }

  public double getX() {
    return bounds.getMinX();
  }

  public double getY() {
    return bounds.getMinY();
  }

  public double getWidth() {
    return bounds.getWidth();
  }

  public double getHeight() {
    return bounds.getHeight();
  }

  public Image getPreview() {
    return preview;
  }

  public void setPreview(Image p) {
    preview = p;
  }
}
