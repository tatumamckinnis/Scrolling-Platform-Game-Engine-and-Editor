package oogasalad.engine.view.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oogasalad.engine.controller.ViewObject;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import oogasalad.engine.view.ObjectImage;
import oogasalad.fileparser.records.FrameData;

public class ViewObjectToImageConverter {

  private Map<String, ObjectImage> UUIDToImageMap;

  public ViewObjectToImageConverter() {
    UUIDToImageMap = new HashMap<>();
  }

  public List<ObjectImage> convertObjectsToImages(List<ViewObject> gameObjects)
      throws FileNotFoundException {
      List<ObjectImage> images = new ArrayList<>();
      for (ViewObject object : gameObjects) {
        if (UUIDToImageMap.containsKey(object.uuid())){
          UUIDToImageMap.get(object.uuid()).updateImageLocation(object.hitBoxXPosition(), object.hitBoxYPosition());
        }
        else {
          ObjectImage newViewObject = new ObjectImage(object.uuid(), object.currentFrame(), object.hitBoxXPosition(), object.hitBoxYPosition(), object.hitBoxWidth(), object.hitBoxHeight(), object.spriteDx(), object.spriteDy());
          images.add(newViewObject);
          UUIDToImageMap.put(object.uuid(), newViewObject);
        }
      }
      return images;
  }

  public Map<String, ObjectImage> getUUIDToImageMap() {
    if(UUIDToImageMap == null){
      UUIDToImageMap = new HashMap<>();
    }
    return UUIDToImageMap;
  }

  public ImageView convertFrameToView(FrameData frameData) throws FileNotFoundException {
    Image sprite = new Image(new FileInputStream(frameData.spriteFile()));
    // Create an ImageView for the sprite image
    ImageView imageView = new ImageView(sprite);
    // Define the viewport from the frame data
    Rectangle2D viewport = new Rectangle2D(
        frameData.x(),
        frameData.y(),
        frameData.width(),
        frameData.height()
    );

    // Apply the viewport
    imageView.setViewport(viewport);

    // Set the fit size so that the ImageView displays only the frame's dimensions
    imageView.setFitWidth(viewport.getWidth());
    imageView.setFitHeight(viewport.getHeight());

    return imageView;
  }

}
