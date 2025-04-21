package oogasalad.editor.view.tools;

import java.util.Objects;
import java.util.UUID;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.view.EditorGameView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A tool for selecting objects rather than creating new ones. When a grid square is clicked, it
 * performs a hit test and notifies the controller of selection.
 */
public class SelectionTool implements ObjectInteractionTool {

  private static final Logger LOG = LogManager.getLogger(SelectionTool.class);

  private final EditorGameView editorView;
  private final EditorController editorController;

  /**
   * Constructs a new SelectionTool.
   *
   * @param editorView       the EditorGameView instance; must not be null.
   * @param editorController the controller to use for selection notifications; must not be null.
   */
  public SelectionTool(EditorGameView editorView, EditorController editorController) {
    this.editorView = Objects.requireNonNull(editorView, "EditorGameView cannot be null.");
    this.editorController = Objects.requireNonNull(editorController,
        "EditorController cannot be null.");
    LOG.info("SelectionTool created.");
  }

  /**
   * Selects the object at the given grid cell.
   *
   * @param worldX X-coordinate of the grid cell.
   * @param worldY Y-coordinate of the grid cell.
   */
  @Override
  public void interactObjectAt(double worldX, double worldY) {
    LOG.debug("Attempting to select at grid coordinates ({}, {})", worldX, worldY);
    UUID id = editorController.getObjectIDAt(worldX, worldY);
    if (id != null) {
      editorController.notifyObjectSelected(id);
    } else {
      editorController.notifyObjectDeselected();
    }
  }
}
