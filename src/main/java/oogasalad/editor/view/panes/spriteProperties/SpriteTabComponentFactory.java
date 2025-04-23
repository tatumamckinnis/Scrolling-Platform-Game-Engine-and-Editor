package oogasalad.editor.view.panes.spriteProperties;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.controller.SpriteDataManager;
import oogasalad.editor.model.data.SpriteTemplateMap;
import oogasalad.editor.model.data.object.sprite.SpriteTemplate;
import oogasalad.editor.view.EditorViewListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SpriteTabComponentFactory implements EditorViewListener {

  private static final Logger LOG = LogManager.getLogger(SpriteTabComponentFactory.class);
  private static final double GRID_HGAP = 8;
  private static final double GRID_VGAP = 6;
  private static final String INT_REGEX = "-?\\d*";
  private static final String DOUBLE_REGEX = "-?\\d*\\.?\\d*";
  private static final double TEXTFIELD_WIDTH = 80;

  private final EditorController controller;
  private final SpriteTemplateMap templateMap;

  private ComboBox<String> templateBox;
  private TextField xField, yField, rotField;
  private CheckBox flipBox;

  private UUID currentObjectId;

  public SpriteTabComponentFactory(EditorController controller) {
    this.controller = Objects.requireNonNull(controller);
    this.templateMap = controller.getEditorDataAPI().getSpriteTemplateMap();
    buildControls();
  }

  public ScrollPane createSpritePane() {
    // each time we show the pane we refresh the drop-down
    refreshTemplateBox();
    GridPane grid = new GridPane();
    grid.setHgap(GRID_HGAP);
    grid.setVgap(GRID_VGAP);
    grid.setPadding(new Insets(10));

    int r = 0;
    grid.add(new Label("Sprite Template:"), 0, r);
    grid.add(templateBox, 1, r++);
    grid.add(new Label("X:"), 0, r);
    grid.add(xField, 1, r++);
    grid.add(new Label("Y:"), 0, r);
    grid.add(yField, 1, r++);
    grid.add(new Label("Rotation (°):"), 0, r);
    grid.add(rotField, 1, r++);
    grid.add(flipBox, 1, r);

    // wrap in a ScrollPane exactly like your Properties tab
    ScrollPane sp = new ScrollPane(new VBox(grid));
    sp.setFitToWidth(true);
    return sp;
  }

  private void buildControls() {
    templateBox = new ComboBox<>();
    templateBox.valueProperty().addListener((obs, oldName, newName) -> {
      if (currentObjectId == null || newName == null) {
        return;
      }
      SpriteTemplate tmpl = templateMap.getSpriteMap().get(newName);
      if (tmpl != null) {
        controller.getEditorDataAPI()
            .getSpriteDataAPI()
            .applyTemplateToSprite(currentObjectId, tmpl);
        refreshFromModel();
      }
    });

    xField = makeIntField((id, v) -> controller.getEditorDataAPI().getSpriteDataAPI().setX(id, v));
    yField = makeIntField((id, v) -> controller.getEditorDataAPI().getSpriteDataAPI().setY(id, v));
    rotField = makeDoubleField(
        (id, v) -> controller.getEditorDataAPI().getSpriteDataAPI().setRotation(id, v));

    flipBox = new CheckBox("Flip Horizontally");
    flipBox.selectedProperty().addListener((obs, oldV, newV) -> {
      if (currentObjectId != null) {
        controller.getEditorDataAPI().getSpriteDataAPI().setFlip(currentObjectId, newV);
      }
    });
  }

  private TextField makeIntField(BiConsumer<UUID, Integer> setter) {
    TextField tf = new TextField();
    tf.setPrefWidth(TEXTFIELD_WIDTH);
    tf.setTextFormatter(new TextFormatter<>(c ->
        c.getControlNewText().matches(INT_REGEX) ? c : null));
    tf.textProperty().addListener((obs, ov, nv) -> {
      if (currentObjectId != null && (nv == null || nv.matches(INT_REGEX))) {
        setter.accept(currentObjectId, parseIntSafe(nv));
      }
    });
    return tf;
  }

  private TextField makeDoubleField(BiConsumer<UUID, Double> setter) {
    TextField tf = new TextField();
    tf.setPrefWidth(TEXTFIELD_WIDTH);
    tf.setTextFormatter(new TextFormatter<>(c ->
        c.getControlNewText().matches(DOUBLE_REGEX) ? c : null));
    tf.textProperty().addListener((obs, ov, nv) -> {
      if (currentObjectId != null) {
        try {
          setter.accept(currentObjectId, Double.parseDouble(nv));
        } catch (NumberFormatException ignored) {
        }
      }
    });
    return tf;
  }

  private int parseIntSafe(String s) {
    try {
      return (s == null || s.isEmpty()) ? 0 : Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private void refreshTemplateBox() {

    /* 1 -- clear current selection first so the model is in a safe state */
    templateBox.getSelectionModel().clearSelection();

    /* 2 -- rebuild the list */
    List<String> names = templateMap.getSpriteMap()
        .keySet()
        .stream()
        .sorted()
        .collect(Collectors.toList());

    templateBox.setItems(FXCollections.observableArrayList(names));

    /* 3 -- (optional) disable if empty to avoid clicks on an empty list */
    templateBox.setDisable(names.isEmpty());

    /* 4 -- re-select the template that belongs to the current object */
    if (!names.isEmpty() && currentObjectId != null) {
      String tplName = controller.getEditorDataAPI()
          .getSpriteDataAPI()
          .getTemplateName(currentObjectId);   // <-- TEMPLATE name!
      if (tplName != null && names.contains(tplName)) {
        templateBox.getSelectionModel().select(tplName);
      }
    }
  }

  private void refreshFromModel() {
    if (currentObjectId == null) {
      clearFields();
      return;
    }
    SpriteDataManager mgr = controller.getEditorDataAPI().getSpriteDataAPI();
    templateBox.getSelectionModel().select(mgr.getName(currentObjectId));
    xField.setText(Integer.toString(mgr.getX(currentObjectId)));
    yField.setText(Integer.toString(mgr.getY(currentObjectId)));
    rotField.setText(Double.toString(mgr.getRotation(currentObjectId)));
    flipBox.setSelected(mgr.getFlip(currentObjectId));
  }

  private void clearFields() {
    templateBox.getSelectionModel().clearSelection();
    xField.clear();
    yField.clear();
    rotField.clear();
    flipBox.setSelected(false);
  }

  @Override
  public void onSelectionChanged(UUID selectedObjectId) {
    Platform.runLater(() -> {
      currentObjectId = selectedObjectId;
      refreshFromModel();
    });
  }

  @Override
  public void onObjectUpdated(UUID objectId) {
    if (Objects.equals(currentObjectId, objectId)) {
      Platform.runLater(this::refreshFromModel);
    }
  }

  @Override
  public void onObjectRemoved(UUID objectId) {
    if (Objects.equals(currentObjectId, objectId)) {
      currentObjectId = null;
      Platform.runLater(this::clearFields);
    }
  }

  // other EditorViewListener methods can remain empty
  @Override
  public void onObjectAdded(UUID id) {
  }

  @Override
  public void onDynamicVariablesChanged() {
  }

  @Override
  public void onErrorOccurred(String msg) {
  }

  @Override
  public void onPrefabsChanged() {
  }

  @Override
  public void onSpriteTemplateChanged() {
    Platform.runLater(this::refreshTemplateBox);
  }
}