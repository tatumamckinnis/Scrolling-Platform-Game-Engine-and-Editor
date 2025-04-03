package oogasalad.engine.view;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.io.FileNotFoundException;
import javafx.scene.image.ImageView;
import oogasalad.fileparser.records.FrameData;

public class viewGameObject {
    private Rectangle hitBox;
    private ImageView imageView;
    private GameObjectToViewObjectConverter converter;
    private int spriteDx;
    private int spriteDy;
    private String UUID;


  public viewGameObject(String UUID, FrameData Frame, int x, int y, int hitBoxWidth, int hitBoxHeight, int spriteDx, int spriteDy)
      throws FileNotFoundException {
    converter = new GameObjectToViewObjectConverter();
    this.hitBox = new Rectangle(x, y, hitBoxWidth, hitBoxHeight);
    hitBox.setArcWidth(20);
    hitBox.setArcHeight(20);
    hitBox.setFill(Color.TRANSPARENT);  // Makes the center clear
    hitBox.setStroke(Color.RED);      // Sets the outline color (change as needed)
    this.imageView = converter.convertFrameToView(Frame);
    imageView.setX(x + spriteDx);
    imageView.setY(y + spriteDy);
    this.spriteDx = spriteDx;
    this.spriteDy = spriteDy;
  }



  public Rectangle getHitBox() {
    return hitBox;
  }

  public String getUUID() {
    return imageView.getId();
  }

  public ImageView getImageView() {
    return imageView;
  }

  public void updateObjectLocation(int x, int y){
    hitBox.setX(x);
    hitBox.setY(y);
    imageView.setX(x+spriteDx);
    imageView.setY(y+spriteDy);
  }

}
