package oogasalad.engine.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import oogasalad.engine.model.object.GameObject;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import oogasalad.fileparser.records.FrameData;

public class GameObjectToViewObjectConverter {

  public List<ImageView> convertGameObjects(List<GameObject> gameObjects)
      throws FileNotFoundException {
      List<ImageView> imageViews = new ArrayList<>();
      for (GameObject gameObject : gameObjects) {
        System.out.println(gameObject);
        imageViews.add(convertGameObjectToView(gameObject));
      }
      return imageViews;
  }

  public ImageView convertGameObjectToView(GameObject gameObject) throws FileNotFoundException {
    FrameData baseSpriteData = gameObject.getSpriteData().baseImage();
    Image sprite = new Image(new FileInputStream(baseSpriteData.spriteFile()));

    // Create an ImageView for the sprite image
    ImageView imageView = new ImageView(sprite);

    // Define the viewport from the frame data
    Rectangle2D viewport = new Rectangle2D(
        baseSpriteData.x(),
        baseSpriteData.y(),
        baseSpriteData.width(),
        baseSpriteData.height()
    );

    // Apply the viewport
    imageView.setViewport(viewport);

    // Set the fit size so that the ImageView displays only the frame's dimensions
    imageView.setFitWidth(viewport.getWidth());
    imageView.setFitHeight(viewport.getHeight());

    System.out.println(gameObject.getSpriteData().baseImage().name());
    System.out.println(baseSpriteData.spriteFile());
    System.out.println("x:"+baseSpriteData.x());
    System.out.println("y:"+baseSpriteData.y());
    System.out.println("width:"+baseSpriteData.width());
    System.out.println("height:"+baseSpriteData.height());

    imageView.setX(gameObject.getX());
    imageView.setY(gameObject.getY());

    return imageView;
  }


}
