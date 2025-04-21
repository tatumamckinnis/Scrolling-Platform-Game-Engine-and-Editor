package oogasalad.editor.view.panes.spriteCreation;

import java.io.File;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import oogasalad.editor.controller.EditorController;

/**
 * Pane for slicing a sprite-sheet in TILE_SIZE, COLS_ROWS, or MANUAL modes.
 * <p>
 * Users can load an image, adjust slicing parameters, preview cut lines, rename regions, and save
 * an XML atlas.
 * </p>
 *
 * @author Jacob You
 */
public class SpriteSheetProcessorPane extends BorderPane {

  // Magic values extracted as constants
  private static final int DEFAULT_TILE_WIDTH = 32;
  private static final int DEFAULT_TILE_HEIGHT = 32;
  private static final int DEFAULT_COLS = 4;
  private static final int DEFAULT_ROWS = 4;
  private static final int DEFAULT_MANUAL_X = 0;
  private static final int DEFAULT_MANUAL_Y = 0;
  private static final int DEFAULT_MANUAL_WIDTH = 32;
  private static final int DEFAULT_MANUAL_HEIGHT = 32;

  private static final double TEXT_FIELD_PREF_WIDTH = 60.0;
  private static final double CONTROL_HGAP = 8.0;
  private static final double CONTROL_VGAP = 4.0;
  private static final double SUBPANE_HGAP = 6.0;
  private static final double TABLE_PREF_HEIGHT = 170.0;
  private static final double NAME_COLUMN_PREF_WIDTH = 140.0;
  private static final double NUM_COLUMN_PREF_WIDTH = 50.0;
  private static final double DIALOG_WIDTH = 900.0;
  private static final double DIALOG_HEIGHT = 600.0;
  private static final Color SPRITESHEET_GRID_COLOR = Color.rgb(255, 0, 0, 0.8);
  private static final Color SPRITESHEET_SELECTED_AREA_COLOR = Color.rgb(0, 200, 0, 0.8);

  enum SpriteSheetMode {TILE_SIZE, COLS_ROWS, MANUAL}

  private interface RegionStrategy {

    void compute(Image img);
  }

  private final Map<SpriteSheetMode, RegionStrategy> strategies = new EnumMap<>(
      SpriteSheetMode.class);

  private final List<SpriteRegion> regions = FXCollections.observableArrayList();

  private final ImageView sheetView = new ImageView();
  private final Canvas overlay = new Canvas();
  private final TextField tileWidth = intField(DEFAULT_TILE_WIDTH);
  private final TextField tileHeight = intField(DEFAULT_TILE_HEIGHT);
  private final TextField numCols = intField(DEFAULT_COLS);
  private final TextField numRows = intField(DEFAULT_ROWS);
  private final TextField manualX = intField(DEFAULT_MANUAL_X);
  private final TextField manualY = intField(DEFAULT_MANUAL_Y);
  private final TextField manualWidth = intField(DEFAULT_MANUAL_WIDTH);
  private final TextField manualHeight = intField(DEFAULT_MANUAL_HEIGHT);
  private final ChoiceBox<SpriteSheetMode> modeBox = new ChoiceBox<>();
  private final Button loadSheet = new Button("Load Sheet");
  private final Button save = new Button("Save");
  private final Button addManual = new Button("Add Box");
  private final TableView<SpriteRegion> table = new TableView<>();

  private final GridPane tilePane = new GridPane();
  private final GridPane colsRowsPane = new GridPane();
  private final GridPane manualPane = new GridPane();

  private final EditorController controller;

  /**
   * Constructs the sprite-sheet processor pane and wires up its UI and logic.
   *
   * @param editorController the editorController for the current editor
   * @param owner            the owner window for file chooser dialogs
   */
  public SpriteSheetProcessorPane(EditorController editorController, Window owner) {
    this.controller = editorController;
    initStrategies();
    buildUI(owner);
    wireEvents(owner);
    recomputeRegions();
  }


  /**
   * Populates modeBox and registers slicing strategies.
   */
  private void initStrategies() {
    modeBox.getItems().addAll(
        SpriteSheetMode.TILE_SIZE,
        SpriteSheetMode.COLS_ROWS,
        SpriteSheetMode.MANUAL);

    modeBox.setValue(SpriteSheetMode.TILE_SIZE);

    strategies.put(SpriteSheetMode.TILE_SIZE, this::tileSizeStrategy);
    strategies.put(SpriteSheetMode.COLS_ROWS, this::colsRowsStrategy);
  }

  /**
   * Builds the scene‑graph (controls pane, image stack, table) and hooks them together.
   */
  private void buildUI(Window owner) {
    GridPane controls = buildControlPanel();
    updateVisibleParamPane();

    StackPane sheetStack = new StackPane(sheetView, overlay);
    ScrollPane sheetScroll = new ScrollPane(sheetStack);
    sheetScroll.setFitToWidth(true);
    sheetScroll.setFitToHeight(true);

    configureTable();

    VBox middle = new VBox(sheetScroll, table);
    VBox.setVgrow(sheetScroll, Priority.ALWAYS);

    setTop(controls);
    setCenter(middle);
    setPadding(new Insets(10));
  }

  /**
   * Connects all event‑handlers and *live* listeners.
   */
  private void wireEvents(Window owner) {
    loadSheet.setOnAction(e -> openSheet(owner));
    save.setOnAction(e -> saveAtlas());
    addManual.setOnAction(e -> addManualRegion());

    modeBox.valueProperty().addListener((o, ov, nv) -> {
      regions.clear();
      updateVisibleParamPane();
      recomputeRegions();
    });

    List.of(tileWidth, tileHeight, numCols, numRows, manualX, manualY, manualWidth, manualHeight)
        .forEach(this::attachLive);
  }

  /**
   * Helper to create an integer-only TextField with a default value.
   */
  private static TextField intField(int init) {
    TextField tf = new TextField(String.valueOf(init));
    tf.setPrefWidth(TEXT_FIELD_PREF_WIDTH);
    tf.setTextFormatter(new TextFormatter<Integer>(
        c -> c.getControlNewText().matches("\\d*") ? c : null));
    return tf;
  }

  /**
   * Attaches listeners so changes to a field immediately recompute regions.
   */
  private void attachLive(TextField tf) {
    tf.textProperty().addListener((o, ov, nv) -> recomputeRegions());
    tf.setOnAction(e -> recomputeRegions());
  }

  /**
   * Opens an image file and displays it for slicing.
   */
  private void openSheet(Window owner) {
    FileChooser fc = new FileChooser();
    fc.getExtensionFilters().add(
        new FileChooser.ExtensionFilter("Image files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
    File imgFile = fc.showOpenDialog(owner);
    if (imgFile == null) {
      return;
    }
    Image img = new Image(imgFile.toURI().toString());
    sheetView.setImage(img);
    overlay.setWidth(img.getWidth());
    overlay.setHeight(img.getHeight());
    regions.clear();
    recomputeRegions();
  }

  /**
   * Saves the current regions as an XML sprite atlas.
   */
  private void saveAtlas() {
    if (sheetView.getImage() == null || regions.isEmpty()) {
      return;
    }
    FileChooser fc = new FileChooser();
    fc.getExtensionFilters().add(
        new FileChooser.ExtensionFilter("Sprite atlas (*.xml)", "*.xml"));
    File dest = fc.showSaveDialog(getScene().getWindow());
    if (dest == null) {
      return;
    }
    try {
      controller.getEditorDataAPI().getSpriteSheetDataAPI().saveSpriteSheet(
          sheetView.getImage().getUrl(),
          (int) sheetView.getImage().getWidth(),
          (int) sheetView.getImage().getHeight(),
          regions,
          dest
      );
    } catch (Exception ex) {
      new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
    }
  }

  /**
   * Rebuilds {@code regions} for automatic modes and repaints the overlay. Manual mode only
   * repaints guides, preserving user‑added rectangles.
   */
  private void recomputeRegions() {
    Image img = sheetView.getImage();
    if (img == null) {
      redrawOverlay();
      return;
    }

    if (modeBox.getValue() == SpriteSheetMode.MANUAL) {
      redrawOverlay();
      return;
    }

    regions.clear();
    strategies.get(modeBox.getValue()).compute(img);

    table.refresh();
    redrawOverlay();
  }

  /**
   * Adds a manually defined region from the input fields.
   */
  private void addManualRegion() {
    if (modeBox.getValue() != SpriteSheetMode.MANUAL) {
      return;
    }
    Image img = sheetView.getImage();
    if (img == null) {
      return;
    }
    try {
      int x = parse(manualX), y = parse(manualY), w = parse(manualWidth), h = parse(manualHeight);
      double imgW = img.getWidth(), imgH = img.getHeight();
      if (x >= imgW || y >= imgH) {
        return;
      }
      int ww = (int) Math.max(1, Math.min(w, imgW - x));
      int hh = (int) Math.max(1, Math.min(h, imgH - y));
      regions.add(new SpriteRegion("sprite_" + regions.size(), new Rectangle2D(x, y, ww, hh)));
      table.refresh();
      redrawOverlay();
    } catch (NumberFormatException ignored) {
    }
  }

  /**
   * TILE_SIZE strategy: fixed‑size tiles that clamp on image borders, all in integer pixels.
   */
  private void tileSizeStrategy(Image img) {
    int imgW = (int) img.getWidth();
    int imgH = (int) img.getHeight();
    if (tileWidth.getText().isEmpty() || tileHeight.getText().isEmpty()) {
      return;
    }
    int w = parse(tileWidth);
    int h = parse(tileHeight);
    if (w <= 0 || h <= 0) {
      return;
    }

    for (int y = 0, r = 0; y < imgH; y += h, r++) {
      for (int x = 0, c = 0; x < imgW; x += w, c++) {
        int ww = Math.min(w, imgW - x);
        int hh = Math.min(h, imgH - y);
        if (ww < 1 || hh < 1) {
          continue;
        }
        regions.add(new SpriteRegion(
            "r" + r + "_c" + c,
            new Rectangle2D(x, y, ww, hh)
        ));
      }
    }
  }

  /**
   * COLS_ROWS strategy: divide sheet into an equal grid of integer columns/rows, last row/column
   * takes the remainder.
   */
  private void colsRowsStrategy(Image img) {
    int imgW = (int) img.getWidth();
    int imgH = (int) img.getHeight();
    if (numCols.getText().isEmpty() || numRows.getText().isEmpty()) {
      return;
    }
    int cols = parse(numCols);
    int rows = parse(numRows);
    if (cols <= 0 || rows <= 0) {
      return;
    }

    int cellW = imgW / cols;
    int cellH = imgH / rows;

    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) {
        int x = c * cellW;
        int y = r * cellH;
        int ww = (c == cols - 1) ? imgW - x : cellW;
        int hh = (r == rows - 1) ? imgH - y : cellH;
        if (ww < 1 || hh < 1) {
          continue;
        }
        regions.add(new SpriteRegion(
            "r" + r + "_c" + c,
            new Rectangle2D(x, y, ww, hh)
        ));
      }
    }
  }

  /**
   * Adds a region whose bounds are clamped so the rectangle never exceeds the image. Width / height
   * are reduced to fit and never fall below 1 pixel.
   */
  private void addClampedRegion(String name,
      double x, double y,
      double w, double h,
      double imgW, double imgH) {
    double ww = Math.max(1, Math.min(w, imgW - x));
    double hh = Math.max(1, Math.min(h, imgH - y));
    if (ww >= 1 && hh >= 1) {
      regions.add(new SpriteRegion(name, new Rectangle2D(x, y, ww, hh)));
    }
  }

  /**
   * Parses the text of a TextField as an integer.
   */
  private int parse(TextField tf) {
    return Integer.parseInt(tf.getText());
  }

  /**
   * Draws all region rectangles on the overlay canvas.
   */
  private void redrawOverlay() {
    GraphicsContext g = overlay.getGraphicsContext2D();
    g.clearRect(0, 0, overlay.getWidth(), overlay.getHeight());

    /* draw confirmed regions (red) */
    g.setStroke(SPRITESHEET_GRID_COLOR);
    g.setLineWidth(1);
    regions.forEach(r -> {
      Rectangle2D b = r.getBounds();
      g.strokeRect(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
    });

    /* live preview for MANUAL mode (green dashed) */
    if (modeBox.getValue() == SpriteSheetMode.MANUAL && sheetView.getImage() != null) {
      try {
        int x = parse(manualX), y = parse(manualY), w = parse(manualWidth), h = parse(manualHeight);
        double imgW = sheetView.getImage().getWidth();
        double imgH = sheetView.getImage().getHeight();
        w = (int) Math.max(1, Math.min(w, imgW - x));
        h = (int) Math.max(1, Math.min(h, imgH - y));

        g.setStroke(SPRITESHEET_SELECTED_AREA_COLOR);
        g.strokeRect(x, y, w, h);
      } catch (NumberFormatException ignored) { /* user still typing */ }
    }
  }

  /**
   * Builds the top control panel containing load/save buttons and parameter panes.
   */
  private GridPane buildControlPanel() {
    GridPane root = new GridPane();
    root.setHgap(CONTROL_HGAP);
    root.setVgap(CONTROL_VGAP);

    addLoadSaveControls(root);
    addParameterPanes(root);

    return root;
  }

  /**
   * Adds the load, mode selection, and save controls to the given grid.
   */
  private void addLoadSaveControls(GridPane root) {
    root.add(loadSheet, 0, 0);
    root.add(new Label("SpriteSheetMode:"), 1, 0);
    root.add(modeBox, 2, 0);
    root.add(save, 3, 0);
  }

  /**
   * Adds and configures the three parameter panes (tile size, cols/rows, manual) to the given
   * grid.
   */
  private void addParameterPanes(GridPane root) {
    configureTilePane();
    configureColsRowsPane();
    configureManualPane();

    root.add(tilePane, 0, 1, 4, 1);
    root.add(colsRowsPane, 0, 1, 4, 1);
    root.add(manualPane, 0, 1, 4, 1);
  }

  /**
   * Configures the tile‑size parameter pane.
   */
  private void configureTilePane() {
    tilePane.setHgap(SUBPANE_HGAP);
    tilePane.getChildren().clear();
    tilePane.add(new Label("Tile W:"), 0, 0);
    tilePane.add(tileWidth, 1, 0);
    tilePane.add(new Label("Tile H:"), 2, 0);
    tilePane.add(tileHeight, 3, 0);
  }

  /**
   * Configures the columns/rows parameter pane.
   */
  private void configureColsRowsPane() {
    colsRowsPane.setHgap(SUBPANE_HGAP);
    colsRowsPane.getChildren().clear();
    colsRowsPane.add(new Label("#Cols:"), 0, 0);
    colsRowsPane.add(numCols, 1, 0);
    colsRowsPane.add(new Label("#Rows:"), 2, 0);
    colsRowsPane.add(numRows, 3, 0);
  }

  /**
   * Configures the manual‑entry parameter pane.
   */
  private void configureManualPane() {
    manualPane.setHgap(SUBPANE_HGAP);
    manualPane.getChildren().clear();
    manualPane.add(new Label("X:"), 0, 0);
    manualPane.add(manualX, 1, 0);
    manualPane.add(new Label("Y:"), 2, 0);
    manualPane.add(manualY, 3, 0);
    manualPane.add(new Label("Width:"), 0, 1);
    manualPane.add(manualWidth, 1, 1);
    manualPane.add(new Label("Height:"), 2, 1);
    manualPane.add(manualHeight, 3, 1);
    manualPane.add(addManual, 0, 2);
  }

  /**
   * Updates which parameter pane is visible based on the selected mode.
   */
  private void updateVisibleParamPane() {
    tilePane.setVisible(modeBox.getValue() == SpriteSheetMode.TILE_SIZE);
    tilePane.setManaged(tilePane.isVisible());
    colsRowsPane.setVisible(modeBox.getValue() == SpriteSheetMode.COLS_ROWS);
    colsRowsPane.setManaged(colsRowsPane.isVisible());
    manualPane.setVisible(modeBox.getValue() == SpriteSheetMode.MANUAL);
    manualPane.setManaged(manualPane.isVisible());
  }

  /**
   * Configures the regions table with editable name and numeric columns.
   */
  private void configureTable() {
    table.setEditable(true);
    table.setPrefHeight(TABLE_PREF_HEIGHT);

    TableColumn<SpriteRegion, String> nameCol = new TableColumn<>("Name");
    nameCol.setPrefWidth(NAME_COLUMN_PREF_WIDTH);
    nameCol.setCellValueFactory(v -> new SimpleStringProperty(v.getValue().getName()));
    nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
    nameCol.setOnEditCommit(e -> e.getRowValue().setName(e.getNewValue()));

    TableColumn<SpriteRegion, Number> xCol = numCol("X", NUM_COLUMN_PREF_WIDTH, SpriteRegion::getX);
    TableColumn<SpriteRegion, Number> yCol = numCol("Y", NUM_COLUMN_PREF_WIDTH, SpriteRegion::getY);
    TableColumn<SpriteRegion, Number> wCol = numCol("Width", NUM_COLUMN_PREF_WIDTH,
        SpriteRegion::getWidth);
    TableColumn<SpriteRegion, Number> hCol = numCol("Height", NUM_COLUMN_PREF_WIDTH,
        SpriteRegion::getHeight);

    table.getColumns().addAll(nameCol, xCol, yCol, wCol, hCol);
    table.setItems((ObservableList<SpriteRegion>) regions);
  }

  /**
   * Helper to build a numeric column given a getter.
   */
  private TableColumn<SpriteRegion, Number> numCol(
      String title, double width, ToDoubleFunction<SpriteRegion> getter) {
    TableColumn<SpriteRegion, Number> col = new TableColumn<>(title);
    col.setPrefWidth(width);
    col.setCellValueFactory(v ->
        new SimpleIntegerProperty((int) getter.applyAsDouble(v.getValue())));
    return col;
  }

  /**
   * Shows this pane in a standalone dialog.
   *
   * @param owner the owner window for modality
   */
  public static void show(EditorController controller, Window owner) {
    Stage st = new Stage();
    st.setTitle("Sprite‑Sheet Processor");
    st.initOwner(owner);
    st.setScene(new Scene(new SpriteSheetProcessorPane(controller, owner), DIALOG_WIDTH, DIALOG_HEIGHT));
    st.show();
  }
}
