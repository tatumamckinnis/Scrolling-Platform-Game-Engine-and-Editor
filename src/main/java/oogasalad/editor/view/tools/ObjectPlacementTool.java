package oogasalad.editor.view.tools;

/**
 * Interface for tools that place objects on the game grid
 */
public interface ObjectPlacementTool {
  /**
   * Places an object at the specified grid position
   *
   * @param gridX X-coordinate on the grid
   * @param gridY Y-coordinate on the grid
   */
  void placeObjectAt(int gridX, int gridY);
}
