package oogasalad.engine.view;

import javafx.scene.Group;
import javafx.scene.Node;

/**
 * This is an abstract class representing any visual component in the game.
 * It will be extended by all classes that need to be displayed in the scene.
 *
 * @author Aksel Bell
 */
public abstract class Display extends Node {

  /**
   * This method should be implemented by subclasses to render the display.
   * It is used to show the content, layout, or other visual elements.
   *
   * @param root the overarching root to add the desired visual elements to.
   */
  public abstract void render(Group root);

  /**
   * This method can be used to update any visual elements.
   * For example, updating scores, health, or animation frames.
   */
  public abstract void update();

  /**
   * This method will provide the option to hide the display from the scene.
   * Subclasses can define how they should be hidden.
   */
  public void hide() {
    this.setVisible(false);
  }

  /**
   * This method will make the display visible again.
   */
  public void show() {
    this.setVisible(true);
  }

  /**
   * This method checks if the display is currently visible.
   */
  public boolean isCurrentlyVisible() {
    return this.isVisible();  // Using Node's built-in isVisible method.
  }
}