package oogasalad.editor.view.tools;

/**
 * Interface for tools that place objects on the game grid
 */
public interface ObjectInteractionTool {

  /**
   * Interacts with an object at the specified grid position
   *
   * @param gridX X-coordinate on the grid
   * @param gridY Y-coordinate on the grid
   */
  void interactObjectAt(double gridX, double gridY);

  void interactObjectAt(int gridX, int gridY);
}
