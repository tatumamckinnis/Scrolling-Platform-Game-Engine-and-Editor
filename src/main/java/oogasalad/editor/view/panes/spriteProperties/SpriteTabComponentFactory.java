package oogasalad.editor.view.panes.spriteProperties;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.controller.object_data.SpriteDataManager;
import oogasalad.editor.model.data.SpriteTemplateMap;
import oogasalad.editor.model.data.object.sprite.SpriteTemplate;
import oogasalad.editor.view.EditorViewListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Fresh implementation of the “Sprites” tab. • Read-only field shows which template is bound to the
 * selected object. • “Change Template…” button opens a ChoiceDialog listing all templates. • X, Y,
 * Rotation, Flip fields push live edits to SpriteDataAPI.
 */
public class SpriteTabComponentFactory implements EditorViewListener {

  /* ── constants ─────────────────────────────────────────── */
  private static final Logger LOG = LogManager.getLogger(SpriteTabComponentFactory.class);
  private static final String INT_REGEX = "-?\\d*";
  private static final String DOUBLE_REGEX = "-?\\d*\\.?\\d*";
  private static final double FIELD_WIDTH_PX = 80;
  private static final double GRID_HGAP = 8;
  private static final double GRID_VGAP = 6;

  /* ── injected deps ─────────────────────────────────────── */
  private final EditorController controller;
  private final SpriteTemplateMap templateMap;

  /* ── UI nodes ──────────────────────────────────────────── */
  private TextField currentTemplateField;           // read-only
  private Button changeTemplateBtn;

  private TextField xField;
  private TextField yField;
  private TextField rotField;
  private CheckBox flipBox;

  /* ── state ─────────────────────────────────────────────── */
  private UUID currentObjectId;                     // null == nothing selected

  public SpriteTabComponentFactory(EditorController controller) {
    this.controller = Objects.requireNonNull(controller);
    this.templateMap = controller.getEditorDataAPI().getSpriteTemplateMap();
  }

  /* ---------------------------------------------------------------------- */
  /*                          public build-method                           */
  /* ---------------------------------------------------------------------- */
  public ScrollPane createSpritePane() {
    buildControls();

    GridPane grid = new GridPane();
    grid.setHgap(GRID_HGAP);
    grid.setVgap(GRID_VGAP);
    grid.setPadding(new Insets(10));

    int r = 0;
    grid.add(new Label("Current Template:"), 0, r);
    grid.add(currentTemplateField, 1, r++);
    grid.add(changeTemplateBtn, 1, r++);

    grid.add(new Label("X:"), 0, r);
    grid.add(xField, 1, r++);
    grid.add(new Label("Y:"), 0, r);
    grid.add(yField, 1, r++);
    grid.add(new Label("Rotation (°):"), 0, r);
    grid.add(rotField, 1, r++);
    grid.add(flipBox, 1, r);

    ScrollPane scroller = new ScrollPane(new VBox(grid));
    scroller.setFitToWidth(true);
    return scroller;
  }

  /* ---------------------------------------------------------------------- */
  /*                          UI construction                               */
  /* ---------------------------------------------------------------------- */
  private void buildControls() {
    /* read-only template name */
    currentTemplateField = new TextField();
    currentTemplateField.setEditable(false);
    currentTemplateField.setPrefWidth(160);

    /* change-template button */
    changeTemplateBtn = new Button("Change Template…");
    changeTemplateBtn.setOnAction(e -> showTemplateDialog());

    /* numeric / rotation text fields */
    xField = makeIntField((id, v) -> controller.getEditorDataAPI().getSpriteDataAPI().setX(id, v));
    yField = makeIntField((id, v) -> controller.getEditorDataAPI().getSpriteDataAPI().setY(id, v));
    rotField = makeDoubleField(
        (id, v) -> controller.getEditorDataAPI().getSpriteDataAPI().setRotation(id, v));

    /* flip checkbox */
    flipBox = new CheckBox("Flip Horizontally");
    flipBox.selectedProperty().addListener((o, ov, nv) -> {
      if (currentObjectId != null) {
        controller.getEditorDataAPI().getSpriteDataAPI().setFlip(currentObjectId, nv);
      }
    });
  }

  private TextField makeIntField(BiConsumer<UUID, Integer> setter) {
    TextField tf = new TextField();
    tf.setPrefWidth(FIELD_WIDTH_PX);
    tf.setTextFormatter(new TextFormatter<>(c ->
        c.getControlNewText().matches(INT_REGEX) ? c : null));
    tf.textProperty().addListener((o, ov, nv) -> {
      if (currentObjectId != null && (nv == null || nv.matches(INT_REGEX))) {
        setter.accept(currentObjectId, safeInt(nv));
      }
    });
    return tf;
  }

  private TextField makeDoubleField(BiConsumer<UUID, Double> setter) {
    TextField tf = new TextField();
    tf.setPrefWidth(FIELD_WIDTH_PX);
    tf.setTextFormatter(new TextFormatter<>(c ->
        c.getControlNewText().matches(DOUBLE_REGEX) ? c : null));
    tf.textProperty().addListener((o, ov, nv) -> {
      if (currentObjectId != null) {
        try {
          setter.accept(currentObjectId, Double.parseDouble(nv));
        } catch (NumberFormatException ignored) {
        }
      }
    });
    return tf;
  }

  private void showTemplateDialog() {
    List<String> names = templateMap.getSpriteMap().keySet()
        .stream().sorted().collect(Collectors.toList());
    if (names.isEmpty()) {
      new Alert(Alert.AlertType.INFORMATION, "No sprite templates available.").showAndWait();
      return;
    }
    ChoiceDialog<String> dlg = new ChoiceDialog<>(null, names);
    dlg.setTitle("Select Sprite Template");
    dlg.setHeaderText("Choose a template to apply:");
    Optional<String> choice = dlg.showAndWait();

    if (choice.isPresent() && currentObjectId != null) {
      String chosen = choice.get();
      SpriteTemplate tmpl = templateMap.getSpriteMap().get(chosen);
      if (tmpl != null) {
        controller.getEditorDataAPI()
            .getSpriteDataAPI()
            .applyTemplateToSprite(currentObjectId, tmpl);
        LOG.debug("Applying template {}", chosen);
        refreshFromModel();
      }
    }
  }

  private void refreshFromModel() {
    if (currentObjectId == null) {
      clearFields();
      return;
    }

    SpriteDataManager mgr = controller.getEditorDataAPI().getSpriteDataAPI();

    currentTemplateField.setText(mgr.getTemplateName(currentObjectId));
    xField.setText(Integer.toString(mgr.getX(currentObjectId)));
    yField.setText(Integer.toString(mgr.getY(currentObjectId)));
    rotField.setText(Double.toString(mgr.getRotation(currentObjectId)));
    flipBox.setSelected(mgr.getFlip(currentObjectId));
  }

  private void clearFields() {
    currentTemplateField.clear();
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

  /**
   * When template list changes, rebuild the ChoiceDialog list next time.
   */
  @Override
  public void onSpriteTemplateChanged() { /* no cached list; nothing to do */ }

  private int safeInt(String s) {
    try {
      return (s == null || s.isEmpty()) ? 0 : Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return 0;
    }
  }
}
