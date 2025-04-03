package oogasalad.engine.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oogasalad.engine.model.object.GameObject;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import oogasalad.fileparser.records.FrameData;

public class GameObjectToViewObjectConverter {

  private Map<String, viewGameObject> imagetoUUIDMap;
  public List<viewGameObject> convertGameObjects(List<GameObject> gameObjects, Map<String,viewGameObject> spriteMap)
      throws FileNotFoundException {
      List<viewGameObject> viewObjects = new ArrayList<>();
      this.imagetoUUIDMap = spriteMap;
      for (GameObject gameObject : gameObjects) {
        if (imagetoUUIDMap.containsKey(gameObject.getUuid())){
          imagetoUUIDMap.get(gameObject.getUuid()).updateObjectLocation(gameObject.getX(), gameObject.getY());
        }
        else {
          viewGameObject newViewObject = new viewGameObject(gameObject.getUuid(),gameObject.getCurrentFrame(),gameObject.getX(),gameObject.getY(),gameObject.getHitBoxWidth(),gameObject.getHitBoxHeight(),gameObject.getmyHitBoxData().spriteDx(),gameObject.getmyHitBoxData().spriteDy());
          viewObjects.add(newViewObject);
          imagetoUUIDMap.put(gameObject.getUuid(),newViewObject);
          System.out.println(gameObject.getUuid());
        }
      }
      return viewObjects;
  }

  public Map<String,viewGameObject> getImagetoUUIDMap() {
    if(imagetoUUIDMap == null){
      imagetoUUIDMap = new HashMap<>();
    }
    return imagetoUUIDMap;
  }

  public ImageView convertFrameToView(FrameData frameData) throws FileNotFoundException {
    FrameData baseSpriteData = frameData;
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

    return imageView;
  }


}
