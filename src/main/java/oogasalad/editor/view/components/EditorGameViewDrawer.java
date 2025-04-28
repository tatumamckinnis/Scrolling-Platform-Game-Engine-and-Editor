package oogasalad.editor.view.components;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.model.data.object.EditorObject;
import oogasalad.editor.model.data.object.sprite.FrameData;
import oogasalad.editor.model.data.object.sprite.SpriteData;
import org.apache.logging.log4j.Logger;

/**
 * Handles all drawing operations for the {@link EditorGameView}.
 * This class is responsible for drawing the grid background, game objects (sprites and hitboxes),
 * selection indicators, and placeholder graphics when images are unavailable. It manages the
 * {@link GraphicsContext} transformations based on camera pan and zoom, and ensures drawing
 * occurs on the JavaFX Application Thread.
 * It interacts with the {@link EditorController} to get object data and the
 * {@link EditorGameViewImageManager} to retrieve cached images.
 * @author Tatum McKinnis
 */
class EditorGameViewDrawer {

  private final EditorGameView view;
  private final EditorController controller;
  private final EditorGameViewImageManager imageManager;
  private final Logger log;


  private Color objectPlaceholderFill;
  private Color objectPlaceholderTextFill;
  private Color selectionBorderColor;
  private Color hitboxColor;
  private double selectionBorderWidth;
  private double placeholderFontSize;
  private String placeholderFontName;
  private String placeholderText;

  /**
   * Constructs an EditorGameViewDrawer.
   *
   * @param view The parent EditorGameView.
   * @param controller The application's editor controller.
   * @param imageManager The manager for object images.
   * @param log The logger instance for logging.
   */
  EditorGameViewDrawer(EditorGameView view, EditorController controller,
      EditorGameViewImageManager imageManager, Logger log) {
    this.view = view;
    this.controller = controller;
    this.imageManager = imageManager;
    this.log = log;
  }

  /**
   * Loads configurable drawing properties (colors, widths, font settings) from a Properties object.
   * Typically called during initialization using properties loaded from a configuration file.
   *
   * @param identifierProps The Properties object containing the drawing configuration values.
   * @throws RuntimeException if any essential configuration value is missing or cannot be parsed.
   */
  void loadConfigurableValues(Properties identifierProps) {
    try {
      objectPlaceholderFill = Color.web(identifierProps.getProperty("color.object.placeholder.fill"));
      objectPlaceholderTextFill = Color.web(identifierProps.getProperty("color.object.placeholder.text"));
      selectionBorderColor = Color.web(identifierProps.getProperty("color.selection.border"));
      hitboxColor = Color.web(identifierProps.getProperty("color.hitbox.fill"));
      selectionBorderWidth = Double.parseDouble(identifierProps.getProperty("width.selection.border"));
      placeholderFontSize = Double.parseDouble(identifierProps.getProperty("placeholder.font.size"));
      placeholderFontName = identifierProps.getProperty("placeholder.font.name");
      placeholderText = identifierProps.getProperty("placeholder.text");
    } catch (Exception e) {
      log.fatal("Failed to load drawing configuration values.", e);

      throw new RuntimeException("Failed to load essential drawing configuration.", e);
    }
  }

  /**
   * Draws the background grid onto the grid canvas.
   * Clears the canvas, applies camera transformations (pan/zoom), and draws the grid lines.
   */
  void drawGrid() {
    GraphicsContext gc = view.getGridGraphicsContext();
    double width = view.getGridWidth();
    double height = view.getGridHeight();
    view.updateCameraCoordinates();


    gc.setFill(view.getGridBackgroundColor());
    gc.fillRect(0, 0, width, height);


    gc.save();
    applyCameraTransform(gc);
    drawGridLines(gc);
    gc.restore();

    log.trace("Grid drawn.");
  }

  /**
   * Applies the current camera translation (pan) and scale (zoom) to the GraphicsContext.
   * This ensures subsequent drawing operations are performed in world coordinates relative
   * to the camera's view.
   *
   * @param gc The GraphicsContext to transform.
   */
  private void applyCameraTransform(GraphicsContext gc) {

    gc.translate(-view.getCornerCameraX() * view.getZoomScale(),
        -view.getCornerCameraY() * view.getZoomScale());

    gc.scale(view.getZoomScale(), view.getZoomScale());
  }

  /**
   * Draws the horizontal and vertical grid lines based on the current cell size and view bounds.
   * Also draws distinct lines for the origin axes (horizon lines). Assumes camera transform
   * has already been applied to the GraphicsContext.
   *
   * @param gc The transformed GraphicsContext for drawing.
   */
  private void drawGridLines(GraphicsContext gc) {
    int cellSize = view.getCellSize();

    int gridMinPixels = view.getGridMinBound() * cellSize;
    int gridMaxPixels = view.getGridMaxBound() * cellSize;


    gc.setStroke(view.getGridLineColor());

    gc.setLineWidth(view.getGridLineWidth() / view.getZoomScale());


    for (int x = gridMinPixels; x <= gridMaxPixels; x += cellSize) {
      gc.strokeLine(x, gridMinPixels, x, gridMaxPixels);
    }

    for (int y = gridMinPixels; y <= gridMaxPixels; y += cellSize) {
      gc.strokeLine(gridMinPixels, y, gridMaxPixels, y);
    }


    gc.setStroke(view.getGridHorizonColor());
    gc.setLineWidth(view.getGridHorizonWidth() / view.getZoomScale());

    if (0 >= gridMinPixels && 0 <= gridMaxPixels) {
      gc.strokeLine(0, gridMinPixels, 0, gridMaxPixels);
    }

    if (0 >= gridMinPixels && 0 <= gridMaxPixels) {
      gc.strokeLine(gridMinPixels, 0, gridMaxPixels, 0);
    }

  }

  /**
   * Schedules a redraw of all objects on the object canvas.
   * Ensures the actual drawing happens on the JavaFX Application Thread.
   */
  void redrawObjects() {
    if (!Platform.isFxApplicationThread()) {
      Platform.runLater(this::redrawObjectsInternal);
    } else {
      redrawObjectsInternal();
    }
  }

  /**
   * Performs the actual redrawing of all objects.
   * Clears the object canvas, applies camera transformations, sorts objects by layer priority,
   * draws each object (sprite and optional hitbox), and draws the selection indicator if applicable.
   * This method MUST be called on the JavaFX Application Thread.
   */
  private void redrawObjectsInternal() {
    GraphicsContext gc = view.getObjectGraphicsContext();
    double width = view.getGridWidth();
    double height = view.getGridHeight();
    view.updateCameraCoordinates();


    gc.clearRect(0, 0, width, height);
    gc.save();
    applyCameraTransform(gc);

    log.trace("Object canvas cleared. Drawing {} objects.", view.getDisplayedObjectIds().size());


    List<UUID> sortedIds = getSortedObjectIds();
    log.debug("Objects sorted by layer priority for drawing.");


    for (UUID id : sortedIds) {
      drawSingleObject(gc, id);
    }


    drawSelectionIfPresent(gc);

    gc.restore();
    log.trace("Finished redrawing objects.");
  }

  /**
   * Retrieves the list of displayed object IDs sorted by their layer priority in descending order.
   * Objects with higher priority values will appear later in the list and thus be drawn on top.
   *
   * @return A new list containing the sorted UUIDs of displayed objects.
   */
  private List<UUID> getSortedObjectIds() {

    List<UUID> idsToDraw = new ArrayList<>(view.getDisplayedObjectIds());


    idsToDraw.sort(Comparator.comparingInt(
            (UUID id) -> controller.getEditorDataAPI().getObjectLayerPriority(id))
        .reversed());
    return idsToDraw;
  }

  /**
   * Draws a single game object, including its sprite and hitbox (if enabled).
   * Fetches object data from the controller.
   *
   * @param gc The transformed GraphicsContext for drawing.
   * @param id The UUID of the object to draw.
   */
  private void drawSingleObject(GraphicsContext gc, UUID id) {
    try {
      int layerPriority = controller.getEditorDataAPI().getObjectLayerPriority(id);
      log.debug("Drawing object {} with layer priority {}", id, layerPriority);


      redrawSprites(gc, id);

      if (view.isDrawHitboxesEnabled()) {
        redrawHitboxes(gc, id);
      }
    } catch (Exception e) {

      log.error("Error drawing object with ID {}: {}", id, e.getMessage(), e);

    }
  }


  /**
   * Draws the selection indicator (e.g., a border) around the currently selected object,
   * if an object is selected and is currently being displayed.
   *
   * @param gc The transformed GraphicsContext for drawing.
   */
  private void drawSelectionIfPresent(GraphicsContext gc) {
    UUID selectedId = view.getSelectedObjectId();

    if (selectedId != null && view.getDisplayedObjectIds().contains(selectedId)) {
      EditorObject selectedObj = controller.getEditorObject(selectedId);

      if (selectedObj != null && selectedObj.getSpriteData() != null) {
        drawSelectionIndicator(gc, selectedObj.getSpriteData().getX(),
            selectedObj.getSpriteData().getY());
      }
    }
  }

  /**
   * Redraws the sprite for a given object ID.
   * Fetches the object's sprite data, retrieves the cached image using the image manager,
   * and draws the appropriate frame or a placeholder if the image is unavailable.
   *
   * @param gc The transformed GraphicsContext for drawing.
   * @param id The UUID of the object whose sprite needs redrawing.
   */
  private void redrawSprites(GraphicsContext gc, UUID id) {
    EditorObject object = controller.getEditorObject(id);

    if (object == null || object.getSpriteData() == null) {
      log.warn("[redrawSprites] Object ID {} or its SpriteData not found.", id);

      return;
    }

    SpriteData spriteData = object.getSpriteData();
    double dx = spriteData.getX();
    double dy = spriteData.getY();


    Image image = imageManager.getImage(id);


    if (isImageReady(image)) {
      drawFrameFromImage(gc, object, spriteData, image, dx, dy);
    } else {

      log.trace("[redrawSprites ID: {}] Image not ready or failed. Drawing placeholder.", id);
      drawPlaceholder(gc, object, dx, dy);
    }
  }

  /**
   * Checks if an Image object is fully loaded and not in an error state.
   *
   * @param image The Image to check.
   * @return true if the image is ready to be drawn, false otherwise.
   */
  private boolean isImageReady(Image image) {
    return image != null && !image.isError() && image.getProgress() >= 1.0;
  }

  /**
   * Determines the correct frame to display from the sprite sheet and draws it.
   * If the specified base frame isn't found, it attempts to fall back to the first available frame.
   * If no frames are available or the image is invalid, it draws a placeholder.
   *
   * @param gc The transformed GraphicsContext.
   * @param object The EditorObject being drawn.
   * @param spriteData The SpriteData containing frame information.
   * @param image The fully loaded Image (sprite sheet).
   * @param dx Destination x-coordinate (world space).
   * @param dy Destination y-coordinate (world space).
   */
  private void drawFrameFromImage(GraphicsContext gc, EditorObject object, SpriteData spriteData,
      Image image, double dx, double dy) {

    FrameData displayFrame = findDisplayFrame(spriteData, object.getId());

    if (displayFrame != null) {

      drawSpecificFrame(gc, image, displayFrame, dx, dy, object.getId());
    } else {

      log.error(
          "[redrawSprites ID: {}] Could not determine display frame (baseFrameName='{}', mapKeys={}). Drawing placeholder.",
          object.getId(), spriteData.getBaseFrameName(),
          spriteData.getFrames() != null ? spriteData.getFrames().keySet() : "null map");
      drawPlaceholder(gc, object, dx, dy);
    }
  }

  /**
   * Finds the {@link FrameData} to be displayed based on the sprite's base frame name.
   * Falls back to the first available frame if the base frame name is invalid or not found.
   *
   * @param spriteData The sprite data containing the frame map and base frame name.
   * @param id The UUID of the object (for logging).
   * @return The FrameData to display, or null if no suitable frame is found.
   */
  private FrameData findDisplayFrame(SpriteData spriteData, UUID id) {
    String baseFrameName = spriteData.getBaseFrameName();
    Map<String, FrameData> frameMap = spriteData.getFrames();

    log.debug("[redrawSprites ID: {}] Trying to display frame. Base='{}', Map size={}",
        id, baseFrameName, frameMap != null ? frameMap.size() : "null");


    if (baseFrameName != null && frameMap != null && frameMap.containsKey(baseFrameName)) {
      log.trace("[redrawSprites ID: {}] Using base frame '{}' from map.", id, baseFrameName);
      return frameMap.get(baseFrameName);
    } else if (frameMap != null && !frameMap.isEmpty()) {

      FrameData fallback = frameMap.values().iterator().next();
      log.warn(
          "[redrawSprites ID: {}] Base frame '{}' not found in map (Keys: {}). Falling back to first frame: {}",
          id, baseFrameName, frameMap.keySet(), fallback.name());
      return fallback;
    }

    return null;
  }

  /**
   * Draws a specific rectangular portion (frame) of a sprite sheet image onto the canvas.
   *
   * @param gc The transformed GraphicsContext.
   * @param image The source sprite sheet Image.
   * @param frame The FrameData defining the source rectangle (sx, sy, sw, sh).
   * @param dx The destination x-coordinate on the canvas (world space).
   * @param dy The destination y-coordinate on the canvas (world space).
   * @param id The UUID of the object being drawn (for logging).
   */
  private void drawSpecificFrame(GraphicsContext gc, Image image, FrameData frame, double dx,
      double dy, UUID id) {

    double sx = frame.x();
    double sy = frame.y();
    double sw = frame.width();
    double sh = frame.height();


    double dw = sw;
    double dh = sh;


    if (sw > 0 && sh > 0) {
      log.trace(
          "[redrawSprites ID: {}] Drawing frame '{}' from [{},{},{},{}] to [{},{},{},{}] (Actual Size)",
          id, frame.name(), sx, sy, sw, sh, dx, dy, dw, dh);

      gc.drawImage(image, sx, sy, sw, sh, dx, dy, dw, dh);
    } else {

      log.warn(
          "[redrawSprites ID: {}] Frame '{}' has invalid dimensions (w={}, h={}). Skipping draw.",
          id, frame.name(), sw, sh);

    }
  }


  /**
   * Draws the hitbox overlay for a given object ID, if hitboxes are enabled.
   * Fetches hitbox data from the controller and draws a semi-transparent rectangle.
   *
   * @param gc The transformed GraphicsContext.
   * @param id The UUID of the object whose hitbox needs drawing.
   */
  private void redrawHitboxes(GraphicsContext gc, UUID id) {
    EditorObject object = controller.getEditorObject(id);

    if (object == null || object.getHitboxData() == null) {
      log.warn("Object ID {} or its HitboxData not found during hitbox redraw.", id);
      return;
    }

    int hitboxX = object.getHitboxData().getX();
    int hitboxY = object.getHitboxData().getY();
    int hitboxWidth = object.getHitboxData().getWidth();
    int hitboxHeight = object.getHitboxData().getHeight();


    if (hitboxWidth > 0 && hitboxHeight > 0) {
      gc.setFill(hitboxColor);
      gc.fillRect(hitboxX, hitboxY, hitboxWidth, hitboxHeight);
    } else {

      log.trace("Skipping hitbox draw for {} due to zero width/height.", id);
    }
  }

  /**
   * Draws a placeholder graphic for an object when its image is unavailable.
   * Typically draws a colored rectangle with text inside.
   *
   * @param g The transformed GraphicsContext.
   * @param object The EditorObject being drawn (used potentially for size, though currently uses cell size).
   * @param x The destination x-coordinate (world space).
   * @param y The destination y-coordinate (world space).
   */
  private void drawPlaceholder(GraphicsContext g, EditorObject object, double x, double y) {
    int cellSize = view.getCellSize();

    g.setFill(objectPlaceholderFill);
    g.fillRect(x, y, cellSize, cellSize);

    g.setFill(objectPlaceholderTextFill);
    g.setFont(Font.font(placeholderFontName, placeholderFontSize));
    g.setTextAlign(TextAlignment.CENTER);

    g.fillText(placeholderText, x + cellSize / 2.0,
        y + cellSize / 2.0 + placeholderFontSize / 3.0);
  }

  /**
   * Draws the selection indicator border around a selected object.
   * Adjusts border width based on zoom level to maintain visibility.
   *
   * @param g The transformed GraphicsContext.
   * @param x The x-coordinate of the selected object's top-left corner (world space).
   * @param y The y-coordinate of the selected object's top-left corner (world space).
   */
  private void drawSelectionIndicator(GraphicsContext g, double x, double y) {
    double currentZoom = view.getZoomScale();
    int cellSize = view.getCellSize();

    g.save();
    g.setStroke(selectionBorderColor);


    double effectiveBorderWidth = selectionBorderWidth / currentZoom;
    g.setLineWidth(effectiveBorderWidth);


    double drawX = x - effectiveBorderWidth / 2.0;
    double drawY = y - effectiveBorderWidth / 2.0;
    double drawW = cellSize + effectiveBorderWidth;
    double drawH = cellSize + effectiveBorderWidth;


    g.strokeRect(drawX, drawY, drawW, drawH);
    g.restore();
  }
}