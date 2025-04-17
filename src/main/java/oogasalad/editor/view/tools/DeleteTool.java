package oogasalad.editor.view.tools;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import javafx.scene.paint.Color;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.view.EditorGameView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class DeleteTool implements ObjectInteractionTool {
  private static final Logger LOG = LogManager.getLogger(DeleteTool.class);

  private final EditorGameView editorView;
  private final EditorController editorController;

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
