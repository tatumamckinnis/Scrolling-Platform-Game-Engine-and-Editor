package oogasalad.engine.view;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import oogasalad.Main;
import oogasalad.engine.exception.RenderingException;
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.view.util.GameObjectToViewObjectConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is the view for a level in a game. It includes all visual elements in a level.
 *
 * @author Aksel Bell
 */
public class LevelView extends Display {
  // has a background and foreground
  // need to store all the game objects and render all of them
  // talks to the camera API to show a certain part of the screen
  // upon update will rerender any objects with the IDs specified
  private static final Logger LOG = LogManager.getLogger();
  private Map<String,ImageView> spriteImageMap;

  /**
   * Default constructor for a level view. Sets the level to pause.
   */
  public LevelView() {
    spriteImageMap = new HashMap<>();
  }

  /**
   * @see Display#render()
   */
  @Override
  public void render() {
    // should do nothing, maybe add empty text saying game is loading...? or maybe just add the background UI?
    LOG.info("Rendering level...");
    setInitialCameraPosition();
  }

  /**
   * Re-renders all game objects that have been updated in the backend.
   * @param gameObjects a list of gameObjects with objects to be updated visually
   * @throws RenderingException thrown if there is an error while rendering
   */
  public void renderGameObjects(List<GameObject> gameObjects)
      throws RenderingException, FileNotFoundException {
    GameObjectToViewObjectConverter converter = new GameObjectToViewObjectConverter();
    List<ImageView> sprites =  converter.convertGameObjects(gameObjects,spriteImageMap);
    spriteImageMap = converter.getImagetoUUIDMap();
    this.getChildren().addAll(sprites);
  }

  /**
   * Method to set camera fixed on portion of map.
   */
  private void setInitialCameraPosition() {
    double translateX = -100;
    double translateY = -100;

    this.setTranslateX(-translateX);
    this.setTranslateY(-translateY);
  }

  /**
   * Sample method to move camera.
   */
  private void moveRight() {
    double translateX = 1;

    this.setTranslateX(this.getTranslateX() - translateX);
  }
}
