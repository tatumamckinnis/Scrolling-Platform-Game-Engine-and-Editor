package oogasalad.engine.view.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import oogasalad.engine.model.object.ViewObject;
import oogasalad.engine.view.ObjectImage;
import oogasalad.fileparser.records.FrameData;

/**
 * The {@code ViewObjectToImageConverter} class is responsible for converting {@link ViewObject}
 * instances into {@link ObjectImage} representations that can be rendered in the game view. It also
 * provides utility methods to convert a single {@link FrameData} into an {@link ImageView}.
 */
public class ViewObjectToImageConverter {

  private final Map<String, ObjectImage> UUIDToImageMap;

  /**
   * Constructs a new {@code ViewObjectToImageConverter} with an empty UUID-to-image map.
   */
  public ViewObjectToImageConverter() {
    UUIDToImageMap = new HashMap<>();
  }

  /**
   * Converts a list of {@link ViewObject} instances to a list of {@link ObjectImage} instances.
   * This method reuses existing ObjectImages if the UUID has already been encountered, updating
   * their positions instead of recreating them.
   *
   * @param gameObjects the list of game objects to convert
   * @return a list of corresponding {@code ObjectImage} instances
   * @throws FileNotFoundException if a sprite image file cannot be found
   */
  public List<ObjectImage> convertObjectsToImages(List<ViewObject> gameObjects)
      throws FileNotFoundException {
    List<ObjectImage> images = new ArrayList<>();
    for (ViewObject object : gameObjects) {
      if (UUIDToImageMap.containsKey(object.getUuid())) {
        UUIDToImageMap.get(object.getUuid()).updateImageLocation(object.getX(), object.getY());
      } else {
        ObjectImage newViewObject = new ObjectImage(
            object.getUuid(),
            object.getCurrentFrame(),
            object.getX(),
            object.getY(),
            object.getHitBoxWidth(),
            object.getHitBoxHeight(),
            object.getSpriteDx(),
            object.getSpriteDy()
        );
        images.add(newViewObject);
        UUIDToImageMap.put(object.getUuid(), newViewObject);
      }
    }
    return images;
  }

  /**
   * Converts a single {@link FrameData} object to an {@link ImageView} configured with a viewport
   * to show only the relevant sprite portion.
   *
   * @param frameData the frame data defining the sprite image and its viewport
   * @return an {@code ImageView} representing the frame
   * @throws FileNotFoundException if the sprite file cannot be loaded
   */
  public ImageView convertFrameToView(FrameData frameData) throws FileNotFoundException {
    Image sprite = new Image(new FileInputStream(frameData.spriteFile()));
    ImageView imageView = new ImageView(sprite);

    Rectangle2D viewport = new Rectangle2D(
        frameData.x(),
        frameData.y(),
        frameData.width(),
        frameData.height()
    );

    imageView.setViewport(viewport);
    imageView.setFitWidth(viewport.getWidth());
    imageView.setFitHeight(viewport.getHeight());

    return imageView;
  }
}
