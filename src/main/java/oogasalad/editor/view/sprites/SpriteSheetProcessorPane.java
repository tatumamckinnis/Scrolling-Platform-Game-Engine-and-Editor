package oogasalad.editor.view.sprites;

import java.io.File;
import java.util.function.ToDoubleFunction;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Pane for slicing a sprite-sheet in TILE_SIZE, COLS_ROWS, or MANUAL modes.
 * <p>
 * Users can load an image, adjust slicing parameters, preview cut lines,
 * rename regions, and save an XML atlas.
 * </p>
 * @author Jacob You
 */
public class SpriteSheetProcessorPane extends BorderPane {

  // Magic values extracted as constants
  private static final int DEFAULT_TILE_WIDTH       = 32;
  private static final int DEFAULT_TILE_HEIGHT      = 32;
  private static final int DEFAULT_COLS             = 4;
  private static final int DEFAULT_ROWS             = 4;
  private static final int DEFAULT_MANUAL_X         = 0;
  private static final int DEFAULT_MANUAL_Y         = 0;
  private static final int DEFAULT_MANUAL_WIDTH     = 32;
  private static final int DEFAULT_MANUAL_HEIGHT    = 32;

  private static final double OVERLAY_STROKE_OPACITY    = 0.55;
  private static final double OVERLAY_STROKE_WIDTH      = 1.0;
  private static final double TEXT_FIELD_PREF_WIDTH     = 60.0;
  private static final double CONTROL_HGAP              = 8.0;
  private static final double CONTROL_VGAP              = 4.0;
  private static final double SUBPANE_HGAP              = 6.0;
  private static final double TABLE_PREF_HEIGHT         = 170.0;
  private static final double NAME_COLUMN_PREF_WIDTH    = 140.0;
  private static final double NUM_COLUMN_PREF_WIDTH     = 50.0;
  private static final double DIALOG_WIDTH              = 900.0;
  private static final double DIALOG_HEIGHT             = 600.0;

  private enum Mode { TILE_SIZE, COLS_ROWS, MANUAL }

  // Holds the computed sprite regions
  private final ObservableList<SpriteRegion> regions = FXCollections.observableArrayList();

  // UI controls
  private final ImageView sheetView   = new ImageView();
  private final Canvas    overlay     = new Canvas();
  private final TextField tileW       = intField(DEFAULT_TILE_WIDTH);
  private final TextField tileH       = intField(DEFAULT_TILE_HEIGHT);
  private final TextField numCols     = intField(DEFAULT_COLS);
  private final TextField numRows     = intField(DEFAULT_ROWS);
  private final TextField manX        = intField(DEFAULT_MANUAL_X);
  private final TextField manY        = intField(DEFAULT_MANUAL_Y);
  private final TextField manW        = intField(DEFAULT_MANUAL_WIDTH);
  private final TextField manH        = intField(DEFAULT_MANUAL_HEIGHT);
  private final ChoiceBox<Mode> modeBox = new ChoiceBox<>();
  private final Button loadSheet       = new Button("Load Sheet");
  private final Button save            = new Button("Save");
  private final Button addManual       = new Button("Add Box");
  private final TableView<SpriteRegion> table = new TableView<>();

  private final GridPane tilePane     = new GridPane();
  private final GridPane colsRowsPane = new GridPane();
  private final GridPane manualPane   = new GridPane();

  /**
   * Constructs the sprite-sheet processor pane and wires up its UI and logic.
   *
   * @param owner the owner window for file chooser dialogs
   */
  public SpriteSheetProcessorPane(Window owner) {
    // Mode selection
    modeBox.getItems().addAll(Mode.TILE_SIZE, Mode.COLS_ROWS, Mode.MANUAL);
    modeBox.setValue(Mode.TILE_SIZE);

    // Top control panel
    GridPane controls = buildControlPanel();
    updateVisibleParamPane();

    // Image + overlay
    StackPane sheetStack = new StackPane(sheetView, overlay);
    ScrollPane sheetScroll = new ScrollPane(sheetStack);
    sheetScroll.setFitToWidth(true);
    sheetScroll.setFitToHeight(true);

    // Table of regions
    configureTable();

    // Layout center
    VBox middle = new VBox(sheetScroll, table);
    VBox.setVgrow(sheetScroll, Priority.ALWAYS);

    setTop(controls);
    setCenter(middle);
    setPadding(new Insets(10));

    // Event wiring
    loadSheet.setOnAction(e -> openSheet(owner));
    save.setOnAction(e -> saveAtlas());
    addManual.setOnAction(e -> addManualRegion());
    modeBox.valueProperty().addListener((o, ov, nv) -> {
      regions.clear();
      updateVisibleParamPane();
      recomputeRegions();
    });

    // Make parameter fields live
    attachLive(tileW);
    attachLive(tileH);
    attachLive(numCols);
    attachLive(numRows);
    attachLive(manX);
    attachLive(manY);
    attachLive(manW);
    attachLive(manH);
  }

  /** Helper to create an integer-only TextField with a default value. */
  private static TextField intField(int init) {
    TextField tf = new TextField(String.valueOf(init));
    tf.setPrefWidth(TEXT_FIELD_PREF_WIDTH);
    tf.setTextFormatter(new TextFormatter<Integer>(
        c -> c.getControlNewText().matches("\\d*") ? c : null));
    return tf;
  }

  /** Attaches listeners so changes to a field immediately recompute regions. */
  private void attachLive(TextField tf) {
    tf.textProperty().addListener((o, ov, nv) -> recomputeRegions());
    tf.setOnAction(e -> recomputeRegions());
  }

  /** Opens an image file and displays it for slicing. */
  private void openSheet(Window owner) {
    FileChooser fc = new FileChooser();
    fc.getExtensionFilters().add(
        new FileChooser.ExtensionFilter("Image files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
    File imgFile = fc.showOpenDialog(owner);
    if (imgFile == null) return;
    Image img = new Image(imgFile.toURI().toString());
    sheetView.setImage(img);
    overlay.setWidth(img.getWidth());
    overlay.setHeight(img.getHeight());
    regions.clear();
    recomputeRegions();
  }

  /** Saves the current regions as an XML sprite atlas. */
  private void saveAtlas() {
    if (sheetView.getImage() == null || regions.isEmpty()) return;
    FileChooser fc = new FileChooser();
    fc.getExtensionFilters().add(
        new FileChooser.ExtensionFilter("Sprite atlas (*.xml)", "*.xml"));
    File dest = fc.showSaveDialog(getScene().getWindow());
    if (dest == null) return;
    try {
      SpriteSheetSaver.save(sheetView.getImage(), regions, dest);
    } catch (Exception ex) {
      new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
    }
  }

  /** Recomputes regions based on the selected mode and parameters. */
  private void recomputeRegions() {
    Image img = sheetView.getImage();
    if (img == null) { redrawOverlay(); return; }
    double imgW = img.getWidth(), imgH = img.getHeight();

    if (modeBox.getValue() == Mode.MANUAL) {
      redrawOverlay();
      return;
    }

    regions.clear();
    try {
      if (modeBox.getValue() == Mode.TILE_SIZE) {
        int w = parse(tileW), h = parse(tileH);
        if (w <= 0 || h <= 0) { redrawOverlay(); return; }
        for (int y = 0, r = 0; y < imgH; y += h, r++) {
          for (int x = 0, c = 0; x < imgW; x += w, c++) {
            int ww = (int)Math.min(w, imgW - x);
            int hh = (int)Math.min(h, imgH - y);
            if (ww < 1 || hh < 1) continue;
            regions.add(new SpriteRegion("r"+r+"_c"+c, new Rectangle2D(x, y, ww, hh)));
          }
        }
      } else {
        int cols = parse(numCols), rows = parse(numRows);
        if (cols <= 0 || rows <= 0) { redrawOverlay(); return; }
        double w = imgW/cols, h = imgH/rows;
        for (int r = 0; r < rows; r++) {
          for (int c = 0; c < cols; c++) {
            double x = c*w, y = r*h;
            double ww = Math.max(1, Math.min(w, imgW - x));
            double hh = Math.max(1, Math.min(h, imgH - y));
            regions.add(new SpriteRegion("r"+r+"_c"+c, new Rectangle2D(x, y, ww, hh)));
          }
        }
      }
    } catch (NumberFormatException ignored) { }

    table.refresh();
    redrawOverlay();
  }

  /** Adds a manually defined region from the input fields. */
  private void addManualRegion() {
    if (modeBox.getValue() != Mode.MANUAL) return;
    Image img = sheetView.getImage();
    if (img == null) return;
    try {
      int x = parse(manX), y = parse(manY), w = parse(manW), h = parse(manH);
      double imgW = img.getWidth(), imgH = img.getHeight();
      if (x >= imgW || y >= imgH) return;
      int ww = (int)Math.max(1, Math.min(w, imgW - x));
      int hh = (int)Math.max(1, Math.min(h, imgH - y));
      regions.add(new SpriteRegion("sprite_"+regions.size(), new Rectangle2D(x, y, ww, hh)));
      table.refresh();
      redrawOverlay();
    } catch (NumberFormatException ignored) { }
  }

  /** Parses the text of a TextField as an integer. */
  private int parse(TextField tf) {
    return Integer.parseInt(tf.getText());
  }

  /** Draws all region rectangles on the overlay canvas. */
  private void redrawOverlay() {
    GraphicsContext g = overlay.getGraphicsContext2D();
    g.clearRect(0, 0, overlay.getWidth(), overlay.getHeight());
    g.setStroke(Color.rgb(255, 0, 0, OVERLAY_STROKE_OPACITY));
    g.setLineWidth(OVERLAY_STROKE_WIDTH);
    regions.forEach(r -> {
      Rectangle2D b = r.getBounds();
      g.strokeRect(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
    });
  }

  /** Builds the top control panel containing load/save buttons and parameter panes. */
  private GridPane buildControlPanel() {
    GridPane root = new GridPane();
    root.setHgap(CONTROL_HGAP);
    root.setVgap(CONTROL_VGAP);

    root.add(loadSheet, 0, 0);
    root.add(new Label("Mode:"), 1, 0);
    root.add(modeBox, 2, 0);
    root.add(save, 3, 0);

    tilePane.setHgap(SUBPANE_HGAP);
    tilePane.add(new Label("Tile W:"), 0, 0); tilePane.add(tileW, 1, 0);
    tilePane.add(new Label("Tile H:"), 2, 0); tilePane.add(tileH, 3, 0);

    colsRowsPane.setHgap(SUBPANE_HGAP);
    colsRowsPane.add(new Label("#Cols:"), 0, 0); colsRowsPane.add(numCols, 1, 0);
    colsRowsPane.add(new Label("#Rows:"), 2, 0); colsRowsPane.add(numRows, 3, 0);

    manualPane.setHgap(SUBPANE_HGAP);
    manualPane.add(new Label("X:"), 0, 0); manualPane.add(manX, 1, 0);
    manualPane.add(new Label("Y:"), 2, 0); manualPane.add(manY, 3, 0);
    manualPane.add(new Label("W:"), 0, 1); manualPane.add(manW, 1, 1);
    manualPane.add(new Label("H:"), 2, 1); manualPane.add(manH, 3, 1);
    manualPane.add(addManual, 0, 2);

    root.add(tilePane,     0, 1, 4, 1);
    root.add(colsRowsPane, 0, 1, 4, 1);
    root.add(manualPane,   0, 1, 4, 1);

    return root;
  }

  /** Updates which parameter pane is visible based on the selected mode. */
  private void updateVisibleParamPane() {
    tilePane.setVisible(modeBox.getValue() == Mode.TILE_SIZE);
    tilePane.setManaged(tilePane.isVisible());
    colsRowsPane.setVisible(modeBox.getValue() == Mode.COLS_ROWS);
    colsRowsPane.setManaged(colsRowsPane.isVisible());
    manualPane.setVisible(modeBox.getValue() == Mode.MANUAL);
    manualPane.setManaged(manualPane.isVisible());
  }

  /** Configures the regions table with editable name and numeric columns. */
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
    TableColumn<SpriteRegion, Number> wCol = numCol("W", NUM_COLUMN_PREF_WIDTH, SpriteRegion::getWidth);
    TableColumn<SpriteRegion, Number> hCol = numCol("H", NUM_COLUMN_PREF_WIDTH, SpriteRegion::getHeight);

    table.getColumns().addAll(nameCol, xCol, yCol, wCol, hCol);
    table.setItems(regions);
  }

  /** Helper to build a numeric column given a getter. */
  private TableColumn<SpriteRegion, Number> numCol(
      String title, double width, ToDoubleFunction<SpriteRegion> getter) {
    TableColumn<SpriteRegion, Number> col = new TableColumn<>(title);
    col.setPrefWidth(width);
    col.setCellValueFactory(v ->
        new SimpleDoubleProperty(getter.applyAsDouble(v.getValue())));
    return col;
  }

  /**
   * Shows this pane in a standalone dialog.
   *
   * @param owner the owner window for modality
   */
  public static void show(Window owner) {
    Stage st = new Stage();
    st.setTitle("Sprite‑Sheet Processor");
    st.initOwner(owner);
    st.setScene(new Scene(new SpriteSheetProcessorPane(owner), DIALOG_WIDTH, DIALOG_HEIGHT));
    st.show();
  }
}
