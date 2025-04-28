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
 * @author Tatum McKinnis
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

  /**
   * Constructs an EditorGameViewEventHandler.
   *
   * @param view The parent EditorGameView.
   * @param controller The application's editor controller.
   * @param prefabPalette The prefab palette pane for accessing dragged prefab data.
   * @param drawer The drawer responsible for rendering updates after events.
   * @param log The logger instance for logging event handling.
   */
  EditorGameViewEventHandler(EditorGameView view, EditorController controller,
      PrefabPalettePane prefabPalette, EditorGameViewDrawer drawer, Logger log) {
    this.view = view;
    this.controller = controller;
    this.prefabPalette = prefabPalette;
    this.drawer = drawer;
    this.log = log;
  }

  /**
   * Sets up the primary mouse event handlers (click, press, drag, release) and
   * drag-and-drop handlers on the object canvas.
   */
  void setupEventHandlers() {
    Canvas objectCanvas = view.getObjectCanvas();
    objectCanvas.setOnMouseClicked(this::handleGridClick);
    objectCanvas.setOnMousePressed(this::handleGridMousePressed);
    objectCanvas.setOnMouseDragged(this::handleGridMouseDragged);
    objectCanvas.setOnMouseReleased(this::handleGridMouseReleased);
    setupDragAndDropHandlers(objectCanvas);
  }

  /**
   * Configures the necessary event handlers on the target canvas to enable
   * drag-and-drop operations, specifically for prefab placement.
   *
   * @param objectCanvas The canvas to attach drag-and-drop listeners to.
   */
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

  /**
   * Handles the drop event when an item (expected to be a prefab) is dropped onto the canvas.
   * Extracts prefab data from the Dragboard and initiates the placement process.
   *
   * @param event The DragEvent containing information about the drop.
   */
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

  /**
   * Processes the drop of a prefab. Retrieves the blueprint ID from the dragboard,
   * looks up the corresponding BlueprintData from the prefab palette, converts screen
   * coordinates to world coordinates, and requests the controller to place the prefab.
   *
   * @param db The Dragboard containing the prefab blueprint ID.
   * @param screenX The screen x-coordinate where the drop occurred.
   * @param screenY The screen y-coordinate where the drop occurred.
   * @return true if the prefab placement request was successfully initiated, false otherwise.
   */
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

  /**
   * Sets up the scroll event handler on the main view pane for zooming.
   */
  void setupZoom() {
    view.setOnScroll(this::handleZoomScrollEvent);
  }

  /**
   * Handles the raw ScrollEvent triggered by the user. Extracts the vertical scroll
   * delta and passes it to the zoom logic. Consumes the event.
   *
   * @param event The ScrollEvent generated by the user action.
   */
  private void handleZoomScrollEvent(ScrollEvent event) {
    handleZoomScroll(event.getDeltaY());
    event.consume();
  }

  /**
   * Processes a zoom action based on the scroll delta. Calculates the new zoom scale,
   * applies it to the view, updates camera coordinates, and redraws the grid and objects.
   *
   * @param deltaY The vertical scroll delta value from the ScrollEvent.
   */
  private void handleZoomScroll(double deltaY) {
    double currentZoom = view.getZoomScale();
    double newZoom = calculateNewZoomScale(deltaY, currentZoom);
    view.setZoomScale(newZoom);
    view.updateCameraCoordinates();
    drawer.drawGrid();
    drawer.redrawObjects();
  }

  /**
   * Calculates the new zoom scale based on the scroll delta and current scale.
   * Applies the configured zoom speed and ensures the new scale does not go below the minimum zoom level.
   *
   * @param deltaY The scroll delta value.
   * @param currentZoomScale The current zoom scale of the view.
   * @return The calculated new zoom scale, clamped to the minimum zoom level.
   */
  private double calculateNewZoomScale(double deltaY, double currentZoomScale) {
    double zoomFactor = Math.pow(1 + view.getZoomSpeed(), -deltaY);
    double newZoomScale = currentZoomScale * zoomFactor;
    newZoomScale = Math.max(view.getMinZoom(), newZoomScale);

    log.trace("Zoom updated: delta={}, factor={}, oldScale={}, newScale={}", deltaY, zoomFactor,
        currentZoomScale, newZoomScale);
    return newZoomScale;
  }


  /**
   * Sets up keyboard event handlers for panning (WASD keys) and starts the
   * animation timer responsible for continuous panning movement. Makes the view focus traversable.
   */
  void setupPanning() {
    view.setFocusTraversable(true);
    view.setOnKeyPressed(this::handlePanningKeyPressed);
    view.setOnKeyReleased(this::handlePanningKeyReleased);
    startPanTimer();
  }

  /**
   * Handles key pressed events, specifically checking for WASD keys to initiate panning.
   * Updates the pan velocity based on the pressed key.
   *
   * @param event The KeyEvent generated by the key press.
   */
  private void handlePanningKeyPressed(KeyEvent event) {
    boolean consumed = updatePanVelocity(event.getCode(), true);
    if (consumed) {
      event.consume();
    }
  }

  /**
   * Handles key released events, specifically checking for WASD keys to stop panning.
   * Resets the pan velocity component corresponding to the released key.
   *
   * @param event The KeyEvent generated by the key release.
   */
  private void handlePanningKeyReleased(KeyEvent event) {
    boolean consumed = updatePanVelocity(event.getCode(), false);
    if (consumed) {
      event.consume();
    }
  }

  /**
   * Updates the horizontal or vertical pan velocity based on the pressed/released key code.
   *
   * @param code The KeyCode of the event.
   * @param isPressed True if the key was pressed, false if released.
   * @return true if the key code corresponds to a panning key (WASD) and the velocity was updated, false otherwise.
   */
  private boolean updatePanVelocity(KeyCode code, boolean isPressed) {
    double speed = isPressed ? view.getPanSpeed() : 0;
    double oppositeSpeed = isPressed ? -view.getPanSpeed() : 0;

    switch (code) {
      case A:
        panVelocityX = oppositeSpeed;
        return true;
      case D:
        panVelocityX = speed;
        return true;
      case W:
        panVelocityY = oppositeSpeed;
        return true;
      case S:
        panVelocityY = speed;
        return true;
      default:
        return false;
    }
  }


  /**
   * Starts the AnimationTimer that handles continuous camera movement based on the current pan velocity.
   * The timer updates the camera position based on elapsed time and triggers redraws.
   */
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

  /**
   * Updates the camera's center coordinates based on the current pan velocity, elapsed time, and zoom level.
   *
   * @param deltaSeconds The time elapsed since the last update, in seconds.
   */
  private void updateCameraPosition(double deltaSeconds) {
    double currentZoom = view.getZoomScale();
    double deltaX = (panVelocityX / currentZoom) * deltaSeconds;
    double deltaY = (panVelocityY / currentZoom) * deltaSeconds;

    view.setCenterCameraX(view.getCenterCameraX() + deltaX);
    view.setCenterCameraY(view.getCenterCameraY() + deltaY);
    view.updateCameraCoordinates();
  }


  /**
   * Handles mouse click events on the object canvas. Requests focus for the view.
   * If an interaction tool is active, converts screen coordinates to world coordinates
   * and delegates the interaction to the current tool.
   *
   * @param event The MouseEvent generated by the click.
   */
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

  /**
   * Handles mouse pressed events on the object canvas, specifically for the primary button.
   * Requests focus and, if the current tool supports dragging (like DragPrefabPlacementTool),
   * notifies the tool to start a drag operation at the calculated world coordinates.
   *
   * @param event The MouseEvent generated by pressing the mouse button.
   */
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

  /**
   * Handles mouse dragged events on the object canvas while the primary button is pressed.
   * If the current tool supports dragging, notifies the tool about the updated drag position
   * in world coordinates.
   *
   * @param event The MouseEvent generated by dragging the mouse.
   */
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

  /**
   * Handles mouse released events on the object canvas.
   * If the current tool supports dragging, notifies the tool that the drag operation has ended.
   *
   * @param event The MouseEvent generated by releasing the mouse button.
   */
  private void handleGridMouseReleased(MouseEvent event) {
    ObjectInteractionTool tool = view.getCurrentTool();
    if (isDragCapableTool(tool)) {
      ((DragPrefabPlacementTool) tool).endDrag();
    }
  }

  /**
   * Checks if the provided interaction tool is capable of handling drag operations,
   * specifically checking if it's an instance of {@link DragPrefabPlacementTool}.
   *
   * @param tool The ObjectInteractionTool to check.
   * @return true if the tool is an instance of DragPrefabPlacementTool, false otherwise.
   */
  private boolean isDragCapableTool(ObjectInteractionTool tool) {
    return tool instanceof DragPrefabPlacementTool;
  }

}