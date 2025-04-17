package oogasalad.editor.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.model.data.EditorObject;
import oogasalad.editor.model.data.object.sprite.SpriteData;
import oogasalad.editor.view.tools.ObjectInteractionTool;
import oogasalad.fileparser.records.BlueprintData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Displays a grid where visual game elements are added/updated. Implements EditorViewListener to
 * react to model changes notified by the controller. Handles drawing, basic object selection
 * notification, and delegates placement actions. (DESIGN-01, DESIGN-09, DESIGN-20: Observer
 * Pattern)
 *
 * @author Tatum McKinnis
 */
public class EditorGameView extends Pane implements EditorViewListener {

  private static final Logger LOG = LogManager.getLogger(EditorGameView.class);

  private static final Color GRID_BACKGROUND_COLOR = Color.WHITE;
  private static final Color GRID_LINE_COLOR = Color.LIGHTGRAY;
  private static final Color GRID_HORIZON_COLOR = Color.DARKGRAY;
  private static final Color OBJECT_PLACEHOLDER_FILL = Color.LIGHTBLUE;
  private static final Color OBJECT_PLACEHOLDER_TEXT_FILL = Color.BLACK;
  private static final Color SELECTION_BORDER_COLOR = Color.BLUE;
  private static final double GRID_LINE_WIDTH = 1.0;
  private static final double GRID_HORIZON_WIDTH = 2.0;
  private static final double SELECTION_BORDER_WIDTH = 2.0;
  private static final double GRID_HORIZON_RATIO = 0.8;
  private static final int GRID_MIN = -500;
  private static final int GRID_MAX = 500;
  private static final double PLACEHOLDER_FONT_SIZE = 10.0;
  private static final String PLACEHOLDER_FONT_NAME = "System";
  private static final double ZOOM_SPEED = 0.02;
  private static final double MIN_ZOOM = 0.5;
  private static final Color HITBOX_COLOR = new Color(1, 0, 0, 0.2);
  private double panVelocityX = 0;
  private double panVelocityY = 0;
  private AnimationTimer panTimer;
  private final double PAN_SPEED = 200;


  private final Canvas gridCanvas;
  private final Canvas objectCanvas;
  private final GraphicsContext gridGraphicsContext;
  private final GraphicsContext objectGraphicsContext;

  private final int cellSize;
  private final EditorController editorController;
  private final Map<UUID, Image> objectImages = new HashMap<>();
  private final List<UUID> displayedObjectIds = new ArrayList<>();
  private double cornerCameraX;
  private double cornerCameraY;
  private double centerCameraX;
  private double centerCameraY;
  private double zoomScale;
  private ObjectInteractionTool currentTool;
  private UUID selectedObjectId;
  private boolean drawHitboxes = true;
  private boolean snapToGrid = true;
  private PrefabPalettePane prefabPalettePane;



  /**
   * Creates a new editor game view.
   *
   * @param cellSize         Size of each grid cell in pixels.
   * @param editorController Controller for handling actions and state changes.
   * @throws IllegalArgumentException if editorController is null or dimensions/cellSize are
   * non-positive.
   */
  public EditorGameView(int cellSize, double zoomScale, EditorController editorController, PrefabPalettePane prefabPalettePane) { // Add PrefabPalettePane parameter
    if (editorController == null) {
      throw new IllegalArgumentException("EditorController cannot be null.");
    }
    if (cellSize <= 0) {
      LOG.error("Invalid dimensions provided: cellSize={}", cellSize);
      throw new IllegalArgumentException("View dimensions and cell size must be positive.");
    }

    this.prefabPalettePane = Objects.requireNonNull(prefabPalettePane, "PrefabPalettePane cannot be null.");
    this.gridCanvas = new Canvas();
    this.objectCanvas = new Canvas();
    this.gridGraphicsContext = gridCanvas.getGraphicsContext2D();
    this.objectGraphicsContext = objectCanvas.getGraphicsContext2D();

    this.cellSize = cellSize;
    this.centerCameraX = 0;
    this.centerCameraY = 0;
    this.zoomScale = zoomScale;
    this.editorController = editorController;

    this.setId("editor-game-view");
    getChildren().addAll(gridCanvas, objectCanvas);


    gridCanvas.widthProperty().bind(widthProperty());
    gridCanvas.heightProperty().bind(heightProperty());
    objectCanvas.widthProperty().bind(widthProperty());
    objectCanvas.heightProperty().bind(heightProperty());

    gridCanvas.widthProperty().addListener((obs, oldVal, newVal) -> drawGrid());
    gridCanvas.heightProperty().addListener((obs, oldVal, newVal) -> drawGrid());

    objectCanvas.widthProperty().addListener((obs, oldVal, newVal) -> redrawObjects());
    objectCanvas.heightProperty().addListener((obs, oldVal, newVal) -> redrawObjects());

    initializeView();
    LOG.info("EditorGameView initialized with cell size {}", cellSize);
  }


  /**
   * Gets the currently selected prefab.
   * @return The selected prefab.
   */
  public BlueprintData getSelectedPrefab() {
    return prefabPalettePane.getSelectedPrefab();
  }

  @Override
  public void onPrefabsChanged() {
    LOG.debug("EditorGameView notified of prefab changes.");
  }

  /**
   * Initializes the view by drawing the grid and setting up event handlers. This is called
   * internally by the constructor.
   */
  private void initializeView() {
    setupEventHandlers();
    setupZoom();
    setupPanning();
    drawGrid();
  }

  /**
   * Draws the background grid lines on the dedicated grid canvas.
   */
  public void drawGrid() {
    double width = gridCanvas.getWidth();
    double height = gridCanvas.getHeight();
    updateCameraCoordinates();

    gridGraphicsContext.setFill(GRID_BACKGROUND_COLOR);
    gridGraphicsContext.fillRect(0, 0, width, height);

    gridGraphicsContext.save();

    gridGraphicsContext.translate(-cornerCameraX, -cornerCameraY);
    gridGraphicsContext.scale(zoomScale, zoomScale);

    int gridMinPixels = GRID_MIN * cellSize;
    int gridMaxPixels = GRID_MAX * cellSize;
    gridGraphicsContext.setStroke(GRID_LINE_COLOR);

    for (int x = gridMinPixels; x <= gridMaxPixels; x += cellSize) {
      gridGraphicsContext.strokeLine(x, gridMinPixels, x, gridMaxPixels);
    }
    for (int y = gridMinPixels; y <= gridMaxPixels; y += cellSize) {
      gridGraphicsContext.strokeLine(gridMinPixels, y, gridMaxPixels, y);
    }

    gridGraphicsContext.restore();
    LOG.trace("Grid drawn with dynamic width={}, height={}.", width, height);
  }

  /**
   * Sets up mouse event handlers for interaction on the object canvas, primarily for handling
   * clicks related to object placement or selection.
   */
  private void setupEventHandlers() {
    objectCanvas.setOnMouseClicked(this::handleGridClick);
  }

  /**
   * Sets up zoom behavior by adding a scroll listener. Zooming is triggered when the user scrolls
   * while holding the Control key.
   */
  private void setupZoom() {
    this.setOnScroll(event -> {
      if (event.isControlDown()) {
        handleZoomScroll(event.getDeltaY());
        event.consume();
      }
    });
  }

  /**
   * Handles the logic for zooming in or out based on the scroll delta. Updates the zoom scale,
   * camera position, grid, and redraws all objects.
   *
   * @param deltaY the amount of scroll (positive or negative)
   */
  private void handleZoomScroll(double deltaY) {
    zoomScale = calculateNewZoomScale(deltaY, zoomScale);
    updateCameraCoordinates();
    drawGrid();
    redrawObjects();
  }

  /**
   * Calculates the new zoom scale based on the scroll delta and current zoom scale. Applies
   * constraints to avoid zooming beyond set minimum or maximum limits.
   *
   * @param deltaY           the scroll delta indicating zoom direction
   * @param currentZoomScale the current zoom scale before applying scroll
   * @return the adjusted zoom scale after clamping within valid bounds
   */
  private double calculateNewZoomScale(double deltaY, double currentZoomScale) {
    double inverseZoom = 1.0 / currentZoomScale;

    if (deltaY < 0) {
      inverseZoom += ZOOM_SPEED;
    } else {
      inverseZoom -= ZOOM_SPEED;
    }

    double maxInverseZoom = 1.0 / MIN_ZOOM;
    double minInverseZoom = 1.0 / 20.0;

    inverseZoom = Math.max(minInverseZoom, Math.min(inverseZoom, maxInverseZoom));

    return 1.0 / inverseZoom;
  }

  /**
   * Sets up input based event handlers for using WASD keys to pan the grid.
   */
  private void setupPanning() {
    // TODO: this really needs to be broken up into smaller pieces
    this.setFocusTraversable(true);
    this.setOnKeyPressed(event -> {
      switch (event.getCode()) {
        case A:
          panVelocityX = -PAN_SPEED;
          break;
        case D:
          panVelocityX = PAN_SPEED;
          break;
        case W:
          panVelocityY = -PAN_SPEED;
          break;
        case S:
          panVelocityY = PAN_SPEED;
          break;
        default:
          break;
      }
      event.consume();
    });
    this.setOnKeyReleased(event -> {
      switch (event.getCode()) {
        case A:
        case D:
          panVelocityX = 0;
          break;
        case W:
        case S:
          panVelocityY = 0;
          break;
        default:
          break;
      }
      event.consume();
    });
    panTimer = new AnimationTimer() {
      private long lastUpdate = -1;

      @Override
      public void handle(long now) {
        if (lastUpdate < 0) {
          lastUpdate = now;
          return;
        }
        double deltaSeconds = (now - lastUpdate) / 1000000000.0;
        lastUpdate = now;
        centerCameraX += (panVelocityX / zoomScale) * deltaSeconds;
        centerCameraY += (panVelocityY / zoomScale) * deltaSeconds;
        updateCameraCoordinates();
        drawGrid();
        redrawObjects();
      }
    };
    panTimer.start();
  }

  /**
   * Updates the top left camera coordinates based off of changing zoom, central x and y, and width
   * height.
   */
  private void updateCameraCoordinates() {
    double canvasWidth = gridCanvas.getWidth();
    double canvasHeight = gridCanvas.getHeight();

    cornerCameraX = centerCameraX - (canvasWidth * 0.5);
    cornerCameraY = centerCameraY - (canvasHeight * 0.5);
  }

  /**
   * Handles mouse clicks on the object canvas. If a placement tool is active, it delegates the
   * click to the tool. Otherwise, it attempts to select an object at the click location.
   *
   * @param event The MouseEvent associated with the click.
   */
  private void handleGridClick(MouseEvent event) {
    this.requestFocus();
    double screenX = event.getX();
    double screenY = event.getY();

    double worldX = (screenX + cornerCameraX) / zoomScale;
    double worldY = (screenY + cornerCameraY) / zoomScale;

    if (snapToGrid) {
      worldX = ((int) Math.floor(worldX / cellSize)) * cellSize;
      worldY = ((int) Math.floor(worldY / cellSize)) * cellSize;
      LOG.info("{} {}", worldX, worldY);
    }

    LOG.debug("Click at screen=({},{}) => world=({},{})", screenX, screenY, worldX,
        worldY);

    if (currentTool != null) {
      LOG.info("Delegating click to tool: {}", currentTool.getClass().getSimpleName());
      currentTool.interactObjectAt((int) worldX, (int) worldY);
    }
  }

  /**
   * Sets the currently active object placement tool for the view.
   *
   * @param tool The ObjectPlacementTool to activate, or null to deactivate placement.
   */
  public void updateCurrentTool(ObjectInteractionTool tool) {
    this.currentTool = tool;
    LOG.info("Current placement tool set to: {}",
        (tool != null) ? tool.getClass().getSimpleName() : "None");
  }

  /**
   * Initiates a redraw of all objects currently displayed on the object canvas. Ensures the drawing
   * operations occur on the JavaFX Application Thread.
   */
  private void redrawObjects() {
    if (!Platform.isFxApplicationThread()) {
      Platform.runLater(this::redrawObjectsInternal);
    } else {
      redrawObjectsInternal();
    }
  }

  /**
   * Performs the actual drawing logic on the JavaFX Application Thread. Clears the object canvas
   * and then iterates through the `displayedObjectIds`, drawing each object's image or a
   * placeholder if the image isn't available. Also draws a selection indicator around the currently
   * selected object.
   */
  void redrawObjectsInternal() {
    double width = objectCanvas.getWidth();
    double height = objectCanvas.getHeight();
    updateCameraCoordinates();

    objectGraphicsContext.clearRect(0, 0, width, height);
    objectGraphicsContext.save();
    objectGraphicsContext.translate(-cornerCameraX, -cornerCameraY);
    objectGraphicsContext.scale(zoomScale, zoomScale);

    LOG.trace("Object canvas cleared for redraw.");
    List<UUID> idsToDraw = new ArrayList<>(displayedObjectIds);

    for (UUID id : idsToDraw) {
      try {
        redrawSprites(id);
        if (drawHitboxes) {
          redrawHitboxes(id);
        }

      } catch (Exception e) {
        LOG.error("Error drawing object with ID {}: {}", id, e.getMessage(), e);
      }
    }
    objectGraphicsContext.restore();
    LOG.trace("Finished redrawing {} objects.", idsToDraw.size());
  }


  /**
   * Draws a placeholder rectangle with the object's group type displayed inside. Used when the
   * object's image is not available or failed to load.
   *
   * @param g      The GraphicsContext to draw on.
   * @param object The EditorObject for which to draw the placeholder. Used to get type info.
   * @param x      The x-coordinate where the placeholder should be drawn.
   * @param y      The y-coordinate where the placeholder should be drawn.
   */
  private void drawPlaceholder(GraphicsContext g, EditorObject object, double x, double y) {
    g.setFill(OBJECT_PLACEHOLDER_FILL);
    g.fillRect(x, y, cellSize, cellSize);
    g.setFill(OBJECT_PLACEHOLDER_TEXT_FILL);
    g.setFont(Font.font(PLACEHOLDER_FONT_NAME, PLACEHOLDER_FONT_SIZE));
    g.setTextAlign(TextAlignment.CENTER);
    String type = "?";
    if (object != null && object.getIdentityData() != null
        && object.getIdentityData().getType() != null) {
      type = object.getIdentityData().getType();
    }
    g.fillText(type, x + cellSize / 2.0, y + cellSize / 2.0 + PLACEHOLDER_FONT_SIZE / 3.0);
  }

  /**
   * Draws a visual indicator (a border) around the cell occupied by the selected object.
   *
   * @param g The GraphicsContext to draw on.
   * @param x The x-coordinate of the selected object's cell.
   * @param y The y-coordinate of the selected object's cell.
   */
  private void drawSelectionIndicator(GraphicsContext g, double x, double y) {
    g.save();
    g.setStroke(SELECTION_BORDER_COLOR);
    g.setLineWidth(SELECTION_BORDER_WIDTH);
    g.strokeRect(x - SELECTION_BORDER_WIDTH / 2, y - SELECTION_BORDER_WIDTH / 2,
        cellSize + SELECTION_BORDER_WIDTH, cellSize + SELECTION_BORDER_WIDTH);
    g.restore();
  }

  /**
   * Preloads the image associated with the given object ID. If the image path is invalid or loading
   * fails, logs the error and removes the image from cache.
   *
   * @param id the UUID of the object whose image is to be preloaded
   */
  private void preloadObjectImage(UUID id) {
    try {
      String imagePath = getObjectSpritePath(id);
      if (imagePath == null) {
        objectImages.remove(id);
        LOG.trace("No valid sprite path found or object missing for ID {}", id);
        return;
      }

      String url = resolveImagePath(imagePath);
      if (url == null) {
        LOG.error("Could not resolve image resource path: {}", imagePath);
        objectImages.remove(id);
        return;
      }

      loadImageIfNotCached(id, url);

    } catch (Exception e) {
      LOG.error("Failed during image preload process for object ID {}: {}", id, e.getMessage(), e);
      objectImages.remove(id);
      redrawObjects();
    }
  }

  /**
   * Retrieves the sprite path for the given object ID, if available and valid.
   *
   * @param id the UUID of the object
   * @return the sprite path, or null if unavailable or invalid
   */
  private String getObjectSpritePath(UUID id) {
    EditorObject object = editorController.getEditorObject(id);
    if (object == null || object.getSpriteData() == null) {
      return null;
    }
    String path = object.getSpriteData().getSpritePath();
    return (path == null || path.trim().isEmpty()) ? null : path;
  }

  /**
   * Loads the image from the given URL if it is not already cached, or if the cached version is
   * outdated or contains an error.
   *
   * @param id  the UUID of the object
   * @param url the resolved image URL
   */
  private void loadImageIfNotCached(UUID id, String url) {
    Image cachedImage = objectImages.get(id);
    boolean needsLoading = cachedImage == null
        || !Objects.equals(cachedImage.getUrl(), url)
        || cachedImage.isError();

    if (needsLoading) {
      LOG.debug("Loading image for object ID {} from resolved URL: {}", id, url);
      Image newImage = new Image(url, true);
      setupImageListeners(newImage, url);
      objectImages.put(id, newImage);
    } else {
      LOG.trace("Image for {} already cached and valid: {}", id, url);
    }
  }

  /**
   * Sets up listeners for an image to handle loading progress and error events.
   *
   * @param image the image to monitor
   * @param url   the URL the image is being loaded from
   */
  private void setupImageListeners(Image image, String url) {
    image.errorProperty()
        .addListener((obs, oldErr, newErr) -> handleImageError(image, url, newErr));
    image.progressProperty().addListener(
        (obs, oldProgress, newProgress) -> handleImageProgress(image, url, newProgress));
  }

  /**
   * Handles the logic when an image loading error occurs. Logs the error and triggers a redraw to
   * display a placeholder.
   *
   * @param image   the image that failed to load
   * @param url     the source URL of the image
   * @param isError whether an error occurred during loading
   */
  private void handleImageError(Image image, String url, boolean isError) {
    if (isError) {
      String errorMessage =
          (image.getException() != null) ? image.getException().getMessage() : "Unknown error";
      LOG.error("Failed to load image from {}: {}", url, errorMessage);
      redrawObjects();
    }
  }

  /**
   * Handles image load progress updates. When loading completes, it checks for errors and triggers
   * a redraw accordingly.
   *
   * @param image    the image being loaded
   * @param url      the URL the image was loaded from
   * @param progress the loading progress value
   */
  private void handleImageProgress(Image image, String url, Number progress) {
    if (progress != null && progress.doubleValue() >= 1.0) {
      if (!image.isError()) {
        LOG.trace("Image loaded successfully: {}", url);
        redrawObjects();
      } else {
        LOG.error("Error detected after image load completion signal for URL: {}", url);
        redrawObjects();
      }
    }
  }


  /**
   * Resolves the given image path to a usable URL string. Returns the original path if it's an
   * absolute path or full URL; otherwise attempts to locate it as a classpath resource.
   *
   * @param path the image path to resolve
   * @return the resolved URL string, or null if resolution fails
   */
  private String resolveImagePath(String path) {
    if (path == null || path.trim().isEmpty()) {
      return null;
    }

    if (path.matches("^([a-zA-Z]+:.*|/).*")) {
      return path;
    }

    return findResourcePath(path);
  }

  /**
   * Attempts to find a relative path as a resource within the classpath.
   *
   * @param relativePath the relative resource path
   * @return the URL string if found, or null if not found or an error occurs
   */
  private String findResourcePath(String relativePath) {
    try {
      String resourcePath = relativePath.startsWith("/") ? relativePath : "/" + relativePath;
      java.net.URL resourceUrl = getClass().getResource(resourcePath);

      if (resourceUrl != null) {
        return resourceUrl.toExternalForm();
      } else {
        LOG.warn("Resource not found for relative path: {}", resourcePath);
        return null;
      }
    } catch (Exception e) {
      LOG.warn("Could not resolve relative resource path '{}': {}", relativePath, e.getMessage());
      return null;
    }
  }


  /**
   * Redraws the sprite associated with the specified object ID on the canvas.
   * <p>
   * If the object or its sprite data is missing, a warning is logged and nothing is drawn. If the
   * corresponding image is available and fully loaded, it is drawn at the object's sprite
   * coordinates. Otherwise, a placeholder is drawn. If the object is currently selected, a visual
   * selection indicator is also rendered.
   *
   * @param id the unique identifier of the object whose sprite should be redrawn
   */
  private void redrawSprites(UUID id) {
    EditorObject object = editorController.getEditorObject(id);
    if (object == null || object.getSpriteData() == null) {
      LOG.warn("Object ID {} or its SpriteData not found during redraw, cannot draw.", id);
      return;
    }
    SpriteData spriteData = object.getSpriteData();
    double x = spriteData.getX();
    double y = spriteData.getY();
    Image image = objectImages.get(id);

    if (image != null && !image.isError() && image.getProgress() >= 1.0) {
      objectGraphicsContext.drawImage(image, x, y, cellSize, cellSize);
    } else {
      drawPlaceholder(objectGraphicsContext, object, x, y);
    }

    if (id.equals(selectedObjectId)) {
      drawSelectionIndicator(objectGraphicsContext, x, y);
    }
  }

  /**
   * Redraws the hitbox of the object associated with the specified ID.
   * <p>
   * If the object or its hitbox data is missing, a warning is logged and no hitbox is drawn.
   * Otherwise, the hitbox is rendered as a filled rectangle using a predefined color.
   *
   * @param id the unique identifier of the object whose hitbox should be redrawn
   */
  private void redrawHitboxes(UUID id) {
    EditorObject object = editorController.getEditorObject(id);
    if (object == null || object.getHitboxData() == null) {
      LOG.warn("Object ID {} or its HitboxData not found during redraw, cannot draw.", id);
      return;
    }
    int hitboxX = object.getHitboxData().getX();
    int hitboxY = object.getHitboxData().getY();
    int hitboxWidth = object.getHitboxData().getWidth();
    int hitboxHeight = object.getHitboxData().getHeight();

    objectGraphicsContext.setFill(HITBOX_COLOR);
    objectGraphicsContext.fillRect(hitboxX, hitboxY, hitboxWidth, hitboxHeight);
  }

  /**
   * Handles the notification that a new object has been added to the model. Adds the object's ID to
   * the list of displayed objects, preloads its image, and ensures these operations occur on the
   * JavaFX Application Thread. Redrawing is often implicitly triggered by a subsequent selection
   * change.
   *
   * @param objectId The UUID of the newly added object.
   */
  @Override
  public void onObjectAdded(UUID objectId) {
    Platform.runLater(() -> {
      // ---> MAKE SURE THE LOG IS HERE <---
      LOG.debug("EditorGameView received: onObjectAdded {}", objectId);
      if (!displayedObjectIds.contains(objectId)) {
        displayedObjectIds.add(objectId);
        preloadObjectImage(objectId);
      }
      redrawObjects();
    });
  }

  /**
   * Handles the notification that an object has been removed from the model. Removes the object's
   * ID from the list of displayed objects, removes its image from the cache, and triggers a redraw
   * to reflect the removal. Ensures these operations occur on the JavaFX Application Thread.
   *
   * @param objectId The UUID of the removed object.
   */
  @Override
  public void onObjectRemoved(UUID objectId) {
    Platform.runLater(() -> {
      LOG.debug("EditorGameView received: onObjectRemoved {}", objectId);
      if (displayedObjectIds.remove(objectId)) {
        objectImages.remove(objectId);
        redrawObjects();
      }
    });
  }

  /**
   * Handles the notification that an existing object's data has been updated. Preloads the object's
   * image again (in case the sprite path changed) and triggers a redraw to reflect any visual
   * changes (position, sprite, etc.). Ensures these operations occur on the JavaFX Application
   * Thread.
   *
   * @param objectId The UUID of the updated object.
   */
  @Override
  public void onObjectUpdated(UUID objectId) {
    Platform.runLater(() -> {
      LOG.debug("EditorGameView received: onObjectUpdated {}", objectId);
      if (displayedObjectIds.contains(objectId)) {
        preloadObjectImage(objectId);
        redrawObjects();
      }
    });
  }

  /**
   * Handles the notification that the currently selected object has changed. Updates the internal
   * `selectedObjectId` state and triggers a redraw to update the visual selection indicator.
   * Ensures these operations occur on the JavaFX Application Thread.
   *
   * @param selectedObjectId The UUID of the newly selected object, or null if no object is
   *                         selected.
   */
  @Override
  public void onSelectionChanged(UUID selectedObjectId) {
    Platform.runLater(() -> {
      LOG.debug("EditorGameView received: onSelectionChanged {}", selectedObjectId);
      if (!Objects.equals(this.selectedObjectId, selectedObjectId)) {
        this.selectedObjectId = selectedObjectId;
        redrawObjects();
      }
    });
  }

  /**
   * Handles the notification that dynamic variables (like game parameters) have changed. This view
   * typically does not need to react directly to these changes.
   */
  @Override
  public void onDynamicVariablesChanged() {
    LOG.trace("EditorGameView received: onDynamicVariablesChanged (no action)");
  }

  /**
   * Handles the notification that an error occurred elsewhere in the editor. Logs the error
   * message. In a more complex UI, this might update a status bar. Ensures any UI updates happen on
   * the JavaFX Application Thread.
   *
   * @param errorMessage A description of the error that occurred.
   */
  @Override
  public void onErrorOccurred(String errorMessage) {
    LOG.warn("EditorGameView received: onErrorOccurred: {}", errorMessage);
    Platform.runLater(() -> {
    });
  }


  /**
   * Gets the size of each grid cell in pixels.
   *
   * @return The cell size.
   */
  public int getCellSize() {
    return cellSize;
  }

  /**
   * Gets the width of the game view grid area in pixels.
   *
   * @return The grid width.
   */
  public double getGridWidth() {
    return gridCanvas.getWidth();
  }

  /**
   * Gets the height of the game view grid area in pixels.
   *
   * @return The grid height.
   */
  public double getGridHeight() {
    return gridCanvas.getHeight();
  }


}
