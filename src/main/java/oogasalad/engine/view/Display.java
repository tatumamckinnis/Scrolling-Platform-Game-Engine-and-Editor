package oogasalad.engine.view;

import javafx.scene.Group;

/**
 * This is an abstract class representing any visual component in the game.
 * It will be extended by all classes that need to be displayed in the scene.
 *
 * @author Aksel Bell
 */
public abstract class Display extends Group {

  /**
   * This method should be implemented by subclasses to render the display.
   * It is used to show the content, layout, or other visual elements.
   */
  public abstract void render();

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
    return this.isVisible();
  }
}