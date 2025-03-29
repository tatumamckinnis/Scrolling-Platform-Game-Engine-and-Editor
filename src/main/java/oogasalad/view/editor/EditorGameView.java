package main.java.oogasalad.view.editor;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

/**
 * Display a grid where visual game elements can be added and updated.
 * This component implements a configurable grid-based editor view for game designers.
 * Can be configured for different types of games through strategy objects rather than subclassing.
 *
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

  /**
   * Creates a new editor game view with the specified dimensions.
   *
   * @param width Width of the game area in pixels
   * @param height Height of the game area in pixels
   * @param cellSize Size of each grid cell in pixels
   */
  public EditorGameView(int width, int height, int cellSize) {
    this.width = width;
    this.height = height;
    this.cellSize = cellSize;

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
   * Can be customized with different grid styles
   */
  public void drawGrid() {
    gridGC.setFill(Color.WHITE); // Or any background color
    gridGC.fillRect(0, 0, width, height);

    // Optionally draw a horizontl line or other game-specific background elements
    gridGC.setStroke(Color.LIGHTGRAY);
    gridGC.setLineWidth(1.0);
    int horizonY = (int)(height * 0.7);
    gridGC.strokeLine(0, horizonY, width, horizonY);
  }

  /**
   * Sets up mouse event handlers for the editor view
   */
  private void setupEventHandlers() {
    objectCanvas.setOnMouseClicked(e -> {
      int gridX = (int)(e.getX() / cellSize);
      int gridY = (int)(e.getY() / cellSize);
      handleGridClick(gridX, gridY);
    });
  }

  /**
   * Handles a click on the grid
   * Default implementation that can be overridden by setting a custom handler
   *
   * @param gridX The x-coordinate of the clicked cell
   * @param gridY The y-coordinate of the clicked cell
   */
  protected void handleGridClick(int gridX, int gridY) {
    System.out.println("Clicked at grid position: " + gridX + ", " + gridY);
  }

  /**
   * Gets the GraphicsContext for the object canvas
   *
   * @return The graphics context used for drawing objects
   */
  public GraphicsContext getObjectGraphicsContext() {
    return objectGC;
  }

  /**
   * Gets the GraphicsContext for the grid canvas
   *
   * @return The graphics context used for drawing the grid
   */
  public GraphicsContext getGridGraphicsContext() {
    return gridGC;
  }

  /**
   * Clears all objects from the canvas
   */
  public void clearObjects() {
    objectGC.clearRect(0, 0, width, height);
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

  /**
   * Sets a new mouse click handler for the grid
   *
   * @param handler The event handler to set
   */
  public void setOnGridClick(javafx.event.EventHandler<javafx.scene.input.MouseEvent> handler) {
    objectCanvas.setOnMouseClicked(handler);
  }
}