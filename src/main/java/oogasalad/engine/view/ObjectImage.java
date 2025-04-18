package oogasalad.engine.view;

import java.io.FileNotFoundException;
import java.util.ResourceBundle;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.engine.view.util.ViewObjectToImageConverter;

/**
 * The {@code ObjectImage} class represents a visual object in the game world, composed of an
 * {@link ImageView} for the sprite and a {@link Rectangle} hitbox for interactions or debugging. It
 * uses a {@link ViewObjectToImageConverter} to convert frame data into visual representation.
 */
public class ObjectImage {

  private static final ResourceBundle OBJECT_IMAGE_RESOURCES = ResourceBundle.getBundle(
      ObjectImage.class.getPackageName() + "." + "ObjectImage");

  private final ViewObjectToImageConverter converter;
  private Rectangle hitBox;
  private final ImageView imageView;
  private final int spriteDx;
  private final int spriteDy;
  private final String UUID;

  /**
   * Constructs an {@code ObjectImage} with the given parameters.
   *
   * @param viewObject object that is converted to an image
   * @throws FileNotFoundException if the frame data image file cannot be found
   */
  public ObjectImage(ImmutableGameObject viewObject)
      throws FileNotFoundException {
    this.UUID = viewObject.getUUID();
    converter = new ViewObjectToImageConverter();
    this.imageView = converter.convertFrameToView(viewObject);
    imageView.setX(viewObject.getXPosition() + viewObject.getSpriteDx());
    imageView.setY(viewObject.getYPosition() + viewObject.getSpriteDy());
    this.spriteDx = viewObject.getSpriteDy();
    this.spriteDy = viewObject.getSpriteDy();
    displayHitBox(viewObject.getXPosition(), viewObject.getYPosition(), viewObject.getHitBoxWidth(),
        viewObject.getHitBoxHeight());
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

