package oogasalad.editor.view.tools;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.view.components.EditorGameView;
import oogasalad.fileparser.records.BlueprintData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Extension of PrefabPlacementTool that supports placing multiple prefab objects through a
 * click-and-drag operation. Keeps track of grid cells that have been populated during a single drag
 * to avoid duplicates.
 *
 * @author Billy McCune
 */
public class DragPrefabPlacementTool extends PrefabPlacementTool {

  private static final Logger LOG = LogManager.getLogger(DragPrefabPlacementTool.class);

  private final Set<String> populated = new HashSet<>();
  private final Set<UUID> placedIds = new HashSet<>();
  private boolean isDragging = false;
  private boolean suppressClick = false;
  private int cellSize;

  /**
   * Creates a new drag prefab placement tool.
   *
   * @param editorView       The editor view to get grid information from.
   * @param editorController The controller to handle object creation requests.
   * @author Billy McCune, Jacob You
   */
  public DragPrefabPlacementTool(EditorGameView editorView, EditorController editorController) {
    super(editorView, editorController);
    LOG.info("Created DragPrefabPlacementTool");
  }

  /**
   * Starts a drag operation at the specified coordinates.
   *
   * @param worldX X-coordinate in world space.
   * @param worldY Y-coordinate in world space.
   */
  public void startDrag(double worldX, double worldY) {
    BlueprintData selectedPrefab = getEditorView().getSelectedPrefab();

    if (selectedPrefab == null) {
      LOG.warn("Drag operation attempted but no prefab is selected.");
      return;
    }

    cellSize = getEditorView().getCellSize();
    if (getEditorController().isSnapToGrid()) {
      worldX = Math.floor(worldX / cellSize) * cellSize;
      worldY = Math.floor(worldY / cellSize) * cellSize;
    }

    LOG.debug("Drag-run begins – cell size {}, start ({},{})",
        cellSize, worldX, worldY);

    isDragging = true;
    populated.clear();
    placedIds.clear();

    placeAt((int) worldX, (int) worldY);
  }

  /**
   * Handles drag movement to the specified coordinates, placing objects in cells that haven't been
   * populated during this drag operation.
   *
   * @param worldX X-coordinate in world space.
   * @param worldY Y-coordinate in world space.
   */
  public void dragTo(double worldX, double worldY) {
    if (!isDragging) {
      return;
    }

    int snapX = (int) Math.floor(worldX / cellSize) * cellSize;
    int snapY = (int) Math.floor(worldY / cellSize) * cellSize;

    placeAt(snapX, snapY);
  }

  /**
   * Ends the current drag operation, clearing any tracking data. Also ensures all placed objects
   * have their images properly loaded.
   */
  public void endDrag() {
    LOG.debug("Drag-run finished – {} objects placed", placedIds.size());
    isDragging = false;
    suppressClick = true;
    populated.clear();
    placedIds.clear();
    getEditorView().refreshDisplay();
  }


  private void placeAt(int x, int y) {
    String key = x + "," + y;
    if (populated.contains(key)) {
      return;
    }

    interactObjectAt(x, y);
    UUID newId = getEditorController().getCurrentSelectedObjectId();
    if (newId != null) {
      placedIds.add(newId);
    }

    LOG.trace("placed prefab at ({},{}); total this run {}", x, y, populated.size());
    populated.add(key);
  }

  /**
   * PrefabPlacementTool already has interactObjectAt(x,y). This is overwritten just to swallow the
   * first click right after a drag-run to avoid a double place.
   */
  @Override
  public void interactObjectAt(double worldX, double worldY) {
    if (suppressClick) {
      suppressClick = false;
      LOG.trace("Click suppressed after drag-run");
      return;
    }
    super.interactObjectAt(worldX, worldY);
  }
} 