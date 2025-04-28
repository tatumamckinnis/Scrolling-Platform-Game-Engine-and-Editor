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
 * @author Tatum McKinnis (Original)
 * @author Gemini (Refactored)
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

    // *** Instantiate helper classes BEFORE loadConfigurableValues ***
    this.imageManager = new EditorGameViewImageManager(this, editorController, LOG);
    this.drawer = new EditorGameViewDrawer(this, editorController, imageManager, LOG);
    this.eventHandler = new EditorGameViewEventHandler(this, editorController, prefabPalettePane,
        drawer, LOG);

    // *** Now load config values, drawer will be initialized ***
    loadConfigurableValues();

    this.setId(getId("id.view"));
    getChildren().addAll(gridCanvas, objectCanvas);

    bindCanvasProperties();
    addCanvasListeners();

    initializeView();
    LOG.info("EditorGameView initialized with cell size {}", cellSize);
  }



  private void initializeView() {
    eventHandler.setupEventHandlers();
    eventHandler.setupZoom();
    eventHandler.setupPanning();
    drawer.drawGrid();
  }

  private void bindCanvasProperties() {
    gridCanvas.widthProperty().bind(widthProperty());
    gridCanvas.heightProperty().bind(heightProperty());
    objectCanvas.widthProperty().bind(widthProperty());
    objectCanvas.heightProperty().bind(heightProperty());
  }

  private void addCanvasListeners() {
    gridCanvas.widthProperty().addListener((obs, oldVal, newVal) -> drawer.drawGrid());
    gridCanvas.heightProperty().addListener((obs, oldVal, newVal) -> drawer.drawGrid());
    objectCanvas.widthProperty().addListener((obs, oldVal, newVal) -> drawer.redrawObjects());
    objectCanvas.heightProperty().addListener((obs, oldVal, newVal) -> drawer.redrawObjects());
  }

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
      // Delegate drawing config load to drawer (now guaranteed to be non-null)
      drawer.loadConfigurableValues(identifierProps);
    } catch (Exception e) {
      LOG.fatal(
          "Failed to load or parse one or more configuration values from identifier properties.",
          e);
      throw new RuntimeException("Failed to load essential view configuration.", e);
    }
  }

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



  public void updateCurrentTool(ObjectInteractionTool tool) {
    this.currentTool = tool;
    LOG.info("Current interaction tool set to: {}",
        (tool != null) ? tool.getClass().getSimpleName() : "None");
  }

  public BlueprintData getSelectedPrefab() {
    return prefabPalettePane.getSelectedPrefab();
  }

  public void refreshDisplay() {
    drawer.redrawObjects();
  }

  public void removeAllObjects() {
    displayedObjectIds.clear();
    imageManager.clearCache();
    drawer.redrawObjects();
  }



  @Override
  public void onObjectAdded(UUID objectId) {
    Platform.runLater(() -> {
      LOG.debug("EditorGameView received: onObjectAdded {}", objectId);
      if (!displayedObjectIds.contains(objectId)) {
        displayedObjectIds.add(objectId);
      }
      imageManager.preloadObjectImage(objectId);
      drawer.redrawObjects();
    });
  }

  @Override
  public void onObjectRemoved(UUID objectId) {
    Platform.runLater(() -> {
      LOG.debug("EditorGameView received: onObjectRemoved {}", objectId);
      if (displayedObjectIds.remove(objectId)) {
        imageManager.removeImage(objectId);
      }
      if (Objects.equals(selectedObjectId, objectId)) {
        selectedObjectId = null;
      }
      drawer.redrawObjects();
    });
  }

  @Override
  public void onObjectUpdated(UUID objectId) {
    Platform.runLater(() -> {
      LOG.debug("EditorGameView received: onObjectUpdated {}", objectId);
      if (displayedObjectIds.contains(objectId)) {
        imageManager.preloadObjectImage(objectId);
        drawer.redrawObjects();
      }
    });
  }

  @Override
  public void onSelectionChanged(UUID selectedObjectId) {
    Platform.runLater(() -> {
      LOG.debug("EditorGameView received: onSelectionChanged {}", selectedObjectId);
      if (!Objects.equals(this.selectedObjectId, selectedObjectId)) {
        this.selectedObjectId = selectedObjectId;
        drawer.redrawObjects();
      }
    });
  }

  @Override
  public void onDynamicVariablesChanged() {
    LOG.trace("EditorGameView received: onDynamicVariablesChanged (no direct action).");

  }

  @Override
  public void onErrorOccurred(String errorMessage) {
    LOG.warn("EditorGameView received: onErrorOccurred: {}", errorMessage);

  }

  @Override
  public void onPrefabsChanged() {
    LOG.debug("EditorGameView notified of prefab changes (no direct action taken).");
  }

  @Override
  public void onSpriteTemplateChanged() {
    LOG.debug("EditorGameView notified of sprite template changes.");

    imageManager.clearCache();
    displayedObjectIds.forEach(imageManager::preloadObjectImage);
    drawer.redrawObjects();
  }

  @Override
  public void setSnapToGrid(boolean doSnap) {
    this.snapToGrid = doSnap;

    LOG.debug("Snap to grid set to: {}", doSnap);
  }

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



  public void updateCameraCoordinates() {
    double canvasWidth = gridCanvas.getWidth();
    double canvasHeight = gridCanvas.getHeight();
    cornerCameraX = centerCameraX - (canvasWidth / (2.0 * zoomScale));
    cornerCameraY = centerCameraY - (canvasHeight / (2.0 * zoomScale));
  }

  public double[] screenToWorld(double screenX, double screenY) {
    updateCameraCoordinates();
    double worldX = cornerCameraX + (screenX / zoomScale);
    double worldY = cornerCameraY + (screenY / zoomScale);

    if (snapToGrid) {
      worldX = Math.floor(worldX / cellSize) * cellSize;
      worldY = Math.floor(worldY / cellSize) * cellSize;
    }
    return new double[]{worldX, worldY};
  }



  public String getId(String key) {
    String value = identifierProps.getProperty(key);
    if (value == null || value.trim().isEmpty()) {
      LOG.error("Missing identifier in properties file for key: {}", key);
      throw new RuntimeException("Missing identifier in properties file for key: " + key);
    }
    return value;
  }

  public ObjectInteractionTool getCurrentTool() {
    return currentTool;
  }

  public double getZoomScale() {
    return zoomScale;
  }

  public void setZoomScale(double zoomScale) {
    this.zoomScale = zoomScale;
  }

  public double getCenterCameraX() {
    return centerCameraX;
  }

  public void setCenterCameraX(double centerCameraX) {
    this.centerCameraX = centerCameraX;
  }

  public double getCenterCameraY() {
    return centerCameraY;
  }

  public void setCenterCameraY(double centerCameraY) {
    this.centerCameraY = centerCameraY;
  }

  public double getCornerCameraX() {
    updateCameraCoordinates();
    return cornerCameraX;
  }

  public double getCornerCameraY() {
    updateCameraCoordinates();
    return cornerCameraY;
  }

  public int getCellSize() {
    return cellSize;
  }

  public double getGridWidth() {
    return gridCanvas.getWidth();
  }

  public double getGridHeight() {
    return gridCanvas.getHeight();
  }

  public GraphicsContext getGridGraphicsContext() {
    return gridGraphicsContext;
  }

  public GraphicsContext getObjectGraphicsContext() {
    return objectGraphicsContext;
  }

  public List<UUID> getDisplayedObjectIds() {
    return displayedObjectIds;
  }

  public UUID getSelectedObjectId() {
    return selectedObjectId;
  }

  public boolean isDrawHitboxesEnabled() {
    return drawHitboxes;
  }

  public ResourceBundle getUiBundle() {
    return uiBundle;
  }

  public Color getGridBackgroundColor() {
    return gridBackgroundColor;
  }

  public Color getGridLineColor() {
    return gridLineColor;
  }

  public Color getGridHorizonColor() {
    return gridHorizonColor;
  }

  public double getGridLineWidth() {
    return gridLineWidth;
  }

  public double getGridHorizonWidth() {
    return gridHorizonWidth;
  }

  public double getZoomSpeed() {
    return zoomSpeed;
  }

  public double getMinZoom() {
    return minZoom;
  }

  public double getPanSpeed() {
    return panSpeed;
  }

  public int getGridMinBound() {
    return gridMinBound;
  }

  public int getGridMaxBound() {
    return gridMaxBound;
  }

  public Canvas getObjectCanvas() {
    return objectCanvas;
  }

  public boolean isSnapToGridEnabled() {
    return snapToGrid;
  }
}