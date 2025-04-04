package oogasalad.engine.view;

import java.util.ResourceBundle;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.io.FileNotFoundException;
import javafx.scene.image.ImageView;
import oogasalad.engine.view.util.ViewObjectToImageConverter;
import oogasalad.fileparser.records.FrameData;

public class ObjectImage {

  private static final ResourceBundle OBJECT_IMAGE_RESOURCES = ResourceBundle.getBundle(
      ObjectImage.class.getPackageName() + "." + "ObjectImage");
  private ViewObjectToImageConverter converter;
  private Rectangle hitBox;
  private ImageView imageView;
  private int spriteDx;
  private int spriteDy;
  private final String UUID;


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

  private void displayHitBox(int x, int y, int hitBoxWidth, int hitBoxHeight) {
    this.hitBox = new Rectangle(x, y, hitBoxWidth, hitBoxHeight);
    hitBox.setArcWidth(Double.parseDouble(OBJECT_IMAGE_RESOURCES.getString("ArcWidth")));
    hitBox.setArcHeight(Double.parseDouble(OBJECT_IMAGE_RESOURCES.getString("ArcHeight")));
    hitBox.setFill(Color.TRANSPARENT);  // Makes the center clear
    hitBox.setStroke(Color.RED);
  }


  public Rectangle getHitBox() {
    return hitBox;
  }

  public String getUUID() {
    return UUID;
  }

  public ImageView getImageView() {
    return imageView;
  }

  public void updateImageLocation(int x, int y) {
    hitBox.setX(x);
    hitBox.setY(y);
    imageView.setX(x + spriteDx);
    imageView.setY(y + spriteDy);
  }

}
