package main.java.oogasalad.view.editor.tools;

import javafx.scene.input.MouseEvent;
import main.java.oogasalad.view.editor.EditorGameView;
import main.java.oogasalad.view.editor.tools.objectfactory.EntityObjectFactory;


/**
 * Tool for adding entity objects to the game scene.
 * This tool allows users to add a new game object to the scene.
 * Uses a factory to create appropriate game objects for different games.
 *
 * @author Tatum
 */
public class EntityObjectPlacementTool {
  private final EditorGameView editorView;
  //private final EditorManagerAPI editorManager;
  private EntityObjectFactory objectFactory;

  /**
   * Creates a new entity object placement tool
   *
   * @param editorView The editor view to place objects on
   * @param editorManager The editor manager to use for backend operations
   * @param objectFactory The factory to create entity objects
   */
  public EntityObjectPlacementTool(EditorGameView editorView,
      //EditorManagerAPI editorManager,
      EntityObjectFactory objectFactory) {
    this.editorView = editorView;
   // this.editorManager = editorManager;
    this.objectFactory = objectFactory;

    setupClickHandler();
  }

  /**
   * Sets the object factory for creating entity objects
   * This allows switching between different types of games
   *
   * @param objectFactory The factory to create entity objects
   */
  public void setObjectFactory(EntityObjectFactory objectFactory) {
    this.objectFactory = objectFactory;
  }

  /**
   * Sets up the click handler for the editor view
   */
  private void setupClickHandler() {
    editorView.setOnGridClick(this::handleGridClick);
  }

  /**
   * Handles grid click events to place entities
   *
   * @param event The mouse click event
   */
  private void handleGridClick(MouseEvent event) {
    int gridX = (int)(event.getX() / editorView.getCellSize());
    int gridY = (int)(event.getY() / editorView.getCellSize());
    placeEntityAt(gridX, gridY);
  }

  /**
   * Places an entity object at the specified grid position
   *
   * @param gridX X-coordinate on the grid
   * @param gridY Y-coordinate on the grid
   */
  public void placeEntityAt(int gridX, int gridY) {
    double x = gridX * editorView.getCellSize();
    double y = gridY * editorView.getCellSize();

    //EditorObject entityObject = objectFactory.createEntityObject(x, y);

    //editorManager.addObject(entityObject);
  }
}
