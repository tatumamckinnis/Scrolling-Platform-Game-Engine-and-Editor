package oogasalad.editor.view.sprites;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import oogasalad.editor.model.data.SpriteSheetAtlas;
import oogasalad.editor.model.data.object.sprite.AnimationData;
import oogasalad.editor.model.data.object.sprite.FrameData;
import oogasalad.editor.model.data.object.sprite.SpriteTemplate;
import oogasalad.editor.model.data.SpriteSheetLibrary;

/**
 * Dialog for creating or editing a {@link SpriteTemplate}.
 * <p>
 * Allows the user to choose a sprite‑sheet, select its base frame,
 * pick which frames to include, and define named animations.
 * </p>
 *
 * @author Jacob You
 */
public class SpriteTemplateComponent extends Stage {

  // — UI controls —
  private final TextField nameField      = new TextField();
  private final TextField pathField      = new TextField();
  private final Button    browseButton   = new Button("Browse…");
  private final ComboBox<String> baseFrameBox = new ComboBox<>();
  private final TableView<FrameRow> framesTable     = new TableView<>();
  private final TableView<AnimationData> animationsTable = new TableView<>();
  private final Button    addAnimation   = new Button("Add Animation");
  private final Button    removeAnimation= new Button("Remove Animation");
  private final Button    okButton       = new Button("OK");
  private final Button    cancelButton   = new Button("Cancel");

  private final SpriteSheetLibrary library;
  private List<FrameData> allFrames;
  private SpriteTemplate result;

  /**
   * Wraps a FrameData and a BooleanProperty for selection.
   */
  private static class FrameRow {
    private final FrameData frame;
    private final BooleanProperty use = new SimpleBooleanProperty(true);
    FrameRow(FrameData frame) { this.frame = frame; }
    FrameData getFrame()   { return frame; }
    BooleanProperty useProperty() { return use; }
    boolean isUsed()       { return use.get(); }
  }

  /**
   * Constructs the dialog.
   *
   * @param owner   the owning window
   * @param library the loaded sprite‑sheet library
   */
  public SpriteTemplateComponent(Window owner, SpriteSheetLibrary library) {
    initOwner(owner);
    initModality(Modality.APPLICATION_MODAL);
    setTitle("Sprite Template Editor");
    this.library = library;

    buildUI();
    wireEvents();
    setScene(new Scene(createRoot(), 800, 600));
  }

  /**
   * Returns the created/edited template, or null if cancelled.
   */
  public SpriteTemplate getResult() {
    return result;
  }

  private Parent createRoot() {
    BorderPane root = new BorderPane();
    root.setPadding(new Insets(10));

    // Top: name + path
    GridPane top = new GridPane();
    top.setHgap(10);
    top.setVgap(8);
    top.add(new Label("Name:"),  0, 0);
    top.add(nameField,           1, 0);
    top.add(new Label("Sheet:"), 0, 1);
    HBox picker = new HBox(5, pathField, browseButton);
    HBox.setHgrow(pathField, Priority.ALWAYS);
    top.add(picker,              1, 1);
    top.add(new Label("Base Frame:"), 0, 2);
    top.add(baseFrameBox,        1, 2);
    root.setTop(top);

    // Center: two tabs
    TabPane tabPane = new TabPane(
        new Tab("Frames",     createFramesPane()),
        new Tab("Animations", createAnimationsPane())
    );
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    root.setCenter(tabPane);

    // Bottom: OK / Cancel
    HBox bottom = new HBox(10, okButton, cancelButton);
    bottom.setPadding(new Insets(10,0,0,0));
    root.setBottom(bottom);

    return root;
  }

  private Parent createFramesPane() {
    // "Use" checkbox column
    TableColumn<FrameRow, Boolean> useCol = new TableColumn<>("Use");
    useCol.setCellValueFactory(cd -> cd.getValue().useProperty());
    useCol.setCellFactory(CheckBoxTableCell.forTableColumn(useCol));
    useCol.setPrefWidth(60);

    // Frame name column
    TableColumn<FrameRow, String> nameCol = new TableColumn<>("Frame Name");
    nameCol.setCellValueFactory(cd ->
        new SimpleStringProperty(cd.getValue().getFrame().name()));
    nameCol.setPrefWidth(200);

    framesTable.getColumns().setAll(useCol, nameCol);
    framesTable.setItems(FXCollections.observableArrayList());
    framesTable.setPlaceholder(new Label("Load a sprite‑sheet first"));
    return new VBox(framesTable);
  }

  private Parent createAnimationsPane() {
    animationsTable.setEditable(true);
    // your existing anim columns here...
    TableColumn<AnimationData, String> animName = new TableColumn<>("Anim Name");
    animName.setCellValueFactory(cd ->
        new SimpleStringProperty(cd.getValue().getFrameNames().toString()));
    animName.setPrefWidth(200);

    TableColumn<AnimationData, Number> lengthCol = new TableColumn<>("Frame Length");
    lengthCol.setCellValueFactory(cd ->
        new SimpleDoubleProperty(cd.getValue().getFrameLength()));
    lengthCol.setPrefWidth(100);

    animationsTable.getColumns().setAll(animName, lengthCol);
    animationsTable.setItems(FXCollections.observableArrayList());

    HBox ctrls = new HBox(5, addAnimation, removeAnimation);
    ctrls.setPadding(new Insets(5));
    return new VBox(5, ctrls, animationsTable);
  }

  private void buildUI() {
    pathField.setEditable(false);
    baseFrameBox.setDisable(true);
    nameField.setPromptText("Enter sprite name...");
  }

  private void wireEvents() {
    browseButton.setOnAction(e -> {
      FileChooser chooser = new FileChooser();
      chooser.getExtensionFilters().add(
          new FileChooser.ExtensionFilter("Sprite Atlas XML", "*.xml"));
      File xml = chooser.showOpenDialog(getOwner());
      if (xml == null) return;

      // 1) store full path
      pathField.setText(xml.getAbsolutePath());

      // 2) load atlas frames
      SpriteSheetAtlas atlas = library.getAtlas(xml.getName());
      allFrames = atlas.frames();

      // 3) populate frame rows
      List<FrameRow> rows = allFrames.stream()
          .map(FrameRow::new)
          .collect(Collectors.toList());
      framesTable.setItems(FXCollections.observableArrayList(rows));

      // 4) base‑frame choices
      baseFrameBox.setItems(FXCollections.observableArrayList(
          allFrames.stream().map(FrameData::name).collect(Collectors.toList())
      ));
      baseFrameBox.setDisable(false);
    });

    addAnimation.setOnAction(e -> {
      // TODO: pop up a small dialog to enter:
      //   * animation name
      //   * frame length
      //   * a multi‑select list of frames (FrameRow.getFrame().name())
      // then animationsTable.getItems().add(new AnimationData(...))
    });

    removeAnimation.setOnAction(e -> {
      AnimationData sel = animationsTable.getSelectionModel().getSelectedItem();
      if (sel != null) animationsTable.getItems().remove(sel);
    });

    okButton.setOnAction(e -> {
      // gather only those FrameRows with use==true
      Map<String, FrameData> selected = framesTable.getItems().stream()
          .filter(FrameRow::isUsed)
          .map(FrameRow::getFrame)
          .collect(Collectors.toMap(FrameData::name, f -> f));

      FrameData base = selected.get(baseFrameBox.getValue());
      Map<String, AnimationData> anims = animationsTable.getItems().stream()
          .collect(Collectors.toMap(
              a -> String.join("_", a.getFrameNames()),  // or your own key
              a -> a
          ));

      result = new SpriteTemplate(
          nameField.getText(),
          pathField.getText(),
          base,
          selected,
          anims
      );
      close();
    });

    cancelButton.setOnAction(e -> {
      result = null;
      close();
    });
  }
}
