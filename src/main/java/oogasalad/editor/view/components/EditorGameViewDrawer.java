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
 * Handles all drawing operations for the EditorGameView.
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

  EditorGameViewDrawer(EditorGameView view, EditorController controller,
      EditorGameViewImageManager imageManager, Logger log) {
    this.view = view;
    this.controller = controller;
    this.imageManager = imageManager;
    this.log = log;
  }

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

  private void applyCameraTransform(GraphicsContext gc) {
    gc.translate(-view.getCornerCameraX() * view.getZoomScale(),
        -view.getCornerCameraY() * view.getZoomScale());
    gc.scale(view.getZoomScale(), view.getZoomScale());
  }

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


  }

  void redrawObjects() {
    if (!Platform.isFxApplicationThread()) {
      Platform.runLater(this::redrawObjectsInternal);
    } else {
      redrawObjectsInternal();
    }
  }

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

  private List<UUID> getSortedObjectIds() {
    List<UUID> idsToDraw = new ArrayList<>(view.getDisplayedObjectIds());

    idsToDraw.sort(Comparator.comparingInt(
            (UUID id) -> controller.getEditorDataAPI().getObjectLayerPriority(id))
        .reversed());
    return idsToDraw;
  }

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

  private boolean isImageReady(Image image) {
    return image != null && !image.isError() && image.getProgress() >= 1.0;
  }

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
