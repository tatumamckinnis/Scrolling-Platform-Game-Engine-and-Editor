package oogasalad.editor.view;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import oogasalad.editor.controller.EditorDataAPI;
import oogasalad.editor.model.data.EditorObject;
import oogasalad.editor.model.data.object_data.SpriteData;
import oogasalad.editor.view.tools.ObjectPlacementTool;

/**
 * Display a grid where visual game elements can be added and updated.
 * This component implements a configurable grid-based editor view for game designers.
 * @author Tatum McKinnis
 */
public class EditorGameView extends Pane {
  private Canvas gridCanvas;
  private Canvas objectCanvas;
  private final int width;
  private final int height;
  private final int cellSize;
  private GraphicsContext gridGC;
  private GraphicsContext objectGC;
  private EditorDataAPI editorDataAPI;
  private EditorAppAPI editorAppAPI;
  private List<UUID> displayedObjects = new ArrayList<>();
  private Map<UUID, Image> objectImages = new HashMap<>();
  private ObjectPlacementTool currentTool;

  /**
   * Creates a new editor game view with the specified dimensions.
   *
   * @param width Width of the game area in pixels
   * @param height Height of the game area in pixels
   * @param cellSize Size of each grid cell in pixels
   * @param editorDataAPI API for editor data access
   */
  public EditorGameView(int width, int height, int cellSize, EditorDataAPI editorDataAPI, EditorAppAPI editorAppAPI) {
    this.width = width;
    this.height = height;
    this.cellSize = cellSize;
    this.editorDataAPI = editorDataAPI;
    this.editorAppAPI = editorAppAPI;

    initializeCanvases();
    drawGrid();
    setupEventHandlers();
  }

  /**
   * Initializes the canvas layers for grid and game objects
   */
  private void initializeCanvases() {
    gridCanvas = new Canvas(width, height);
    objectCanvas = new Canvas(width, height);

    gridGC = gridCanvas.getGraphicsContext2D();
    objectGC = objectCanvas.getGraphicsContext2D();

    getChildren().addAll(gridCanvas, objectCanvas);
  }

  /**
   * Draws the grid on the grid canvas
   */
  public void drawGrid() {
    gridGC.setFill(Color.WHITE);
    gridGC.fillRect(0, 0, width, height);

    gridGC.setStroke(Color.LIGHTGRAY);
    gridGC.setLineWidth(1.0);

    for (int x = 0; x <= width; x += cellSize) {
      gridGC.strokeLine(x, 0, x, height);
    }

    for (int y = 0; y <= height; y += cellSize) {
      gridGC.strokeLine(0, y, width, y);
    }

    int horizonY = (int)(height * 0.8);
    gridGC.setStroke(Color.DARKGRAY);
    gridGC.setLineWidth(2.0);
    gridGC.strokeLine(0, horizonY, width, horizonY);
  }

  /**
   * Sets up mouse event handlers for the editor view
   */
  private void setupEventHandlers() {
    objectCanvas.setOnMouseClicked(this::handleGridClick);
  }

  /**
   * Handles a click on the grid
   *
   * @param event The mouse click event
   */
  private void handleGridClick(MouseEvent event) {
    if (currentTool != null) {
      int gridX = (int)(event.getX() / cellSize);
      int gridY = (int)(event.getY() / cellSize);

      currentTool.placeObjectAt(gridX, gridY);
    }
  }

  /**
   * Sets the current object placement tool
   *
   * @param tool The tool to use for placing objects
   */
  public void setCurrentTool(ObjectPlacementTool tool) {
    this.currentTool = tool;
  }

  /**
   * Adds an object to the view
   *
   * @param id The UUID of the object
   */
  public void addObject(UUID id) {
    displayedObjects.add(id);
    editorAppAPI.setCurrentObjectID(id);
    EditorObject object = editorDataAPI.getEditorObject(id);

    SpriteData spriteData = object.getSpriteData();
    if (spriteData != null && spriteData.getSpritePath() != null) {
      try {
        Image image = new Image(spriteData.getSpritePath());
        objectImages.put(id, image);
      } catch (Exception e) {
        System.err.println("Failed to load image: " + e.getMessage());
      }
    }

    redrawObjects();
  }

  /**
   * Removes an object from the view
   *
   * @param id The UUID of the object to remove
   */
  public void removeObject(UUID id) {
    displayedObjects.remove(id);
    objectImages.remove(id);
    redrawObjects();
  }

  /**
   * Redraws all objects on the object canvas
   */
  private void redrawObjects() {
    objectGC.clearRect(0, 0, width, height);

    for (UUID entry : displayedObjects) {
      UUID id = entry;
      EditorObject object = editorDataAPI.getEditorObject(id);

      SpriteData spriteData = object.getSpriteData();
      if (spriteData == null) continue;

      double x = spriteData.getX();
      double y = spriteData.getY();

      if (objectImages.containsKey(id)) {
        Image image = objectImages.get(id);
        objectGC.drawImage(image, x, y, cellSize, cellSize);
      } else {
        objectGC.setFill(Color.LIGHTBLUE);
        objectGC.fillRect(x, y, cellSize, cellSize);

        objectGC.setFill(Color.BLACK);
        objectGC.setFont(new Font(10));
        objectGC.setTextAlign(TextAlignment.CENTER);
        String type = object.getIdentityData().getGroup();
        objectGC.fillText(type, x + cellSize/2, y + cellSize/2);
      }
    }
  }

  /**
   * Gets the cell size used by this grid
   *
   * @return The cell size in pixels
   */
  public int getCellSize() {
    return cellSize;
  }

  /**
   * Gets the width of the game area
   *
   * @return The width in pixels
   */
  public int getGridWidth() {
    return width;
  }

  /**
   * Gets the height of the game area
   *
   * @return The height in pixels
   */
  public int getGridHeight() {
    return height;
  }
}