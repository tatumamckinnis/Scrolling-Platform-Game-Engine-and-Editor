package oogasalad.editor.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.model.data.EditorObject;
import oogasalad.editor.model.data.object.sprite.SpriteData;
import oogasalad.editor.view.tools.ObjectInteractionTool;
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


  private final Canvas gridCanvas;
  private final Canvas objectCanvas;
  private final GraphicsContext gridGraphicsContext;
  private final GraphicsContext objectGraphicsContext;

  private final int cellSize;
  private double cameraX;
  private double cameraY;
  private double zoomScale;
  private final EditorController editorController;
  private final Map<UUID, Image> objectImages = new HashMap<>();
  private final List<UUID> displayedObjectIds = new ArrayList<>();
  private ObjectInteractionTool currentTool;
  private UUID selectedObjectId;
  private boolean drawHitboxes = false;

  /**
   * Creates a new editor game view.
   *
   * @param cellSize         Size of each grid cell in pixels.
   * @param editorController Controller for handling actions and state changes.
   * @throws IllegalArgumentException if editorController is null or dimensions/cellSize are
   *                                  non-positive.
   */
  public EditorGameView(int cellSize, double zoomScale, EditorController editorController) {
    if (editorController == null) {
      throw new IllegalArgumentException("EditorController cannot be null.");
    }
    if (cellSize <= 0) {
      LOG.error("Invalid dimensions provided: cellSize={}", cellSize);
      throw new IllegalArgumentException("View dimensions and cell size must be positive.");
    }

    this.cellSize = cellSize;
    this.cameraX = 0;
    this.cameraY = 0;
    this.zoomScale = zoomScale;
    this.editorController = editorController;

    this.gridCanvas = new Canvas();
    this.objectCanvas = new Canvas();
    this.gridGraphicsContext = gridCanvas.getGraphicsContext2D();
    this.objectGraphicsContext = objectCanvas.getGraphicsContext2D();

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
   * Initializes the view by drawing the grid and setting up event handlers. This is called
   * internally by the constructor.
   */
  private void initializeView() {
    drawGrid();
    setupEventHandlers();
    setupZoom();
  }

  /**
   * Draws the background grid lines on the dedicated grid canvas.
   */
  public void drawGrid() {
    double width = gridCanvas.getWidth();
    double height = gridCanvas.getHeight();

    gridGraphicsContext.setFill(GRID_BACKGROUND_COLOR);
    gridGraphicsContext.fillRect(0, 0, width, height);

    gridGraphicsContext.save();

    gridGraphicsContext.translate(-cameraX, -cameraY);
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

  private void setupZoom() {
    this.setOnScroll(event -> {
      if (event.isControlDown()) {
        double delta = event.getDeltaY();

        zoomScale = 1 / zoomScale;
        if (delta < 0) {
          zoomScale += ZOOM_SPEED;
        } else {
          zoomScale -= ZOOM_SPEED;
        }
        if (zoomScale < MIN_ZOOM) {
          zoomScale = MIN_ZOOM;
        }
        zoomScale = 1 / zoomScale;

        drawGrid();
        redrawObjects();

        event.consume();
      }
    });
  }

  /**
   * Handles mouse clicks on the object canvas. If a placement tool is active, it delegates the
   * click to the tool. Otherwise, it attempts to select an object at the click location.
   *
   * @param event The MouseEvent associated with the click.
   */
  private void handleGridClick(MouseEvent event) {

    double screenX = event.getX();
    double screenY = event.getY();

    double worldX = (screenX / zoomScale) + cameraX;
    double worldY = (screenY / zoomScale) + cameraY;

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
    //TODO: deselect all other tools
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
  private void redrawObjectsInternal() {
    double width = objectCanvas.getWidth();
    double height = objectCanvas.getHeight();

    objectGraphicsContext.clearRect(0, 0, width, height);
    objectGraphicsContext.save();
    objectGraphicsContext.translate(-cameraX, -cameraY);
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
    objectGraphicsContext.fillRect(hitboxX, hitboxY, hitboxWidth, hitboxHeight); // TODO: Other shapes other than rect
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
        && object.getIdentityData().getGroup() != null) {
      type = object.getIdentityData().getGroup();
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
   * Attempts to load the image associated with a given object ID. It retrieves the image path from
   * the object's data via the controller, resolves the path to a loadable URL (handling resource
   * vs. file paths), checks if a valid image is already cached, and if not, loads the image
   * asynchronously. It sets up listeners to redraw the view when the image finishes loading or if
   * an error occurs.
   *
   * @param id The UUID of the object whose image needs to be loaded or retrieved.
   */
  private void preloadObjectImage(UUID id) {
    try {
      EditorObject object = editorController.getEditorObject(id);
      if (object == null || object.getSpriteData() == null
          || object.getSpriteData().getSpritePath() == null || object.getSpriteData()
          .getSpritePath().isEmpty()) {
        objectImages.remove(id);
        LOG.trace("No valid sprite path found for object ID {}", id);
        return;
      }

      String imagePath = object.getSpriteData().getSpritePath();
      String url = resolveImagePath(imagePath);

      if (url == null) {
        LOG.error("Could not resolve image resource path: {}", imagePath);
        objectImages.remove(id);
        return;
      }

      Image cachedImage = objectImages.get(id);
      if (cachedImage == null || !cachedImage.getUrl().equals(url) || cachedImage.isError()) {
        LOG.debug("Loading image for object ID {} from resolved URL: {}", id, url);
        Image image = new Image(url, true);
        image.errorProperty().addListener((obs, oldErr, newErr) -> {
          if (newErr) {
            LOG.error("Failed to load image from {}: {}", url, image.getException().getMessage());
            redrawObjects();
          }
        });
        image.progressProperty().addListener((obs, oldProgress, newProgress) -> {
          if (newProgress.doubleValue() >= 1.0) {
            redrawObjects();
          }
        });
        objectImages.put(id, image);
      }
    } catch (Exception e) {
      LOG.error("Failed to preload image for object ID {}: {}", id, e.getMessage(), e);
      objectImages.remove(id);
      redrawObjects();
    }
  }

  /**
   * Helper method to resolve an image path string into a loadable URL string. Attempts to
   * differentiate between absolute/URL paths and relative resource paths. For relative paths, it
   * assumes they are located within the application's resources.
   *
   * @param path The image path string to resolve.
   * @return A string representation of the URL to load the image from, or null if the path cannot
   * be resolved or is invalid.
   */
  private String resolveImagePath(String path) {
    try {
      if (path == null || path.trim().isEmpty()) {
        return null;
      }
      if (path.startsWith("/") || path.matches("^[a-zA-Z]+:.*")) {
        return path;
      } else {
        String resourcePath = path.startsWith("/") ? path : "/" + path;
        java.net.URL resourceUrl = getClass().getResource(resourcePath);
        if (resourceUrl != null) {
          return resourceUrl.toExternalForm();
        } else {
          LOG.warn("Resource not found for relative path: {}", resourcePath);
          return null;
        }
      }
    } catch (Exception e) {
      LOG.warn("Could not resolve path: {}", path, e);
      return null;
    }
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
      LOG.debug("EditorGameView received: onObjectAdded {}", objectId);
      if (!displayedObjectIds.contains(objectId)) {
        displayedObjectIds.add(objectId);
        preloadObjectImage(objectId);
      }
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