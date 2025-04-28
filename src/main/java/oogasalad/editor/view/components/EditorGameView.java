package oogasalad.editor.view.components;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.view.EditorViewListener;
import oogasalad.editor.view.tools.ObjectInteractionTool;
import oogasalad.fileparser.records.BlueprintData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Displays a grid where visual game elements are added/updated. Implements EditorViewListener to
 * react to model changes notified by the controller. Delegates drawing, event handling, and image
 * management to helper classes.
 *
 * @author Tatum McKinnis
 */
public class EditorGameView extends Pane implements EditorViewListener {

  private static final Logger LOG = LogManager.getLogger(EditorGameView.class);
  private static final String IDENTIFIERS_PROPERTIES_PATH = "/oogasalad/config/editor/resources/editor_game_view_identifiers.properties";

  private final Canvas gridCanvas;
  private final Canvas objectCanvas;
  private final GraphicsContext gridGraphicsContext;
  private final GraphicsContext objectGraphicsContext;

  private final EditorController editorController;
  private final PrefabPalettePane prefabPalettePane;
  private final ResourceBundle uiBundle;
  private final Properties identifierProps;

  private final List<UUID> displayedObjectIds = new ArrayList<>();
  private UUID selectedObjectId;
  private ObjectInteractionTool currentTool;


  private int cellSize;
  private double cornerCameraX;
  private double cornerCameraY;
  private double centerCameraX;
  private double centerCameraY;
  private double zoomScale;
  private boolean snapToGrid = true;
  private boolean drawHitboxes = true;


  private Color gridBackgroundColor;
  private Color gridLineColor;
  private Color gridHorizonColor;
  private double gridLineWidth;
  private double gridHorizonWidth;
  private double zoomSpeed;
  private double minZoom;
  private double panSpeed;
  private int gridMinBound;
  private int gridMaxBound;


  private final EditorGameViewImageManager imageManager;
  private final EditorGameViewEventHandler eventHandler;
  private final EditorGameViewDrawer drawer;


  /**
   * Creates a new editor game view.
   *
   * @param cellSize          Size of each grid cell in pixels.
   * @param initialZoomScale  The initial zoom level.
   * @param editorController  Controller for handling actions and state changes.
   * @param prefabPalettePane The palette pane for accessing selected prefabs.
   * @param uiBundle          ResourceBundle for localized UI text (errors).
   * @throws IllegalArgumentException if controller, palette, or bundle is null or
   * dimensions/cellSize are non-positive.
   * @throws RuntimeException         if identifier properties cannot be loaded.
   */
  public EditorGameView(int cellSize, double initialZoomScale, EditorController editorController,
      PrefabPalettePane prefabPalettePane, ResourceBundle uiBundle) {

    this.editorController = Objects.requireNonNull(editorController,
        "EditorController cannot be null.");
    this.prefabPalettePane = Objects.requireNonNull(prefabPalettePane,
        "PrefabPalettePane cannot be null.");
    this.uiBundle = Objects.requireNonNull(uiBundle, "UI Bundle cannot be null.");
    this.identifierProps = loadIdentifierProperties();

    if (cellSize <= 0) {
      String errorMsg = String.format(uiBundle.getString(getId("key.errorInvalidDimensions")),
          cellSize);
      LOG.error(errorMsg);
      throw new IllegalArgumentException(errorMsg);
    }

    this.cellSize = cellSize;
    this.centerCameraX = 0;
    this.centerCameraY = 0;
    this.zoomScale = initialZoomScale;

    this.gridCanvas = new Canvas();
    this.objectCanvas = new Canvas();
    this.gridGraphicsContext = gridCanvas.getGraphicsContext2D();
    this.objectGraphicsContext = objectCanvas.getGraphicsContext2D();

    this.imageManager = new EditorGameViewImageManager(this, editorController, LOG);
    this.drawer = new EditorGameViewDrawer(this, editorController, imageManager, LOG);
    this.eventHandler = new EditorGameViewEventHandler(this, editorController, prefabPalettePane,
        drawer, LOG);

    loadConfigurableValues();

    this.setId(getId("id.view"));
    getChildren().addAll(gridCanvas, objectCanvas);

    bindCanvasProperties();
    addCanvasListeners();

    initializeView();
    LOG.info("EditorGameView initialized with cell size {}", cellSize);
  }


  /**
   * Initializes the view components, sets up event handlers, zoom, panning,
   * and performs the initial grid drawing.
   */
  private void initializeView() {
    eventHandler.setupEventHandlers();
    eventHandler.setupZoom();
    eventHandler.setupPanning();
    drawer.drawGrid();
  }

  /**
   * Binds the width and height properties of the grid and object canvases
   * to the width and height properties of this Pane. This ensures the canvases
   * resize automatically when the Pane resizes.
   */
  private void bindCanvasProperties() {
    gridCanvas.widthProperty().bind(widthProperty());
    gridCanvas.heightProperty().bind(heightProperty());
    objectCanvas.widthProperty().bind(widthProperty());
    objectCanvas.heightProperty().bind(heightProperty());
  }

  /**
   * Adds listeners to the canvas width and height properties. When the dimensions change,
   * the grid or objects are redrawn accordingly.
   */
  private void addCanvasListeners() {
    gridCanvas.widthProperty().addListener((obs, oldVal, newVal) -> drawer.drawGrid());
    gridCanvas.heightProperty().addListener((obs, oldVal, newVal) -> drawer.drawGrid());
    objectCanvas.widthProperty().addListener((obs, oldVal, newVal) -> drawer.redrawObjects());
    objectCanvas.heightProperty().addListener((obs, oldVal, newVal) -> drawer.redrawObjects());
  }

  /**
   * Loads configurable values (colors, line widths, zoom/pan speeds, grid bounds)
   * from the identifier properties file. Logs and throws a RuntimeException if any
   * essential value fails to load or parse.
   *
   * @throws RuntimeException if loading or parsing configuration values fails.
   */
  private void loadConfigurableValues() {
    try {
      gridBackgroundColor = Color.web(getId("color.grid.background"));
      gridLineColor = Color.web(getId("color.grid.line"));
      gridHorizonColor = Color.web(getId("color.grid.horizon"));
      gridLineWidth = Double.parseDouble(getId("width.grid.line"));
      gridHorizonWidth = Double.parseDouble(getId("width.grid.horizon"));
      zoomSpeed = Double.parseDouble(getId("zoom.speed"));
      minZoom = Double.parseDouble(getId("zoom.min"));
      panSpeed = Double.parseDouble(getId("pan.speed"));
      gridMinBound = Integer.parseInt(getId("grid.min.bound"));
      gridMaxBound = Integer.parseInt(getId("grid.max.bound"));
      drawer.loadConfigurableValues(identifierProps);
    } catch (Exception e) {
      LOG.fatal(
          "Failed to load or parse one or more configuration values from identifier properties.",
          e);
      throw new RuntimeException("Failed to load essential view configuration.", e);
    }
  }

  /**
   * Loads the identifier properties file specified by {@code IDENTIFIERS_PROPERTIES_PATH}.
   * This file contains keys for UI elements, configuration values, and error messages.
   *
   * @return The loaded Properties object.
   * @throws RuntimeException if the properties file cannot be found or loaded.
   */
  private Properties loadIdentifierProperties() {
    Properties props = new Properties();
    try (InputStream input = EditorGameView.class.getResourceAsStream(
        IDENTIFIERS_PROPERTIES_PATH)) {
      if (input == null) {
        String errorMsg = "CRITICAL: Unable to find identifiers properties file: "
            + IDENTIFIERS_PROPERTIES_PATH;
        LOG.error(errorMsg);
        throw new RuntimeException(errorMsg);
      }
      props.load(input);
    } catch (IOException ex) {
      String errorMsg = "CRITICAL: Error loading identifiers properties file: "
          + IDENTIFIERS_PROPERTIES_PATH;
      LOG.error(errorMsg, ex);
      throw new RuntimeException(errorMsg, ex);
    }
    return props;
  }


  /**
   * Updates the currently active interaction tool used for mouse events.
   *
   * @param tool The new ObjectInteractionTool to use, or null to disable tool interaction.
   */
  public void updateCurrentTool(ObjectInteractionTool tool) {
    this.currentTool = tool;
    LOG.info("Current interaction tool set to: {}",
        (tool != null) ? tool.getClass().getSimpleName() : "None");
  }

  /**
   * Retrieves the currently selected prefab data from the associated PrefabPalettePane.
   *
   * @return The BlueprintData of the selected prefab, or null if none is selected.
   */
  public BlueprintData getSelectedPrefab() {
    return prefabPalettePane.getSelectedPrefab();
  }

  /**
   * Forces a complete redraw of all objects currently displayed on the object canvas.
   * Useful after bulk updates or changes that might not trigger individual redraws.
   */
  public void refreshDisplay() {
    drawer.redrawObjects();
  }

  /**
   * Removes all objects from the display, clears the list of displayed IDs,
   * clears the image cache, and redraws the empty object canvas.
   */
  public void removeAllObjects() {
    displayedObjectIds.clear();
    imageManager.clearCache();
    drawer.redrawObjects();
  }


  /**
   * Called when a new game object is added to the model. Adds the object's ID
   * to the display list, preloads its image, and triggers a redraw.
   * Ensures execution on the JavaFX Application Thread.
   *
   * @param objectId The UUID of the object that was added.
   */
  @Override
  public void onObjectAdded(UUID objectId) {
    Platform.runLater(() -> {
      LOG.trace("EditorGameView received: onObjectAdded {}", objectId);
      if (!displayedObjectIds.contains(objectId)) {
        displayedObjectIds.add(objectId);
      }
      imageManager.preloadObjectImage(objectId);
      drawer.redrawObjects();
    });
  }

  /**
   * Called when a game object is removed from the model. Removes the object's ID
   * from the display list, removes its image from the cache, deselects it if it was
   * selected, and triggers a redraw.
   * Ensures execution on the JavaFX Application Thread.
   *
   * @param objectId The UUID of the object that was removed.
   */
  @Override
  public void onObjectRemoved(UUID objectId) {
    Platform.runLater(() -> {
      LOG.trace("EditorGameView received: onObjectRemoved {}", objectId);
      if (displayedObjectIds.remove(objectId)) {
        imageManager.removeImage(objectId);
      }

      if (Objects.equals(selectedObjectId, objectId)) {
        selectedObjectId = null;

      }
      drawer.redrawObjects();
    });
  }

  /**
   * Called when a game object's properties (position, size, image) are updated in the model.
   * Preloads the potentially updated image and triggers a redraw if the object is currently displayed.
   * Ensures execution on the JavaFX Application Thread.
   *
   * @param objectId The UUID of the object that was updated.
   */
  @Override
  public void onObjectUpdated(UUID objectId) {
    Platform.runLater(() -> {
      LOG.trace("EditorGameView received: onObjectUpdated {}", objectId);
      if (displayedObjectIds.contains(objectId)) {

        imageManager.preloadObjectImage(objectId);
        drawer.redrawObjects();
      }
    });
  }

  /**
   * Called when the selected object changes in the model. Updates the internal
   * selected object ID and triggers a redraw to potentially highlight the new selection.
   * Ensures execution on the JavaFX Application Thread.
   *
   * @param selectedObjectId The UUID of the newly selected object, or null if none is selected.
   */
  @Override
  public void onSelectionChanged(UUID selectedObjectId) {
    Platform.runLater(() -> {
      LOG.trace("EditorGameView received: onSelectionChanged {}", selectedObjectId);
      if (!Objects.equals(this.selectedObjectId, selectedObjectId)) {
        this.selectedObjectId = selectedObjectId;
        drawer.redrawObjects();
      }
    });
  }

  /**
   * Called when dynamic game variables (potentially affecting object appearance or behavior)
   * change in the model. Currently, no direct action is taken in the view.
   * This could be used in the future to trigger redraws if variables affect rendering.
   */
  @Override
  public void onDynamicVariablesChanged() {
    LOG.trace("EditorGameView received: onDynamicVariablesChanged (no direct action).");

  }

  /**
   * Called when an error occurs in the model or controller logic. Logs the error message.
   * Consider displaying the error to the user via an alert or status bar.
   *
   * @param errorMessage The description of the error that occurred.
   */
  @Override
  public void onErrorOccurred(String errorMessage) {
    LOG.warn("EditorGameView received: onErrorOccurred: {}", errorMessage);

  }

  /**
   * Called when the list of available prefabs changes in the model.
   * Currently, no direct action is taken in this view, as prefab changes are handled
   * by the {@link PrefabPalettePane}. Logs the event for debugging.
   */
  @Override
  public void onPrefabsChanged() {
    LOG.debug("EditorGameView notified of prefab changes (no direct action taken).");
  }

  /**
   * Called when the underlying sprite template definitions change. This requires clearing
   * the image cache and reloading images for all displayed objects, followed by a redraw,
   * as existing images might be outdated.
   */
  @Override
  public void onSpriteTemplateChanged() {
    LOG.debug("EditorGameView notified of sprite template changes.");

    imageManager.clearCache();

    displayedObjectIds.forEach(imageManager::preloadObjectImage);
    drawer.redrawObjects();
  }

  /**
   * Enables or disables the snap-to-grid feature for object placement and movement.
   *
   * @param doSnap True to enable snapping, false to disable.
   */
  @Override
  public void setSnapToGrid(boolean doSnap) {
    this.snapToGrid = doSnap;
    LOG.debug("Snap to grid set to: {}", doSnap);

  }

  /**
   * Sets the size of the grid cells in world coordinates. Redraws both the grid
   * and the objects to reflect the new cell size. Ensures the cell size is positive.
   *
   * @param cellSize The new size for grid cells (must be positive).
   */
  @Override
  public void setCellSize(int cellSize) {
    if (cellSize > 0) {
      this.cellSize = cellSize;

      drawer.redrawObjects();
      drawer.drawGrid();
      LOG.debug("Cell size set to: {}", cellSize);
    } else {
      LOG.warn("Attempted to set invalid cell size: {}", cellSize);

    }
  }


  /**
   * Updates the camera's top-left corner coordinates (cornerCameraX, cornerCameraY)
   * based on the current center coordinates (centerCameraX, centerCameraY),
   * canvas dimensions, and zoom scale. This is necessary for converting between
   * screen and world coordinates and for determining the visible world area.
   */
  public void updateCameraCoordinates() {
    double canvasWidth = gridCanvas.getWidth();
    double canvasHeight = gridCanvas.getHeight();

    double viewWidthWorld = canvasWidth / zoomScale;
    double viewHeightWorld = canvasHeight / zoomScale;

    cornerCameraX = centerCameraX - (viewWidthWorld / 2.0);
    cornerCameraY = centerCameraY - (viewHeightWorld / 2.0);
  }

  /**
   * Converts screen coordinates (e.g., mouse position relative to the canvas)
   * to world coordinates within the game view, taking into account camera position (pan)
   * and zoom level. Optionally snaps the resulting world coordinates to the nearest
   * grid intersection if snap-to-grid is enabled.
   *
   * @param screenX The x-coordinate on the screen (relative to the canvas top-left).
   * @param screenY The y-coordinate on the screen (relative to the canvas top-left).
   * @return A double array containing the corresponding [worldX, worldY].
   */
  public double[] screenToWorld(double screenX, double screenY) {
    updateCameraCoordinates();

    double worldXOffset = screenX / zoomScale;
    double worldYOffset = screenY / zoomScale;

    double worldX = cornerCameraX + worldXOffset;
    double worldY = cornerCameraY + worldYOffset;


    if (snapToGrid && cellSize > 0) {
      worldX = Math.floor(worldX / cellSize) * cellSize;
      worldY = Math.floor(worldY / cellSize) * cellSize;


    }
    return new double[]{worldX, worldY};
  }


  /**
   * Retrieves a value from the loaded identifier properties file using the given key.
   * Used for accessing UI element IDs, configuration strings, and potentially localized text keys.
   * Throws a RuntimeException if the key is not found or the value is empty, as these
   * are considered essential for the view's operation.
   *
   * @param key The key for the desired property value.
   * @return The non-empty string value associated with the key.
   * @throws RuntimeException if the key is missing or the value is empty/null.
   */
  public String getId(String key) {
    String value = identifierProps.getProperty(key);
    if (value == null || value.trim().isEmpty()) {
      LOG.error("Missing identifier in properties file for key: {}", key);

      throw new RuntimeException("Missing identifier in properties file for key: " + key);
    }
    return value;
  }

  // --- Getters and Setters ---

  /**
   * Gets the currently active object interaction tool.
   *
   * @return The current ObjectInteractionTool, or null if no tool is active.
   */
  public ObjectInteractionTool getCurrentTool() {
    return currentTool;
  }

  /**
   * Gets the current zoom scale of the view. A scale of 1.0 means no zoom,
   * greater than 1.0 means zoomed in, less than 1.0 means zoomed out.
   *
   * @return The current zoom scale.
   */
  public double getZoomScale() {
    return zoomScale;
  }

  /**
   * Sets the zoom scale of the view. Should be validated against minZoom.
   * Consider moving zoom logic into a dedicated method or the event handler.
   *
   * @param zoomScale The new zoom scale (should be > 0, ideally >= minZoom).
   */
  public void setZoomScale(double zoomScale) {

    if (zoomScale > 0) {
      this.zoomScale = zoomScale;


    } else {
      LOG.warn("Attempted to set invalid zoom scale: {}", zoomScale);
    }
  }

  /**
   * Gets the x-coordinate of the center of the camera's view in world coordinates.
   *
   * @return The center x-coordinate of the camera.
   */
  public double getCenterCameraX() {
    return centerCameraX;
  }

  /**
   * Sets the x-coordinate of the center of the camera's view in world coordinates.
   * Used for panning.
   *
   * @param centerCameraX The new center x-coordinate.
   */
  public void setCenterCameraX(double centerCameraX) {
    this.centerCameraX = centerCameraX;


  }

  /**
   * Gets the y-coordinate of the center of the camera's view in world coordinates.
   *
   * @return The center y-coordinate of the camera.
   */
  public double getCenterCameraY() {
    return centerCameraY;
  }

  /**
   * Sets the y-coordinate of the center of the camera's view in world coordinates.
   * Used for panning.
   *
   * @param centerCameraY The new center y-coordinate.
   */
  public void setCenterCameraY(double centerCameraY) {
    this.centerCameraY = centerCameraY;


  }

  /**
   * Gets the x-coordinate of the top-left corner of the camera's view in world coordinates.
   * Ensures camera coordinates are updated before returning the value.
   *
   * @return The top-left x-coordinate of the camera view.
   */
  public double getCornerCameraX() {
    updateCameraCoordinates();
    return cornerCameraX;
  }

  /**
   * Gets the y-coordinate of the top-left corner of the camera's view in world coordinates.
   * Ensures camera coordinates are updated before returning the value.
   *
   * @return The top-left y-coordinate of the camera view.
   */
  public double getCornerCameraY() {
    updateCameraCoordinates();
    return cornerCameraY;
  }

  /**
   * Gets the current size of the grid cells in world coordinates.
   *
   * @return The current cell size (should be positive).
   */
  public int getCellSize() {
    return cellSize;
  }

  /**
   * Gets the current width of the grid canvas (and the pane) in pixels.
   *
   * @return The width of the grid canvas in pixels.
   */
  public double getGridWidth() {
    return gridCanvas.getWidth();
  }

  /**
   * Gets the current height of the grid canvas (and the pane) in pixels.
   *
   * @return The height of the grid canvas in pixels.
   */
  public double getGridHeight() {
    return gridCanvas.getHeight();
  }

  /**
   * Gets the GraphicsContext associated with the grid canvas, used for drawing the grid lines
   * and background.
   *
   * @return The GraphicsContext for the grid canvas.
   */
  public GraphicsContext getGridGraphicsContext() {
    return gridGraphicsContext;
  }

  /**
   * Gets the GraphicsContext associated with the object canvas, used for drawing game objects,
   * selection highlights, and potentially other overlays.
   *
   * @return The GraphicsContext for the object canvas.
   */
  public GraphicsContext getObjectGraphicsContext() {
    return objectGraphicsContext;
  }

  /**
   * Gets the list of UUIDs of the game objects currently being tracked for display in the view.
   * Note: Returns the internal modifiable list. Consider returning an unmodifiable view
   * ({@code Collections.unmodifiableList(displayedObjectIds)}) if external modification is undesirable.
   *
   * @return The list of displayed object UUIDs.
   */
  public List<UUID> getDisplayedObjectIds() {

    return displayedObjectIds;
  }

  /**
   * Gets the UUID of the currently selected game object.
   *
   * @return The UUID of the selected object, or null if no object is selected.
   */
  public UUID getSelectedObjectId() {
    return selectedObjectId;
  }

  /**
   * Checks if drawing hitboxes is currently enabled.
   * Note: The variable {@code drawHitboxes} is initialized to true but currently has no mechanism
   * to be changed after initialization. Consider adding a setter or configuration option
   * if this should be dynamically controllable.
   *
   * @return True if hitbox drawing is enabled, false otherwise.
   */
  public boolean isDrawHitboxesEnabled() {
    return drawHitboxes;
  }

  /**
   * Gets the ResourceBundle used for UI text localization (e.g., error messages).
   *
   * @return The ResourceBundle instance.
   */
  public ResourceBundle getUiBundle() {
    return uiBundle;
  }

  /**
   * Gets the background color used for the grid canvas.
   *
   * @return The grid background color.
   */
  public Color getGridBackgroundColor() {
    return gridBackgroundColor;
  }

  /**
   * Gets the color used for drawing standard grid lines.
   *
   * @return The grid line color.
   */
  public Color getGridLineColor() {
    return gridLineColor;
  }

  /**
   * Gets the color used for drawing the horizon lines (origin axes, x=0 and y=0) of the grid.
   *
   * @return The grid horizon line color.
   */
  public Color getGridHorizonColor() {
    return gridHorizonColor;
  }

  /**
   * Gets the line width used for drawing standard grid lines in pixels.
   *
   * @return The grid line width.
   */
  public double getGridLineWidth() {
    return gridLineWidth;
  }

  /**
   * Gets the line width used for drawing the horizon lines (origin axes) of the grid in pixels.
   *
   * @return The grid horizon line width.
   */
  public double getGridHorizonWidth() {
    return gridHorizonWidth;
  }

  /**
   * Gets the speed factor used for zooming via scroll events. Determines how much
   * the zoom scale changes per scroll unit.
   *
   * @return The zoom speed factor.
   */
  public double getZoomSpeed() {
    return zoomSpeed;
  }

  /**
   * Gets the minimum allowed zoom scale (e.g., prevents zooming out infinitely).
   *
   * @return The minimum zoom scale.
   */
  public double getMinZoom() {
    return minZoom;
  }

  /**
   * Gets the speed factor used for panning the view via drag events. Determines how much
   * the camera center moves per pixel dragged.
   *
   * @return The pan speed factor.
   */
  public double getPanSpeed() {
    return panSpeed;
  }

  /**
   * Gets the minimum boundary coordinate (in grid cells) for grid drawing.
   * Used to limit the extent of grid line drawing for performance.
   *
   * @return The minimum grid boundary in cell units.
   */
  public int getGridMinBound() {
    return gridMinBound;
  }

  /**
   * Gets the maximum boundary coordinate (in grid cells) for grid drawing.
   * Used to limit the extent of grid line drawing for performance.
   *
   * @return The maximum grid boundary in cell units.
   */
  public int getGridMaxBound() {
    return gridMaxBound;
  }

  /**
   * Gets the Canvas node used specifically for drawing game objects and related overlays.
   *
   * @return The object canvas node.
   */
  public Canvas getObjectCanvas() {
    return objectCanvas;
  }

  /**
   * Checks if the snap-to-grid feature is currently enabled.
   *
   * @return True if snap-to-grid is enabled, false otherwise.
   */
  public boolean isSnapToGridEnabled() {
    return snapToGrid;
  }
}