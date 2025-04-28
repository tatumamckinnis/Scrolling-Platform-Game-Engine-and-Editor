package oogasalad.editor.view.components;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.view.tools.DragPrefabPlacementTool;
import oogasalad.editor.view.tools.ObjectInteractionTool;
import oogasalad.fileparser.records.BlueprintData;
import org.apache.logging.log4j.Logger;

/**
 * Handles event setup and processing for the EditorGameView.
 */
class EditorGameViewEventHandler {

  private final EditorGameView view;
  private final EditorController controller;
  private final PrefabPalettePane prefabPalette;
  private final EditorGameViewDrawer drawer;
  private final Logger log;

  private AnimationTimer panTimer;
  private double panVelocityX = 0;
  private double panVelocityY = 0;

  EditorGameViewEventHandler(EditorGameView view, EditorController controller,
      PrefabPalettePane prefabPalette, EditorGameViewDrawer drawer, Logger log) {
    this.view = view;
    this.controller = controller;
    this.prefabPalette = prefabPalette;
    this.drawer = drawer;
    this.log = log;
  }

  void setupEventHandlers() {
    Canvas objectCanvas = view.getObjectCanvas();
    objectCanvas.setOnMouseClicked(this::handleGridClick);
    objectCanvas.setOnMousePressed(this::handleGridMousePressed);
    objectCanvas.setOnMouseDragged(this::handleGridMouseDragged);
    objectCanvas.setOnMouseReleased(this::handleGridMouseReleased);
    setupDragAndDropHandlers(objectCanvas);
  }

  private void setupDragAndDropHandlers(Canvas objectCanvas) {
    objectCanvas.setOnDragOver(event -> {
      Dragboard db = event.getDragboard();
      if (db.hasContent(PrefabPalettePane.PREFAB_BLUEPRINT_ID)) {
        event.acceptTransferModes(TransferMode.COPY);
        log.trace("Drag over accepted for prefab ID");
      }
      event.consume();
    });

    objectCanvas.setOnDragDropped(this::handleDragDropped);
  }

  private void handleDragDropped(javafx.scene.input.DragEvent event) {
    Dragboard db = event.getDragboard();
    boolean success = false;
    if (db.hasContent(PrefabPalettePane.PREFAB_BLUEPRINT_ID)) {
      log.debug("Prefab dropped onto game view");
      success = processPrefabDrop(db, event.getX(), event.getY());
    }
    event.setDropCompleted(success);
    event.consume();
  }

  private boolean processPrefabDrop(Dragboard db, double screenX, double screenY) {
    try {
      String blueprintIdStr = (String) db.getContent(PrefabPalettePane.PREFAB_BLUEPRINT_ID);
      int blueprintId = Integer.parseInt(blueprintIdStr);
      BlueprintData prefabData = prefabPalette.getPrefabById(blueprintId);

      if (prefabData != null) {
        double[] worldCoords = view.screenToWorld(screenX, screenY);
        log.info("Requesting placement for prefab ID {} ({}) at world coords ({}, {})",
            blueprintId, prefabData.type(), worldCoords[0], worldCoords[1]);
        controller.requestPrefabPlacement(prefabData, worldCoords[0], worldCoords[1]);
        return true;
      } else {
        log.error("Dropped prefab ID {} not found in palette's loaded prefabs.", blueprintId);
        controller.notifyErrorOccurred("Could not find data for dropped prefab.");
        return false;
      }
    } catch (NumberFormatException e) {
      log.error("Failed to parse blueprint ID from dragboard: {}",
          db.getContent(PrefabPalettePane.PREFAB_BLUEPRINT_ID), e);
      controller.notifyErrorOccurred("Invalid data format on drop.");
      return false;
    } catch (Exception e) {
      log.error("Error handling prefab drop: {}", e.getMessage(), e);
      controller.notifyErrorOccurred("Error placing dropped prefab: " + e.getMessage());
      return false;
    }
  }

  void setupZoom() {
    view.setOnScroll(this::handleZoomScrollEvent);
  }

  private void handleZoomScrollEvent(ScrollEvent event) {
    handleZoomScroll(event.getDeltaY());
    event.consume();
  }

  private void handleZoomScroll(double deltaY) {
    double currentZoom = view.getZoomScale();
    double newZoom = calculateNewZoomScale(deltaY, currentZoom);
    view.setZoomScale(newZoom);
    view.updateCameraCoordinates();
    drawer.drawGrid();
    drawer.redrawObjects();
  }

  private double calculateNewZoomScale(double deltaY, double currentZoomScale) {
    double zoomFactor = Math.pow(1 + view.getZoomSpeed(), -deltaY);
    double newZoomScale = currentZoomScale * zoomFactor;
    newZoomScale = Math.max(view.getMinZoom(), newZoomScale);

    log.trace("Zoom updated: delta={}, factor={}, oldScale={}, newScale={}", deltaY, zoomFactor,
        currentZoomScale, newZoomScale);
    return newZoomScale;
  }


  void setupPanning() {
    view.setFocusTraversable(true);
    view.setOnKeyPressed(this::handlePanningKeyPressed);
    view.setOnKeyReleased(this::handlePanningKeyReleased);
    startPanTimer();
  }

  private void handlePanningKeyPressed(KeyEvent event) {
    boolean consumed = updatePanVelocity(event.getCode(), true);
    if (consumed) {
      event.consume();
    }
  }

  private void handlePanningKeyReleased(KeyEvent event) {
    boolean consumed = updatePanVelocity(event.getCode(), false);
    if (consumed) {
      event.consume();
    }
  }

  private boolean updatePanVelocity(KeyCode code, boolean isPressed) {
    double speed = isPressed ? view.getPanSpeed() : 0;
    double oppositeSpeed = isPressed ? -view.getPanSpeed() : 0;

    switch (code) {
      case A -> {
        panVelocityX = oppositeSpeed;
        return true;
      }
      case D -> {
        panVelocityX = speed;
        return true;
      }
      case W -> {
        panVelocityY = oppositeSpeed;
        return true;
      }
      case S -> {
        panVelocityY = speed;
        return true;
      }
      default -> {
        return false;
      }
    }
  }


  private void startPanTimer() {
    panTimer = new AnimationTimer() {
      private long lastUpdate = -1;

      @Override
      public void handle(long now) {
        if (panVelocityX == 0 && panVelocityY == 0) {
          lastUpdate = -1;
          return;
        }
        if (lastUpdate < 0) {
          lastUpdate = now;
          return;
        }

        double deltaSeconds = (now - lastUpdate) / 1_000_000_000.0;
        lastUpdate = now;

        updateCameraPosition(deltaSeconds);
        drawer.drawGrid();
        drawer.redrawObjects();
      }
    };
    panTimer.start();
  }

  private void updateCameraPosition(double deltaSeconds) {
    double currentZoom = view.getZoomScale();
    double deltaX = (panVelocityX / currentZoom) * deltaSeconds;
    double deltaY = (panVelocityY / currentZoom) * deltaSeconds;

    view.setCenterCameraX(view.getCenterCameraX() + deltaX);
    view.setCenterCameraY(view.getCenterCameraY() + deltaY);
    view.updateCameraCoordinates();
  }


  private void handleGridClick(MouseEvent event) {
    view.requestFocus();
    if (view.getCurrentTool() != null) {
      double[] worldCoords = view.screenToWorld(event.getX(), event.getY());
      log.debug("Click at screen=({},{}) => world=({},{}) (Snapped: {})",
          event.getX(), event.getY(), worldCoords[0], worldCoords[1], view.isSnapToGridEnabled());
      log.info("Delegating click to tool: {}", view.getCurrentTool().getClass().getSimpleName());
      view.getCurrentTool().interactObjectAt((int) worldCoords[0], (int) worldCoords[1]);
    } else {
      log.trace("Grid clicked, but no interaction tool is active.");
    }
  }

  private void handleGridMousePressed(MouseEvent event) {
    view.requestFocus();
    if (!event.isPrimaryButtonDown()) {
      return;
    }
    ObjectInteractionTool tool = view.getCurrentTool();
    if (isDragCapableTool(tool)) {
      double[] worldCoords = view.screenToWorld(event.getX(), event.getY());
      ((DragPrefabPlacementTool) tool).startDrag(worldCoords[0], worldCoords[1]);
    }
  }

  private void handleGridMouseDragged(MouseEvent event) {
    if (!event.isPrimaryButtonDown()) {
      return;
    }
    ObjectInteractionTool tool = view.getCurrentTool();
    if (isDragCapableTool(tool)) {
      double[] worldCoords = view.screenToWorld(event.getX(), event.getY());
      ((DragPrefabPlacementTool) tool).dragTo(worldCoords[0], worldCoords[1]);
    }
  }

  private void handleGridMouseReleased(MouseEvent event) {

    ObjectInteractionTool tool = view.getCurrentTool();
    if (isDragCapableTool(tool)) {
      ((DragPrefabPlacementTool) tool).endDrag();
    }
  }

  private boolean isDragCapableTool(ObjectInteractionTool tool) {
    return tool instanceof DragPrefabPlacementTool;
  }

}
