package oogasalad.editor.view.tools;


import javafx.scene.input.MouseEvent;
import oogasalad.editor.view.EditorGameView;
import oogasalad.editor.view.tools.objectfactory.EnemyObjectFactory;

/**
 * Tool for adding enemy objects to the game scene.
 * This tool allows users to add a new enemy object to the scene.
 * Uses a factory to create appropriate enemy objects for different games.
 *
 * @author Tatum McKinnis
 */
public class EnemyObjectPlacementTool {
  private final EditorGameView editorView;
  //private final EditorManagerAPI editorManager;
  private EnemyObjectFactory objectFactory;
  private String enemyType;

  /**
   * Creates a new enemy object placement tool
   *
   * @param editorView The editor view to place objects on
   * @param editorManager The editor manager to use for backend operations
   * @param objectFactory The factory to create enemy objects
   * @param defaultEnemyType The default enemy type to create
   */
  public EnemyObjectPlacementTool(EditorGameView editorView,
      //EditorManagerAPI editorManager,
      EnemyObjectFactory objectFactory,
      String defaultEnemyType) {
    this.editorView = editorView;
    //this.editorManager = editorManager;
    this.objectFactory = objectFactory;
    this.enemyType = defaultEnemyType;

    setupClickHandler();
  }

  /**
   * Sets the object factory for creating enemy objects
   * This allows switching between different types of games
   *
   * @param objectFactory The factory to create enemy objects
   */
  public void setObjectFactory(EnemyObjectFactory objectFactory) {
    this.objectFactory = objectFactory;
  }

  /**
   * Sets up the click handler for the editor view
   */
  private void setupClickHandler() {
    editorView.setOnGridClick(this::handleGridClick);
  }

  /**
   * Sets the type of enemy to place
   *
   * @param enemyType Type of enemy
   */
  public void setEnemyType(String enemyType) {
    this.enemyType = enemyType;
  }

  /**
   * Handles grid click events to place enemies
   *
   * @param event The mouse click event
   */
  private void handleGridClick(MouseEvent event) {
    int gridX = (int)(event.getX() / editorView.getCellSize());
    int gridY = (int)(event.getY() / editorView.getCellSize());
    placeEnemyAt(gridX, gridY);
  }

  /**
   * Places an enemy object at the specified grid position
   *
   * @param gridX X-coordinate on the grid
   * @param gridY Y-coordinate on the grid
   */
  public void placeEnemyAt(int gridX, int gridY) {
    double x = gridX * editorView.getCellSize();
    double y = gridY * editorView.getCellSize();

    if (isValidPosition(gridX, gridY)) {
      //EditorObject enemyObject = objectFactory.createEnemyObject(enemyType, x, y);
      //editorManager.addObject(enemyObject);
    }
  }

  /**
   * Checks if the grid position is valid (within bounds)
   *
   * @param gridX X-coordinate on the grid
   * @param gridY Y-coordinate on the grid
   * @return true if position is valid, false otherwise
   */
  private boolean isValidPosition(int gridX, int gridY) {
    int maxGridX = editorView.getGridWidth() / editorView.getCellSize();
    int maxGridY = editorView.getGridHeight() / editorView.getCellSize();

    return gridX >= 0 && gridX < maxGridX && gridY >= 0 && gridY < maxGridY;
  }
}
