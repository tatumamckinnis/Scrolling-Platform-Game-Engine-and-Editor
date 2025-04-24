package oogasalad.editor.view.panes.spriteCreation;

import java.io.File;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.model.data.SpriteSheetAtlas;
import oogasalad.editor.model.data.SpriteSheetLibrary;
import oogasalad.editor.model.data.object.sprite.AnimationData;
import oogasalad.editor.model.data.object.sprite.SpriteTemplate;
import oogasalad.exceptions.SpriteSheetLoadException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Dialog for creating or editing a {@link SpriteTemplate}.
 * <p>
 * Allows the user to choose a sprite‑sheet, select its base frame, pick which frames to include,
 * and define named animations.
 * </p>
 *
 * @author Jacob You
 */
public class SpriteTemplateComponent extends Stage {

  private static final Logger LOG = LogManager.getLogger(SpriteTemplateComponent.class);

  private static final double WINDOW_W = 800;
  private static final double WINDOW_H = 600;
  private static final double HGAP = 10;
  private static final double VGAP = 8;
  private static final double SPACING = 5;
  private static final double BOTTOM_SP = 10;

  private final EditorController editorController;
  private final SpriteSheetLibrary library;
  private SpriteTemplate result;
  private SpriteTemplate original;
  private boolean editing = false;
  private String currentAtlas;

  private final TextField nameField = new TextField();
  private final TextField pathField = new TextField();
  private final Button browseButton = new Button("Browse…");
  private final FrameSelectionPane framesPane = new FrameSelectionPane();
  private final TableView<AnimationData> animTable = new TableView<>();
  private final Button addAnimButton = new Button("Add Animation");
  private final Button removeAnimButton = new Button("Remove Animation");
  private final Button okButton = new Button("OK");
  private final Button cancelButton = new Button("Cancel");

  public SpriteTemplateComponent(
      EditorController editorController,
      Window owner,
      SpriteSheetLibrary library) {
    this.editorController = editorController;
    this.library = library;
    this.original = null;

    initOwner(owner);
    initModality(Modality.APPLICATION_MODAL);
    setTitle("Sprite Template Editor");

    nameField.setPromptText("Enter sprite name…");
    pathField.setEditable(false);

    wireEvents();
    setScene(new Scene(createRoot(), WINDOW_W, WINDOW_H));
  }

  public SpriteTemplateComponent(EditorController editorController,
      Window owner,
      SpriteSheetLibrary library,
      SpriteTemplate toEdit) {
    this(editorController, owner, library);
    this.original = toEdit;
    this.editing = true;

    nameField.setText(toEdit.getName());
    pathField.setText(toEdit.getSpriteFile());

    String spriteFile = toEdit.getAtlasFile();
    String atlasId = spriteFile.contains(".") ? spriteFile.substring(0, spriteFile.lastIndexOf('.'))
        : spriteFile;

    SpriteSheetAtlas atlas = library.getAtlas(atlasId);

    framesPane.setFrames(atlas.frames(), toEdit.getFrames().keySet(), toEdit.getBaseFrame().name());
    animTable.getItems().setAll(toEdit.getAnimations().values());
  }

  /**
   * Returns the user’s chosen template, or null if cancelled.
   */
  public SpriteTemplate getResult() {
    return result;
  }

  private Parent createRoot() {
    BorderPane root = new BorderPane();
    root.setPadding(new Insets(VGAP));

    // Top: name + sheet picker
    GridPane top = new GridPane();
    top.setHgap(HGAP);
    top.setVgap(VGAP);
    top.add(new Label("Name:"), 0, 0);
    top.add(nameField, 1, 0);
    top.add(new Label("Sheet:"), 0, 1);
    HBox picker = new HBox(SPACING, pathField, browseButton);
    HBox.setHgrow(pathField, Priority.ALWAYS);
    top.add(picker, 1, 1);
    root.setTop(top);

    // Center: tabs for frames + animations
    TabPane tabs = new TabPane(
        new Tab("Frames", framesPane),
        new Tab("Animations", buildAnimationsPane())
    );
    tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    root.setCenter(tabs);

    // Bottom: OK / Cancel
    HBox bottom = new HBox(BOTTOM_SP, okButton, cancelButton);
    bottom.setPadding(new Insets(VGAP, 0, 0, 0));
    root.setBottom(bottom);

    return root;
  }

  private Parent buildAnimationsPane() {
    animTable.setEditable(true);
    animTable.setPlaceholder(new Label("No animations defined"));

    TableColumn<AnimationData, String> nameCol = new TableColumn<>("Name");
    nameCol.setCellValueFactory(cd ->
        new SimpleStringProperty(cd.getValue().getName()));
    nameCol.setPrefWidth(260);

    TableColumn<AnimationData, Number> lengthCol = new TableColumn<>("Length");
    lengthCol.setCellValueFactory(cd ->
        new SimpleDoubleProperty(cd.getValue().getFrameLength()));
    lengthCol.setPrefWidth(100);

    animTable.getColumns().setAll(nameCol, lengthCol);
    animTable.setItems(FXCollections.observableArrayList());

    HBox ctrls = new HBox(SPACING, addAnimButton, removeAnimButton);
    ctrls.setPadding(new Insets(SPACING));
    return new VBox(SPACING, ctrls, animTable);
  }

  private void wireEvents() {
    browseButton.setOnAction(e -> loadAtlas());
    addAnimButton.setOnAction(e -> {
      var usedFrames = framesPane.getUsedMap().values().stream().toList();
      if (usedFrames.isEmpty()) {
        new Alert(Alert.AlertType.WARNING, "Select at least one frame first.").showAndWait();
        return;
      }
      new AnimationDialog(usedFrames).showAndWait()
          .ifPresent(animTable.getItems()::add);
    });
    removeAnimButton.setOnAction(e -> {
      var sel = animTable.getSelectionModel().getSelectedItem();
      if (sel != null) {
        animTable.getItems().remove(sel);
      }
    });

    okButton.setOnAction(e -> {
      var usedMap = framesPane.getUsedMap();
      if (usedMap.isEmpty()) {
        new Alert(Alert.AlertType.ERROR, "You must select at least one frame.").showAndWait();
        return;
      }
      String base = framesPane.getSelectedBase();
      if (base == null) {
        new Alert(Alert.AlertType.ERROR, "You must choose a base frame.").showAndWait();
        return;
      }

      var anims = animTable.getItems().stream()
          .collect(Collectors.toMap(AnimationData::getName, a -> a));

      if (editing) {
        original.setName(nameField.getText());
        original.setSpriteName(pathField.getText());
        original.setBaseFrame(usedMap.get(base));
        original.setFrames(usedMap);
        original.setAnimations(anims);
        result = original;
      } else {
        result = new SpriteTemplate(
            nameField.getText(),
            pathField.getText(),
            currentAtlas,
            usedMap.get(base),
            usedMap,
            anims
        );
      }

      close();
    });

    cancelButton.setOnAction(e -> {
      result = null;
      close();
    });
  }

  private void loadAtlas() {
    FileChooser chooser = new FileChooser();
    chooser.getExtensionFilters().add(
        new FileChooser.ExtensionFilter("Sprite Atlas XML", "*.xml"));
    File xml = chooser.showOpenDialog(getOwner());
    if (xml == null) {
      return;
    }

    String fname = xml.getName();
    String atlasId = fname.substring(0, fname.lastIndexOf('.'));

    SpriteSheetAtlas atlas = library.getAtlas(atlasId);
    if (atlas == null) {
      try {
        atlas = editorController.getEditorDataAPI()
            .getSpriteSheetDataAPI()
            .loadSpriteSheet(xml.getAbsolutePath());
        library.addAtlas(atlas.atlasName(), atlas);
      } catch (SpriteSheetLoadException ex) {
        LOG.error("Load failed: {}", ex.getMessage());
        new Alert(Alert.AlertType.ERROR, "Cannot load sheet").showAndWait();
        return;
      }
    }
    currentAtlas = atlas.atlasName();

    pathField.setText(atlas.imagePath());
    framesPane.setFrames(atlas.frames());
  }
}
