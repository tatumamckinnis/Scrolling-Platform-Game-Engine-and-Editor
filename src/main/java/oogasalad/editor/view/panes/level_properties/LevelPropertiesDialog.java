package oogasalad.editor.view.panes.level_properties;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.controller.level.CameraDataManager;
import oogasalad.editor.controller.level.EditorDataAPI;
import oogasalad.editor.model.data.CameraSpecLoader;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.Layer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Modal window that lets the designer tweak high-level level-wide settings: • overall
 * (auto-derived) bounds • camera type + per-type parameters • render layers • object groups
 * <p>
 * It writes straight back through the EditorController’s {@link CameraDataManager} and
 * {@link EditorLevelData}.
 */
public class LevelPropertiesDialog extends Stage {

  /* ---------------------------------------------------- */
  private static final Logger LOG =
      LogManager.getLogger(LevelPropertiesDialog.class);

  /* --------------- dependencies & model ---------------- */
  private final EditorController controller;
  private final EditorDataAPI editorAPI;
  private final EditorLevelData level;
  private final CameraDataManager cameraManager;

  /* ------------------- UI controls -------------------- */
  private final TextField gameNameTextField = new TextField();
  private final TextField widthField = new TextField();
  private final TextField heightField = new TextField();

  private final ComboBox<String> cameraTypeBox = new ComboBox<>();
  private final GridPane cameraParamGrid = new GridPane();
  private final Map<String, TextField> paramFieldMap = new HashMap<>();

  private final ObservableList<Layer> layerRows = FXCollections.observableArrayList();
  private final ObservableList<String> groupRows = FXCollections.observableArrayList();

  private final CameraSpecLoader SPEC_LOADER = new CameraSpecLoader();

  /* ==================================================== */
  public LevelPropertiesDialog(EditorController controller, Window owner) {
    this.controller = Objects.requireNonNull(controller);
    this.editorAPI = controller.getEditorDataAPI();
    this.level = editorAPI.getLevel();
    this.cameraManager = new CameraDataManager(level);

    initModality(Modality.APPLICATION_MODAL);
    initOwner(owner);
    setTitle("Level Properties");

    setScene(new Scene(buildUI(), 640, 520));
    populateFromModel();
  }

  /* ================= UI construction ================== */
  private Parent buildUI() {

    /* ––– Level size (auto-generated from current objects) ––– */
    GridPane sizeGrid = miniGrid();
    sizeGrid.addRow(0, new Label("Width:"), widthField,
        new Label("Height:"), heightField);

    /* ––– camera type & dynamic parameter grid ––– */
    cameraTypeBox.getItems().setAll(SPEC_LOADER.getCameraTypes());
    cameraTypeBox.getSelectionModel().selectedItemProperty()
        .addListener((o, oldV, newV) -> rebuildCameraParamGrid(newV));

    VBox cameraBlock = new VBox(6,
        new HBox(6, new Label("Camera type:"), cameraTypeBox),
        cameraParamGrid
    );

    /* ––– layers ––– */
    TableView<Layer> layerTable = buildLayerTable();

    /* ––– groups ––– */
    ListView<String> groupList = buildGroupList();

    /* ––– dialog buttons ––– */
    Button ok = new Button("Save");
    ok.setDefaultButton(true);
    ok.setOnAction(e -> {
      if (pushToModel()) {
        close();
      }
    });

    Button cancel = new Button("Cancel");
    cancel.setCancelButton(true);
    cancel.setOnAction(e -> close());

    HBox buttons = new HBox(10, ok, cancel);
    buttons.setPadding(new Insets(10));
    buttons.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

    /* ––– root layout ––– */
    VBox root = new VBox(12,
        section("Game Name:", gameNameTextField),
        section("Dimensions", sizeGrid),
        section("Camera", cameraBlock),
        section("Layers", new VBox(layerTable, layerButtons(layerTable))),
        section("Groups", new VBox(groupList, groupButtons(groupList))),
        buttons
    );
    root.setPadding(new Insets(15));
    VBox.setVgrow(layerTable, Priority.ALWAYS);
    VBox.setVgrow(groupList, Priority.ALWAYS);

    return new ScrollPane(root); // scrollable if dialog shrinks
  }

  private GridPane miniGrid() {
    GridPane g = new GridPane();
    g.setHgap(6);
    g.setVgap(6);
    return g;
  }

  private Node section(String title, Node content) {
    VBox box = new VBox(4, new Label(title), content);
    return box;
  }

  /* ================= camera param helpers ============== */
  private void rebuildCameraParamGrid(String type) {
    cameraParamGrid.getChildren().clear();
    paramFieldMap.clear();
    if (type == null) {
      return;
    }

    CameraSpecLoader.Specifications spec =
        SPEC_LOADER.getSpecifications(type);

    if (spec == null) {
      return;
    }

    int r = 0;
    for (String p : spec.strParams()) {
      TextField tf = new TextField();
      cameraParamGrid.addRow(r++, new Label(p + ":"), tf);
      paramFieldMap.put(p, tf);
    }
    for (String p : spec.dblParams()) {
      TextField tf = new TextField();
      tf.setTextFormatter(new TextFormatter<>(c ->
          c.getControlNewText().matches("-?\\d*\\.?\\d*") ? c : null));
      cameraParamGrid.addRow(r++, new Label(p + ":"), tf);
      paramFieldMap.put(p, tf);
    }
  }

  /* ================= layer helpers ===================== */
  private TableView<Layer> buildLayerTable() {
    TableView<Layer> tv = new TableView<>(layerRows);
    tv.setPlaceholder(new Label("No layers"));

    TableColumn<Layer, String> nameCol = new TableColumn<>("Name");
    nameCol.setCellValueFactory(cd ->
        new SimpleStringProperty(cd.getValue().getName()));
    nameCol.setPrefWidth(220);

    TableColumn<Layer, Number> prioCol = new TableColumn<>("Priority");
    prioCol.setCellValueFactory(cd ->
        new SimpleIntegerProperty(cd.getValue().getPriority()));
    prioCol.setPrefWidth(100);

    tv.getColumns().setAll(nameCol, prioCol);
    return tv;
  }

  private Node layerButtons(TableView<Layer> tv) {
    Button add = new Button("+");
    add.setOnAction(e -> {
      TextInputDialog d = new TextInputDialog("Layer");
      d.setHeaderText("Layer name:");
      d.showAndWait().ifPresent(n -> layerRows.add(new Layer(n, 0)));
    });
    Button rm = new Button("-");
    rm.setOnAction(e -> {
      Layer sel = tv.getSelectionModel().getSelectedItem();
      if (sel != null) {
        layerRows.remove(sel);
      }
    });
    return new HBox(6, add, rm);
  }

  /* ================= group helpers ===================== */
  private ListView<String> buildGroupList() {
    ListView<String> lv = new ListView<>(groupRows);
    lv.setPlaceholder(new Label("No groups"));
    return lv;
  }

  private Node groupButtons(ListView<String> lv) {
    Button add = new Button("+");
    add.setOnAction(e -> {
      TextInputDialog d = new TextInputDialog("Group");
      d.setHeaderText("Group name:");
      d.showAndWait().ifPresent(groupRows::add);
    });
    Button rm = new Button("-");
    rm.setOnAction(e -> {
      String sel = lv.getSelectionModel().getSelectedItem();
      if (sel != null) {
        groupRows.remove(sel);
      }
    });
    return new HBox(6, add, rm);
  }

  /* ================ model ←→ UI sync =================== */
  private void populateFromModel() {

    /* Bounds → width/height (auto-computed) */
    int[] b = level.getBounds();
    widthField.setText(String.valueOf(b[2] - b[0]));
    heightField.setText(String.valueOf(b[3] - b[1]));

    /* Camera */
    cameraTypeBox.getSelectionModel().select(cameraManager.getType());
    rebuildCameraParamGrid(cameraManager.getType());

    cameraManager.getStringParams().forEach((k, v) -> {
      TextField tf = paramFieldMap.get(k);
      if (tf != null) {
        tf.setText(v);
      }
    });
    cameraManager.getDoubleParams().forEach((k, v) -> {
      TextField tf = paramFieldMap.get(k);
      if (tf != null) {
        tf.setText(Double.toString(v));
      }
    });

    layerRows.setAll(level.getLayers());
    groupRows.setAll(level.getGroups());
  }

  private boolean pushToModel() {
    try {
      setGameName();
      /* camera type + per-type params */
      setCamera();
      return true;

    } catch (NumberFormatException nfe) {
      new Alert(Alert.AlertType.ERROR,
          "Width / Height and numeric camera params must be numbers.")
          .showAndWait();
    } catch (Exception ex) {
      LOG.error("Failed to save level properties", ex);
      new Alert(Alert.AlertType.ERROR,
          "Could not save level properties:\n" + ex.getMessage())
          .showAndWait();
    }
    return false;
  }

  private void setCamera() {
    String camType = cameraTypeBox.getValue();
    cameraManager.setType(camType);

    CameraSpecLoader.Specifications spec =
        SPEC_LOADER.getSpecifications(camType);

    Map<String, String> strParams = new HashMap<>();
    Map<String, Double> dblParams = new HashMap<>();

    spec.strParams().forEach(p ->
        strParams.put(p, paramFieldMap.get(p).getText()));
    spec.dblParams().forEach(p -> {
      String txt = paramFieldMap.get(p).getText();
      dblParams.put(p, txt.isBlank() ? 0 : Double.parseDouble(txt));
    });

    cameraManager.replaceStringParams(strParams);
    cameraManager.replaceDoubleParams(dblParams);
  }

  private void setGameName() {
    String gameName = gameNameTextField.getText();
    gameName = gameName.trim();
    level.setGameName(gameName);
  }
}
