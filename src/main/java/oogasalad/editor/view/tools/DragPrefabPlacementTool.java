package oogasalad.editor.view.tools;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import oogasalad.editor.controller.EditorController;
import oogasalad.editor.model.data.object.EditorObject;
import oogasalad.editor.view.components.EditorGameView;
import oogasalad.fileparser.records.BlueprintData;

/**
 * Extension of PrefabPlacementTool that supports placing multiple prefab objects
 * through a click-and-drag operation. Keeps track of grid cells that have been
 * populated during a single drag to avoid duplicates.
 */
public class DragPrefabPlacementTool extends PrefabPlacementTool {

  private static final Logger LOG = LogManager.getLogger(DragPrefabPlacementTool.class);
  
  private final Set<String> populatedCells = new HashSet<>();
  private final Set<UUID> placedObjectIds = new HashSet<>();
  private boolean isDragging = false;
  private int lastCellX = Integer.MIN_VALUE;
  private int lastCellY = Integer.MIN_VALUE;
  
  /**
   * Creates a new drag prefab placement tool.
   *
   * @param editorView       The editor view to get grid information from.
   * @param editorController The controller to handle object creation requests.
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
    
    LOG.debug("Starting drag operation from ({}, {}) with prefab: '{}', group: '{}'", 
              worldX, worldY, selectedPrefab.type(), selectedPrefab.group());
    isDragging = true;
    populatedCells.clear();
    placedObjectIds.clear();
    
    // Convert to cell coordinates to track the first cell
    int cellSize = getEditorView().getCellSize();
    int cellX = (int) Math.floor(worldX / cellSize);
    int cellY = (int) Math.floor(worldY / cellSize);
    
    // Mark this cell as populated
    String cellKey = getCellKey(cellX, cellY);
    populatedCells.add(cellKey);
    
    // Place the first object
    interactObjectAt(worldX, worldY);
    
    lastCellX = cellX;
    lastCellY = cellY;
  }
  
  /**
   * Handles drag movement to the specified coordinates, placing objects in cells
   * that haven't been populated during this drag operation.
   * 
   * @param worldX X-coordinate in world space.
   * @param worldY Y-coordinate in world space.
   */
  public void dragTo(double worldX, double worldY) {
    if (!isDragging) {
      return;
    }
    
    BlueprintData selectedPrefab = getEditorView().getSelectedPrefab();
    if (selectedPrefab == null) {
      LOG.warn("Drag operation in progress but prefab is no longer selected.");
      return;
    }
    
    int cellSize = getEditorView().getCellSize();
    int cellX = (int) Math.floor(worldX / cellSize);
    int cellY = (int) Math.floor(worldY / cellSize);
    
    // Skip if we're still in the same cell
    if (cellX == lastCellX && cellY == lastCellY) {
      return;
    }
    
    // Check if this cell has already been populated in this drag
    String cellKey = getCellKey(cellX, cellY);
    if (!populatedCells.contains(cellKey)) {
      // Place an object at this cell's world coordinates
      double cellWorldX = cellX * cellSize;
      double cellWorldY = cellY * cellSize;
      
      LOG.debug("Drag placement at cell ({}, {}), world ({}, {}) with prefab: '{}', group: '{}'", 
          cellX, cellY, cellWorldX, cellWorldY, selectedPrefab.type(), selectedPrefab.group());
      
      // Place the prefab at this location, ensuring group information is preserved
      interactObjectAt(cellWorldX, cellWorldY);
      
      // The controller selects the new object immediately after placement
      UUID newId = getEditorController().getCurrentSelectedObjectId();
      if (newId != null) {
        placedObjectIds.add(newId);
        LOG.debug("Added object with ID: {} to tracked objects", newId);
      }
      
      // Mark this cell as populated
      populatedCells.add(cellKey);
    }
    
    lastCellX = cellX;
    lastCellY = cellY;
  }
  
  /**
   * Ends the current drag operation, clearing any tracking data.
   * Also ensures all placed objects have their images properly loaded.
   */
  public void endDrag() {
    LOG.debug("Ending drag operation, placed {} objects in {} cells", 
              placedObjectIds.size(), populatedCells.size());
    
    // Debug the objects that were placed during this drag operation
    if (!placedObjectIds.isEmpty()) {
        LOG.debug("Objects placed during drag:");
        for (UUID id : placedObjectIds) {
            EditorObject obj = getEditorController().getEditorObject(id);
            if (obj != null && obj.getIdentityData() != null) {
                LOG.debug("  Object ID: {}, Type: '{}', Name: '{}'", 
                    id, obj.getIdentityData().getType(), obj.getIdentityData().getName());
            }
        }
    
        // Force a refresh of all object images
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            // Ignore interruption
        }
      
        // Refresh the display to ensure all objects are properly shown
        getEditorView().refreshDisplay();
    }
    
    isDragging = false;
    populatedCells.clear();
    placedObjectIds.clear();
    lastCellX = Integer.MIN_VALUE;
    lastCellY = Integer.MIN_VALUE;
  }
  
  /**
   * Gets a unique key string for a cell coordinate pair.
   * 
   * @param cellX X-coordinate of the cell.
   * @param cellY Y-coordinate of the cell.
   * @return A string key in the format "x,y".
   */
  private String getCellKey(int cellX, int cellY) {
    return cellX + "," + cellY;
  }
} 