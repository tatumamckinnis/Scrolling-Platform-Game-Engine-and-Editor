package oogasalad.engine.view;

import java.util.ResourceBundle;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.io.FileNotFoundException;
import javafx.scene.image.ImageView;
import oogasalad.engine.view.util.ViewObjectToImageConverter;
import oogasalad.fileparser.records.FrameData;

/**
 * The {@code ObjectImage} class represents a visual object in the game world, composed of an
 * {@link ImageView} for the sprite and a {@link Rectangle} hitbox for interactions or debugging. It
 * uses a {@link ViewObjectToImageConverter} to convert frame data into visual representation.
 */
public class ObjectImage {

  private static final ResourceBundle OBJECT_IMAGE_RESOURCES = ResourceBundle.getBundle(
      ObjectImage.class.getPackageName() + "." + "ObjectImage");

  private ViewObjectToImageConverter converter;
  private Rectangle hitBox;
  private ImageView imageView;
  private int spriteDx;
  private int spriteDy;
  private final String UUID;

  /**
   * Constructs an {@code ObjectImage} with the given parameters.
   *
   * @param UUID         the unique identifier for this object
   * @param Frame        the {@link FrameData} representing the current visual frame of the object
   * @param x            the initial X-coordinate of the object
   * @param y            the initial Y-coordinate of the object
   * @param hitBoxWidth  the width of the hitbox
   * @param hitBoxHeight the height of the hitbox
   * @param spriteDx     the X-offset of the image relative to the hitbox
   * @param spriteDy     the Y-offset of the image relative to the hitbox
   * @throws FileNotFoundException if the frame data image file cannot be found
   */
  public ObjectImage(String UUID, FrameData Frame, int x, int y, int hitBoxWidth, int hitBoxHeight,
      int spriteDx, int spriteDy)
      throws FileNotFoundException {
    this.UUID = UUID;
    converter = new ViewObjectToImageConverter();
    this.imageView = converter.convertFrameToView(Frame);
    imageView.setX(x + spriteDx);
    imageView.setY(y + spriteDy);
    this.spriteDx = spriteDx;
    this.spriteDy = spriteDy;
    displayHitBox(x, y, hitBoxWidth, hitBoxHeight);
  }

  /**
   * Returns the hitbox rectangle of the object.
   *
   * @return the hitbox as a {@link Rectangle}
   */
  public Rectangle getHitBox() {
    return hitBox;
  }

  /**
   * Returns the unique identifier associated with this object.
   *
   * @return the UUID string
   */
  public String getUUID() {
    return UUID;
  }

  /**
   * Returns the image view used to visually represent the object.
   *
   * @return the {@link ImageView} of the object
   */
  public ImageView getImageView() {
    return imageView;
  }

  /**
   * Updates the position of the object by setting new coordinates for both the hitbox and the
   * image, accounting for sprite offsets.
   *
   * @param x the new X-coordinate
   * @param y the new Y-coordinate
   */
  public void updateImageLocation(int x, int y) {
    hitBox.setX(x);
    hitBox.setY(y);
    imageView.setX(x + spriteDx);
    imageView.setY(y + spriteDy);
  }

  /**
   * Creates and configures the hitbox for the object.
   *
   * @param x            the X-coordinate of the hitbox
   * @param y            the Y-coordinate of the hitbox
   * @param hitBoxWidth  the width of the hitbox
   * @param hitBoxHeight the height of the hitbox
   */
  private void displayHitBox(int x, int y, int hitBoxWidth, int hitBoxHeight) {
    this.hitBox = new Rectangle(x, y, hitBoxWidth, hitBoxHeight);
    hitBox.setArcWidth(Double.parseDouble(OBJECT_IMAGE_RESOURCES.getString("ArcWidth")));
    hitBox.setArcHeight(Double.parseDouble(OBJECT_IMAGE_RESOURCES.getString("ArcHeight")));
    hitBox.setFill(Color.TRANSPARENT);  // Makes the center clear
    hitBox.setStroke(Color.RED);
  }
}

