package oogasalad.editor.view;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;
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
import oogasalad.editor.model.data.object.sprite.FrameData;
import oogasalad.editor.model.data.object.sprite.SpriteData;
import oogasalad.editor.view.tools.ObjectInteractionTool;
import oogasalad.fileparser.records.BlueprintData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Displays a grid where visual game elements are added/updated. Implements EditorViewListener to
 * react to model changes notified by the controller. Handles drawing, basic object selection
 * notification, delegates placement actions, and manages drag-and-drop operations for placing
 * prefabs.
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

  private final int cellSize;
  private final EditorController editorController;
  private final Properties identifierProps;
  private final ResourceBundle uiBundle;
  private final Map<UUID, Image> objectImages = new HashMap<>();
  private final List<UUID> displayedObjectIds = new ArrayList<>();
  private final PrefabPalettePane prefabPalettePane;

  private double cornerCameraX;
  private double cornerCameraY;
  private double centerCameraX;
  private double centerCameraY;
  private double zoomScale;
  private ObjectInteractionTool currentTool;
  private UUID selectedObjectId;
  private AnimationTimer panTimer;
  private double panVelocityX = 0;
  private double panVelocityY = 0;

  private Color gridBackgroundColor;
  private Color gridLineColor;
  private Color gridHorizonColor;
  private Color objectPlaceholderFill;
  private Color objectPlaceholderTextFill;
  private Color selectionBorderColor;
  private Color hitboxColor;
  private double gridLineWidth;
  private double gridHorizonWidth;
  private double selectionBorderWidth;
  private double placeholderFontSize;
  private String placeholderFontName;
  private String placeholderText;
  private double zoomSpeed;
  private double minZoom;
  private double panSpeed;
  private int gridMinBound;
  private int gridMaxBound;

  private boolean drawHitboxes = true;
  private boolean snapToGrid = true;

  /**
   * Creates a new editor game view.
   *
   * @param cellSize          Size of each grid cell in pixels.
   * @param initialZoomScale  The initial zoom level.
   * @param editorController  Controller for handling actions and state changes.
   * @param prefabPalettePane The palette pane for accessing selected prefabs.
   * @param uiBundle          ResourceBundle for localized UI text (errors).
   * @throws IllegalArgumentException if controller, palette, or bundle is null or
   *                                  dimensions/cellSize are non-positive.
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

    loadConfigurableValues();

    this.gridCanvas = new Canvas();
    this.objectCanvas = new Canvas();
    this.gridGraphicsContext = gridCanvas.getGraphicsContext2D();
    this.objectGraphicsContext = objectCanvas.getGraphicsContext2D();

    this.setId(getId("id.view"));
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
   * Loads configuration values (colors, widths, speeds, etc.) from the identifier properties file.
   */
  private void loadConfigurableValues() {
    try {
      gridBackgroundColor = Color.web(getId("color.grid.background"));
      gridLineColor = Color.web(getId("color.grid.line"));
      gridHorizonColor = Color.web(getId("color.grid.horizon"));
      objectPlaceholderFill = Color.web(getId("color.object.placeholder.fill"));
      objectPlaceholderTextFill = Color.web(getId("color.object.placeholder.text"));
      selectionBorderColor = Color.web(getId("color.selection.border"));
      hitboxColor = Color.web(getId("color.hitbox.fill"));

      gridLineWidth = Double.parseDouble(getId("width.grid.line"));
      gridHorizonWidth = Double.parseDouble(getId("width.grid.horizon"));
      selectionBorderWidth = Double.parseDouble(getId("width.selection.border"));

      placeholderFontSize = Double.parseDouble(getId("placeholder.font.size"));
      placeholderFontName = getId("placeholder.font.name");
      placeholderText = getId("placeholder.text");

      zoomSpeed = Double.parseDouble(getId("zoom.speed"));
      minZoom = Double.parseDouble(getId("zoom.min"));
      panSpeed = Double.parseDouble(getId("pan.speed"));

      gridMinBound = Integer.parseInt(getId("grid.min.bound"));
      gridMaxBound = Integer.parseInt(getId("grid.max.bound"));

    } catch (Exception e) {
      LOG.fatal(
          "Failed to load or parse one or more configuration values from identifier properties.",
          e);
      throw new RuntimeException("Failed to load essential view configuration.", e);
    }
  }

  /**
   * Loads the identifier strings (keys, CSS classes, IDs, paths) from the properties file.
   *
   * @return A Properties object containing the loaded identifiers.
   * @throws RuntimeException If the properties file cannot be found or read.
   */
  private Properties loadIdentifierProperties() {
    Properties props = new Properties();
    try (InputStream input = EditorGameView.class.getResourceAsStream(
        IDENTIFIERS_PROPERTIES_PATH)) {
      if (input == null) {
        LOG.error("CRITICAL: Unable to find identifiers properties file: {}",
            IDENTIFIERS_PROPERTIES_PATH);
        throw new RuntimeException(
            "Missing required identifiers properties file: " + IDENTIFIERS_PROPERTIES_PATH);
      }
      props.load(input);
    } catch (IOException ex) {
      LOG.error("CRITICAL: Error loading identifiers properties file: {}",
          IDENTIFIERS_PROPERTIES_PATH, ex);
      throw new RuntimeException("Error loading identifiers properties file", ex);
    }
    return props;
  }

  /**
   * Retrieves an identifier value from the loaded identifier properties.
   *
   * @param key The key for the identifier.
   * @return The identifier string.
   * @throws RuntimeException If the key is not found.
   */
  private String getId(String key) {
    String value = identifierProps.getProperty(key);
    if (value == null || value.trim().isEmpty()) {
      LOG.error("Missing identifier in properties file for key: {}", key);
      throw new RuntimeException("Missing identifier in properties file for key: " + key);
    }
    return value;
  }

  /**
   * Gets the currently selected prefab from the associated palette pane.
   *
   * @return The selected BlueprintData, or null if none is selected.
   */
  public BlueprintData getSelectedPrefab() {
    return prefabPalettePane.getSelectedPrefab();
  }

  /**
   * Initializes the view by drawing the grid and setting up event handlers for mouse interactions,
   * zooming, panning, and drag-and-drop. This is called internally by the constructor.
   */
  private void initializeView() {
    setupEventHandlers();
    setupZoom();
    setupPanning();
    drawGrid();
  }

  /**
   * Draws the background grid lines on the dedicated grid canvas using configured colors and
   * widths.
   */
  public void drawGrid() {
    double width = gridCanvas.getWidth();
    double height = gridCanvas.getHeight();
    updateCameraCoordinates();

    gridGraphicsContext.setFill(gridBackgroundColor);
    gridGraphicsContext.fillRect(0, 0, width, height);

    gridGraphicsContext.save();
    gridGraphicsContext.translate(-cornerCameraX * zoomScale, -cornerCameraY * zoomScale);
    gridGraphicsContext.scale(zoomScale, zoomScale);

    int gridMinPixels = gridMinBound * cellSize;
    int gridMaxPixels = gridMaxBound * cellSize;
    gridGraphicsContext.setStroke(gridLineColor);
    gridGraphicsContext.setLineWidth(gridLineWidth);

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
   * Sets up mouse event handlers for interaction on the object canvas, including click handling and
   * drag-and-drop target handling.
   */
  private void setupEventHandlers() {
    objectCanvas.setOnMouseClicked(this::handleGridClick);

    objectCanvas.setOnDragOver(event -> {
      Dragboard db = event.getDragboard();
      if (db.hasContent(PrefabPalettePane.PREFAB_BLUEPRINT_ID)) {
        event.acceptTransferModes(TransferMode.COPY);
        LOG.trace("Drag over accepted for prefab ID");
      }
      event.consume();
    });

    objectCanvas.setOnDragDropped(event -> {
      Dragboard db = event.getDragboard();
      boolean success = false;
      if (db.hasContent(PrefabPalettePane.PREFAB_BLUEPRINT_ID)) {
        LOG.debug("Prefab dropped onto game view");
        try {
          String blueprintIdStr = (String) db.getContent(PrefabPalettePane.PREFAB_BLUEPRINT_ID);
          int blueprintId = Integer.parseInt(blueprintIdStr);
          BlueprintData prefabData = prefabPalettePane.getPrefabById(blueprintId);

          if (prefabData != null) {
            double screenX = event.getX();
            double screenY = event.getY();
            double[] worldCoords = screenToWorld(screenX, screenY);
            double worldX = worldCoords[0];
            double worldY = worldCoords[1];

            LOG.info("Requesting placement for prefab ID {} ({}) at world coords ({}, {})",
                blueprintId, prefabData.type(), worldX, worldY);

            editorController.requestPrefabPlacement(prefabData, worldX, worldY);
            success = true;
          } else {
            LOG.error("Dropped prefab ID {} not found in palette's loaded prefabs.", blueprintId);
            editorController.notifyErrorOccurred("Could not find data for dropped prefab.");
          }
        } catch (NumberFormatException e) {
          LOG.error("Failed to parse blueprint ID from dragboard: {}",
              db.getContent(PrefabPalettePane.PREFAB_BLUEPRINT_ID), e);
          editorController.notifyErrorOccurred("Invalid data format on drop.");
        } catch (Exception e) {
          LOG.error("Error handling prefab drop: {}", e.getMessage(), e);
          editorController.notifyErrorOccurred("Error placing dropped prefab: " + e.getMessage());
        }
      }
      event.setDropCompleted(success);
      event.consume();
    });
  }

  /**
   * Converts screen coordinates (e.g., from a mouse event) to world coordinates, applying camera
   * translation, zoom, and grid snapping if enabled.
   *
   * @param screenX The x-coordinate in the view's screen space.
   * @param screenY The y-coordinate in the view's screen space.
   * @return A double array containing {worldX, worldY}.
   */
  private double[] screenToWorld(double screenX, double screenY) {
    updateCameraCoordinates();
    double worldX = cornerCameraX + (screenX / zoomScale);
    double worldY = cornerCameraY + (screenY / zoomScale);

    if (snapToGrid) {
      worldX = Math.floor(worldX / cellSize) * cellSize;
      worldY = Math.floor(worldY / cellSize) * cellSize;
    }
    return new double[]{worldX, worldY};
  }


  /**
   * Sets up zoom behavior by adding a scroll listener to the pane. Zooming is triggered when the
   * user scrolls (e.g., with a mouse wheel or trackpad). Assumes standard scroll delta
   * interpretation.
   */
  private void setupZoom() {
    this.setOnScroll(event -> {
      handleZoomScroll(event.getDeltaY());
      event.consume();
    });
  }

  /**
   * Handles the logic for zooming in or out based on the scroll delta. Updates the zoom scale,
   * camera position, grid, and redraws all objects.
   *
   * @param deltaY The amount of scroll (positive for zoom out, negative for zoom in typically).
   */
  private void handleZoomScroll(double deltaY) {
    zoomScale = calculateNewZoomScale(deltaY, zoomScale);
    updateCameraCoordinates();
    drawGrid();
    redrawObjects();
  }

  /**
   * Calculates the new zoom scale based on the scroll delta and current zoom scale. Applies
   * constraints to avoid zooming beyond the configured minimum limit.
   *
   * @param deltaY           The scroll delta indicating zoom direction.
   * @param currentZoomScale The current zoom scale before applying scroll.
   * @return The adjusted zoom scale after clamping within valid bounds.
   */
  private double calculateNewZoomScale(double deltaY, double currentZoomScale) {
    double zoomFactor = Math.pow(1 + zoomSpeed, -deltaY);
    double newZoomScale = currentZoomScale * zoomFactor;

    newZoomScale = Math.max(minZoom, newZoomScale);

    LOG.trace("Zoom updated: delta={}, factor={}, oldScale={}, newScale={}", deltaY, zoomFactor,
        currentZoomScale, newZoomScale);
    return newZoomScale;
  }

  /**
   * Sets up keyboard event handlers for panning the view using WASD keys. Uses an
   * {@link AnimationTimer} for smooth panning based on key presses.
   */
  private void setupPanning() {
    this.setFocusTraversable(true);

    this.setOnKeyPressed(event -> {
      switch (event.getCode()) {
        case A -> panVelocityX = -panSpeed;
        case D -> panVelocityX = panSpeed;
        case W -> panVelocityY = -panSpeed;
        case S -> panVelocityY = panSpeed;
        default -> {
          return;
        }
      }
      event.consume();
    });

    this.setOnKeyReleased(event -> {
      switch (event.getCode()) {
        case A, D -> panVelocityX = 0;
        case W, S -> panVelocityY = 0;
        default -> {
          return;
        }
      }
      event.consume();
    });

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
   * Updates the top-left corner coordinates of the camera view based on the current center
   * coordinates, canvas dimensions, and zoom scale.
   */
  private void updateCameraCoordinates() {
    double canvasWidth = gridCanvas.getWidth();
    double canvasHeight = gridCanvas.getHeight();

    cornerCameraX = centerCameraX - (canvasWidth / (2.0 * zoomScale));
    cornerCameraY = centerCameraY - (canvasHeight / (2.0 * zoomScale));
  }

  /**
   * Handles mouse click events on the object canvas. Requests focus, calculates world coordinates
   * from screen coordinates (applying snapping if enabled), and delegates the interaction to the
   * currently active {@link ObjectInteractionTool}.
   *
   * @param event The MouseEvent associated with the click.
   */
  private void handleGridClick(MouseEvent event) {
    this.requestFocus();
    double screenX = event.getX();
    double screenY = event.getY();

    double[] worldCoords = screenToWorld(screenX, screenY);
    double worldX = worldCoords[0];
    double worldY = worldCoords[1];

    LOG.debug("Click at screen=({},{}) => world=({},{}) (Snapped: {})",
        screenX, screenY, worldX, worldY, snapToGrid);

    if (currentTool != null) {
      LOG.info("Delegating click to tool: {}", currentTool.getClass().getSimpleName());
      currentTool.interactObjectAt((int) worldX, (int) worldY);
    } else {
      LOG.trace("Grid clicked, but no interaction tool is active.");
    }
  }

  /**
   * Sets the currently active object interaction tool for the view.
   *
   * @param tool The ObjectInteractionTool to activate, or null to deactivate interaction tools.
   */
  public void updateCurrentTool(ObjectInteractionTool tool) {
    this.currentTool = tool;
    LOG.info("Current interaction tool set to: {}",
        (tool != null) ? tool.getClass().getSimpleName() : "None");
  }

  /**
   * Initiates a redraw of all objects currently displayed on the object canvas. Ensures the drawing
   * operations occur safely on the JavaFX Application Thread.
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
   * and then iterates through the list of displayed object IDs, drawing each object's sprite (or
   * placeholder) and hitbox (if enabled). Also draws the selection indicator for the currently
   * selected object.
   */
  void redrawObjectsInternal() {
    double width = objectCanvas.getWidth();
    double height = objectCanvas.getHeight();
    updateCameraCoordinates();

    objectGraphicsContext.clearRect(0, 0, width, height);

    objectGraphicsContext.save();
    objectGraphicsContext.translate(-cornerCameraX * zoomScale, -cornerCameraY * zoomScale);
    objectGraphicsContext.scale(zoomScale, zoomScale);

    LOG.trace("Object canvas cleared for redraw. Drawing {} objects.", displayedObjectIds.size());
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

    if (selectedObjectId != null && displayedObjectIds.contains(selectedObjectId)) {
      EditorObject selectedObj = editorController.getEditorObject(selectedObjectId);
      if (selectedObj != null && selectedObj.getSpriteData() != null) {
        drawSelectionIndicator(objectGraphicsContext, selectedObj.getSpriteData().getX(),
            selectedObj.getSpriteData().getY());
      }
    }

    objectGraphicsContext.restore();
    LOG.trace("Finished redrawing objects.");
  }


  /**
   * Draws a placeholder rectangle with configured text inside when an object's image is
   * unavailable. Uses colors, font name, and font size loaded from identifier properties.
   *
   * @param g      The GraphicsContext to draw on.
   * @param object The EditorObject (used potentially for type info, though currently unused).
   * @param x      The world x-coordinate where the placeholder should be drawn.
   * @param y      The world y-coordinate where the placeholder should be drawn.
   */
  private void drawPlaceholder(GraphicsContext g, EditorObject object, double x, double y) {
    g.setFill(objectPlaceholderFill);
    g.fillRect(x, y, cellSize, cellSize);
    g.setFill(objectPlaceholderTextFill);
    g.setFont(Font.font(placeholderFontName, placeholderFontSize));
    g.setTextAlign(TextAlignment.CENTER);
    g.fillText(placeholderText, x + cellSize / 2.0, y + cellSize / 2.0 + placeholderFontSize / 3.0);
  }

  /**
   * Draws a visual indicator (a border) around the cell occupied by the selected object. Uses color
   * and line width loaded from identifier properties.
   *
   * @param g The GraphicsContext to draw on.
   * @param x The world x-coordinate of the selected object's top-left corner.
   * @param y The world y-coordinate of the selected object's top-left corner.
   */
  private void drawSelectionIndicator(GraphicsContext g, double x, double y) {
    g.save();
    g.setStroke(selectionBorderColor);
    double effectiveBorderWidth = selectionBorderWidth / zoomScale;
    g.setLineWidth(effectiveBorderWidth);
    g.strokeRect(x - effectiveBorderWidth / 2, y - effectiveBorderWidth / 2,
        cellSize + effectiveBorderWidth, cellSize + effectiveBorderWidth);
    g.restore();
  }


  private void preloadObjectImage(UUID id) {
    try {
      String imagePath = getObjectSpritePath(id);
      if (imagePath == null) {
        objectImages.remove(id);
        LOG.trace("No valid sprite path found for ID {}. Removing image cache.", id);
        return;
      }

      String resolvedPathOrUrl = resolveImagePath(imagePath);
      if (resolvedPathOrUrl == null) {
        LOG.error("Could not resolve image path/URL for: {}", imagePath);
        objectImages.remove(id);
        redrawObjects();
        return;
      }

      loadImageIfNotCached(id, resolvedPathOrUrl);

    } catch (Exception e) {
      LOG.error("Failed during image preload process for object ID {}: {}", id, e.getMessage(), e);
      objectImages.remove(id);
      redrawObjects();
    }
  }



  /**
   * Retrieves the sprite path associated with an object from the controller.
   *
   * @param id The UUID of the object.
   * @return The sprite path string, or null if the object, sprite data, or path is missing/empty.
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
   * Loads an image from the specified path string into the cache if it's not already present,
   * has a different path than the cached version, or if the cached image is marked as errored.
   * Handles converting absolute file paths to proper file URLs for JavaFX Image loading.
   *
   * @param id   The UUID of the object associated with the image.
   * @param path The path string (can be absolute file path or classpath resource).
   */
  private void loadImageIfNotCached(UUID id, String path) {
    Image cachedImage = objectImages.get(id);

    String urlString = null;
    try {
      File file = new File(path);
      if (file.isAbsolute() && file.exists()) {
        urlString = file.toURI().toString();
        LOG.trace("Converted absolute path '{}' to URL '{}'", path, urlString);
      } else {
        urlString = path;
        LOG.trace("Assuming path '{}' is a classpath resource or URL", path);
      }
    } catch (Exception e) {
      LOG.error("Error converting path '{}' to URL: {}", path, e.getMessage(), e);
      objectImages.remove(id);
      redrawObjects();
      return;
    }

    if (urlString == null) {
      LOG.error("Could not create a valid URL string from path: {}", path);
      objectImages.remove(id);
      redrawObjects();
      return;
    }


    boolean needsLoading = cachedImage == null
        || !Objects.equals(cachedImage.getUrl(), urlString)
        || cachedImage.isError();

    if (needsLoading) {
      LOG.debug("Loading image for object ID {} from resolved URL string: {}", id, urlString);
      try {
        Image newImage = new Image(urlString, true);
        setupImageListeners(newImage, urlString);
        objectImages.put(id, newImage);
      } catch (IllegalArgumentException e) {
        LOG.error("Failed to load image for {} - Invalid URL or resource not found: {}", id, urlString, e);
        objectImages.remove(id);
        redrawObjects();
      } catch (Exception e) {
        LOG.error("Unexpected error loading image for {} from URL {}: {}", id, urlString, e.getMessage(), e);
        objectImages.remove(id);
        redrawObjects();
      }
    } else {
      LOG.trace("Image for {} already cached and valid: {}", id, urlString);
    }
  }


  /**
   * Attaches listeners to an image's error and progress properties to handle asynchronous loading
   * feedback and trigger UI updates (redraws).
   *
   * @param image The JavaFX Image object being loaded.
   * @param url   The source URL of the image, used for logging purposes.
   */
  private void setupImageListeners(Image image, String url) {
    image.errorProperty().addListener((obs, oldErr, newErr) -> {
      if (newErr) {
        handleImageError(image, url);
      }
    });
    image.progressProperty().addListener((obs, oldProgress, newProgress) -> {
      if (newProgress != null && newProgress.doubleValue() >= 1.0) {
        handleImageLoadComplete(image, url);
      }
    });
  }

  /**
   * Handles the scenario where an image loading error occurs. Logs the error details and triggers a
   * redraw, which will likely result in a placeholder being shown.
   *
   * @param image The image that encountered an error.
   * @param url   The source URL of the image, for context in logging.
   */
  private void handleImageError(Image image, String url) {
    String errorMessage = (image.getException() != null) ? image.getException().getMessage()
        : "Unknown image loading error";
    LOG.error("Failed to load image from {}: {}", url, errorMessage);
    redrawObjects();
  }

  /**
   * Handles the scenario when an image finishes loading (progress reaches 1.0). Checks if the load
   * completed successfully or with an error and triggers a redraw.
   *
   * @param image The image whose loading has completed.
   * @param url   The source URL of the image, for context in logging.
   */
  private void handleImageLoadComplete(Image image, String url) {
    if (image.isError()) {
      LOG.error("Error flag set after image load completion signal for URL: {}", url);
    } else {
      LOG.trace("Image loaded successfully: {}", url);
    }
    redrawObjects();
  }


  private String resolveImagePath(String path) {
    if (path == null || path.trim().isEmpty()) {
      return null;
    }
    File f = new File(path);
    if (f.isAbsolute()) {
      LOG.trace("Path '{}' identified as absolute.", path);
      return path;
    }

    LOG.trace("Path '{}' not absolute, attempting classpath resolution.", path);
    return findResourcePath(path);
  }

  /**
   * Attempts to find a relative path as a resource within the application's classpath. Prepends "/"
   * if not already present.
   *
   * @param relativePath The relative path string (e.g., "images/player.png").
   * @return The full URL string if the resource is found, or null otherwise.
   */
  private String findResourcePath(String relativePath) {
    try {
      String resourcePath = relativePath.startsWith("/") ? relativePath : "/" + relativePath;
      java.net.URL resourceUrl = getClass().getResource(resourcePath);

      if (resourceUrl != null) {
        return resourceUrl.toExternalForm();
      }

      String gameName = editorController
          .getEditorDataAPI()
          .getGameName();
      Path assetPath = Paths.get("data", "graphicsData", gameName, relativePath);
      File assetFile = assetPath.toFile();
      if (assetFile.exists() && assetFile.isFile()) {
        return assetFile.toURI().toString();
      }

      LOG.warn("Classpath resource not found for relative path: {}", resourcePath);
      return null;
    } catch (Exception e) {
      LOG.warn("Could not resolve relative resource path '{}': {}", relativePath, e.getMessage());
      return null;
    }
  }




  /**
   * Redraws the sprite visual for a specific object ID on the object canvas.
   * Retrieves object data, checks for a loaded image, finds the correct frame,
   * and draws the specific frame using its original dimensions.
   *
   * @param id The UUID of the object whose sprite needs redrawing.
   */
  private void redrawSprites(UUID id) {
    EditorObject object = editorController.getEditorObject(id);
    if (object == null || object.getSpriteData() == null) {
      LOG.warn("[redrawSprites] Object ID {} or its SpriteData not found.", id);
      return;
    }

    SpriteData spriteData = object.getSpriteData();
    double dx = spriteData.getX();
    double dy = spriteData.getY();

    Image image = objectImages.get(id);

    if (image != null && !image.isError() && image.getProgress() >= 1.0) {
      FrameData displayFrame = null;
      String baseFrameName = spriteData.getName();
      Map<String, FrameData> frameMap = spriteData.getFrames();

      LOG.debug("[redrawSprites ID: {}] Trying to display frame.", id);
      LOG.debug("[redrawSprites ID: {}] BaseFrame Name from SpriteData: '{}'", id, baseFrameName);
      LOG.debug("[redrawSprites ID: {}] Frame Map Size: {}", id, frameMap != null ? frameMap.size() : "null map");
      if (frameMap != null && baseFrameName != null) {
        LOG.debug("[redrawSprites ID: {}] Frame Map Keys: {}", id, frameMap.keySet());
        LOG.debug("[redrawSprites ID: {}] Does map contain key '{}'? {}", id, baseFrameName, frameMap.containsKey(baseFrameName));
      }

      if (baseFrameName != null && frameMap != null && frameMap.containsKey(baseFrameName)) {
        displayFrame = frameMap.get(baseFrameName);
        LOG.trace("[redrawSprites ID: {}] Using base frame '{}' retrieved from map.", id, baseFrameName);
      } else if (frameMap != null && !frameMap.isEmpty()) {
        displayFrame = frameMap.values().iterator().next();
        LOG.warn("[redrawSprites ID: {}] Base frame '{}' not found in map (Keys: {}). Falling back to first available frame: {}",
            id, baseFrameName, frameMap.keySet(), displayFrame.name());
      }

      if (displayFrame != null) {
        double sx = displayFrame.x();
        double sy = displayFrame.y();
        double sw = displayFrame.width();
        double sh = displayFrame.height();

        double dw = sw;
        double dh = sh;

        if (sw > 0 && sh > 0) {
          LOG.trace("[redrawSprites ID: {}] Drawing frame '{}' from [{},{},{},{}] to [{},{},{},{}] (Actual Size)",
              id, displayFrame.name(), sx, sy, sw, sh, dx, dy, dw, dh);
          objectGraphicsContext.drawImage(image, sx, sy, sw, sh, dx, dy, dw, dh);
        } else {
          LOG.warn("[redrawSprites ID: {}] Frame '{}' has invalid dimensions (w={}, h={}). Drawing placeholder.", id, displayFrame.name(), sw, sh);
          drawPlaceholder(objectGraphicsContext, object, dx, dy);
        }
      } else {
        LOG.error("[redrawSprites ID: {}] Could not determine display frame (baseFrameName='{}', mapKeys={}). Drawing placeholder.",
            id, baseFrameName, frameMap != null ? frameMap.keySet() : "null map");
        drawPlaceholder(objectGraphicsContext, object, dx, dy);
      }
    } else {
      LOG.trace("[redrawSprites ID: {}] Image not ready or failed. Drawing placeholder.", id);
      drawPlaceholder(objectGraphicsContext, object, dx, dy);
    }
  }
  /**
   * Redraws the hitbox visualization for a specific object ID on the object canvas. Retrieves
   * object data, gets hitbox dimensions, and draws a semi-transparent rectangle.
   *
   * @param id The UUID of the object whose hitbox needs redrawing.
   */
  private void redrawHitboxes(UUID id) {
    EditorObject object = editorController.getEditorObject(id);
    if (object == null || object.getHitboxData() == null) {
      LOG.warn("Object ID {} or its HitboxData not found during hitbox redraw.", id);
      return;
    }
    int hitboxX = object.getHitboxData().getX();
    int hitboxY = object.getHitboxData().getY();
    int hitboxWidth = object.getHitboxData().getWidth();
    int hitboxHeight = object.getHitboxData().getHeight();

    objectGraphicsContext.setFill(hitboxColor);
    objectGraphicsContext.fillRect(hitboxX, hitboxY, hitboxWidth, hitboxHeight);
  }

  /**
   * {@inheritDoc} Adds the object ID, preloads its image, and redraws. Ensures execution on the FX
   * thread.
   */
  @Override
  public void onObjectAdded(UUID objectId) {
    Platform.runLater(() -> {
      LOG.debug("EditorGameView received: onObjectAdded {}", objectId);
      if (!displayedObjectIds.contains(objectId)) {
        displayedObjectIds.add(objectId);
        preloadObjectImage(objectId);
        redrawObjects();
      } else {
        preloadObjectImage(objectId);
        redrawObjects();
      }
    });
  }

  /**
   * {@inheritDoc} Removes the object ID and its cached image, then redraws. Ensures execution on
   * the FX thread.
   */
  @Override
  public void onObjectRemoved(UUID objectId) {
    if (displayedObjectIds.remove(objectId)) {
      objectImages.remove(objectId);
    }
    Platform.runLater(() -> {
      LOG.debug("EditorGameView received: onObjectRemoved {}", objectId);
      if (Objects.equals(selectedObjectId, objectId)) {
        selectedObjectId = null;
      }
      redrawObjects();
    });
  }

  /**
   * {@inheritDoc} Preloads the object's image (in case sprite changed) and redraws. Ensures
   * execution on the FX thread.
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
   * {@inheritDoc} Updates the selected object ID and redraws to show/hide selection indicator.
   * Ensures execution on the FX thread.
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
   * {@inheritDoc} Logs the event; currently no direct action needed in this view.
   */
  @Override
  public void onDynamicVariablesChanged() {
    LOG.trace("EditorGameView received: onDynamicVariablesChanged (no action taken).");
  }

  /**
   * {@inheritDoc} Logs the error message received from the controller.
   */
  @Override
  public void onErrorOccurred(String errorMessage) {
    LOG.warn("EditorGameView received: onErrorOccurred: {}", errorMessage);
  }

  /**
   * {@inheritDoc} Logs the event; currently no direct action needed in this view regarding prefab
   * list changes.
   */
  @Override
  public void onPrefabsChanged() {
    LOG.debug("EditorGameView notified of prefab changes (no direct action taken).");
  }

  /**
   * Called when a sprite template is changed
   */
  @Override
  public void onSpriteTemplateChanged() {
  }

  /**
   * Gets the size of each grid cell in pixels.
   *
   * @return The cell size used for drawing and snapping.
   */
  public int getCellSize() {
    return cellSize;
  }

  /**
   * Gets the current width of the game view canvas.
   *
   * @return The current canvas width in pixels.
   */
  public double getGridWidth() {
    return gridCanvas.getWidth();
  }

  /**
   * Gets the current height of the game view canvas.
   *
   * @return The current canvas height in pixels.
   */
  public double getGridHeight() {
    return gridCanvas.getHeight();
  }

  public void removeAllObjects() {
    displayedObjectIds.clear();
    objectImages.clear();
    redrawObjects();
  }

}
