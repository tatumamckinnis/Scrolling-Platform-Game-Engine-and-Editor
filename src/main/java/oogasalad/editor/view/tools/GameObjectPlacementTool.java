package oogasalad.editor.view.tools;

import java.util.Objects;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.view.components.EditorGameView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Concrete implementation of ObjectInteractionTool for placing standard game objects
 * like entities or enemies. Delegates actual object creation to the EditorController.
 * (DESIGN-04: DRY, DESIGN-09: MVC, DESIGN-11, DESIGN-20: Strategy Pattern)
 * @author Tatum McKinnis
 */
public class GameObjectPlacementTool implements ObjectInteractionTool {

  private static final Logger LOG = LogManager.getLogger(GameObjectPlacementTool.class);

  protected final EditorGameView editorView;
  private final EditorController editorController;
  private final String objectGroup;
  private final String objectNamePrefix;

  /**
   * Creates a new game object placement tool.
   *
   * @param editorView       The editor view to get grid information from.
   * @param editorController The controller to handle object creation requests.
   * @param objectGroup      The group/type identifier for the objects created by this tool (e.g.,
   *                         "PLAYER").
   * @param objectNamePrefix The prefix used for generating default names (e.g., "Player_").
   */
  public GameObjectPlacementTool(EditorGameView editorView, EditorController editorController,
      String objectGroup, String objectNamePrefix) {
    this.editorView = Objects.requireNonNull(editorView, "EditorGameView cannot be null.");
    this.editorController = Objects.requireNonNull(editorController,
        "EditorController cannot be null.");
    this.objectGroup = Objects.requireNonNull(objectGroup, "objectGroup cannot be null.");
    this.objectNamePrefix = Objects.requireNonNull(objectNamePrefix,
        "objectNamePrefix cannot be null.");
    LOG.info("Created GameObjectPlacementTool for type: {}", objectGroup);
  }

  /**
   * Handles the logic for initiating object placement when the grid is clicked. Calculates position
   * and calls the EditorController to handle the actual creation.
   *
   * @param gridX X-coordinate on the grid.
   * @param gridY Y-coordinate on the grid.
   */
  @Override
  public void interactObjectAt(double gridX, double gridY) {
    try {
      int cellSize = editorView.getCellSize();
      if (cellSize <= 0) {
        LOG.error("Invalid cell size ({}) obtained from EditorGameView. Cannot place object.",
            cellSize);
        return;
      }

      editorController.requestObjectPlacement(objectGroup, objectNamePrefix, gridX, gridY,
          cellSize);
      LOG.debug("Delegated object placement request to controller for type '{}' at world ({}, {})",
          objectGroup, gridX, gridY);

    } catch (Exception e) {
      LOG.error("Error during placement request for object type '{}' at ({}, {}): {}", objectGroup,
          gridX, gridY, e.getMessage(), e);
    }
  }
  
  /**
   * Gets the editor view used by this tool.
   * 
   * @return The EditorGameView instance.
   */
  protected EditorGameView getEditorView() {
    return editorView;
  }
  
  /**
   * Gets the editor controller used by this tool.
   * 
   * @return The EditorController instance.
   */
  protected EditorController getEditorController() {
    return editorController;
  }
  
  /**
   * Gets the object group/type for this tool.
   * 
   * @return The object group string.
   */
  protected String getObjectGroup() {
    return objectGroup;
  }
  
  /**
   * Gets the name prefix used for objects created by this tool.
   * 
   * @return The name prefix string.
   */
  protected String getObjectNamePrefix() {
    return objectNamePrefix;
  }
}
