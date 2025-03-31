package oogasalad.engine.view;

import java.util.List;
import java.util.Random;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import oogasalad.engine.exception.RenderingException;
import oogasalad.engine.model.object.GameObject;

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
  private boolean isPaused;

  /**
   * Default constructor for a level view. Sets the level to pause.
   */
  public LevelView() {
    isPaused = true;
  }

  public void setPlay(boolean isPaused) {
    this.isPaused = isPaused;
  }

  @Override
  public void render() {
    // should do nothing
    Rectangle r1 = new Rectangle(50, 50, Color.RED);
    r1.setX(50);
    r1.setY(50);
    r1.setId("0");

    Rectangle r2 = new Rectangle(50, 50, Color.GREEN);
    r2.setX(150);
    r2.setY(50);
    r2.setId("1");

    Rectangle r3 = new Rectangle(50, 50, Color.BLUE);
    r3.setX(250);
    r3.setY(50);
    r3.setId("2");

    Text text = new Text(200, 200, "This is my level view");
    text.setId("3");
    this.getChildren().addAll(r1, r2, r3, text);
  }

  public void renderGameObjects(List<GameObject> gameObjects) throws RenderingException {
    // update any changed objects
    // newClass.update(newObjectsToUpdate
    Random r = new Random();
    int id = r.nextInt(3);
    for (Node node : this.getChildren()) {
      if (Integer.parseInt(node.getId()) == id) {
        Color randomColor = Color.color(r.nextDouble(), r.nextDouble(), r.nextDouble());
        ((Rectangle) node).setFill(randomColor);
      }
    }
  }
}
