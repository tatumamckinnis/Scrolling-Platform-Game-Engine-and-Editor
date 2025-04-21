package oogasalad.editor.view.tools;

import java.util.Objects;
import java.util.UUID;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.view.EditorGameView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A tool for deleting objects. When a grid square is clicked, it performs a hit test and notifies
 * the controller of deletion.
 *
 * @author Alana Zinkin
 */
public class DeleteTool implements ObjectInteractionTool {

  private static final Logger LOG = LogManager.getLogger(DeleteTool.class);

  private final EditorGameView editorView;
  private final EditorController editorController;

  /**
   * Constructs a new Delete Tool, which removes objects from the editor scene
   *
   * @param editorView       the EditorGameView instance; must not be null.
   * @param editorController the controller to use for selection notifications; must not be null.
   */
  public DeleteTool(EditorGameView editorView, EditorController editorController) {
    this.editorView = Objects.requireNonNull(editorView, "EditorGameView cannot be null.");
    this.editorController = Objects.requireNonNull(editorController,
        "EditorController cannot be null.");
    LOG.info("DeleteTool created.");
  }

  /**
   * Deletes the object at the given grid cell.
   *
   * @param worldX X-coordinate of the grid cell.
   * @param worldY Y-coordinate of the grid cell.
   */
  @Override
  public void interactObjectAt(double worldX, double worldY) {
    UUID id = editorController.getObjectIDAt(worldX, worldY);
    if (id != null) {
      editorController.requestObjectRemoval(id);
    } else {
      LOG.warn("No object found at ({}, {}) to delete.", worldX, worldY);
    }
  }

}
