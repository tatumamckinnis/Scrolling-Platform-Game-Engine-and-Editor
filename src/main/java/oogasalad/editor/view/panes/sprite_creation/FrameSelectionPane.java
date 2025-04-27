package oogasalad.editor.view.panes.sprite_creation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import oogasalad.editor.model.data.object.sprite.FrameData;

/**
 * Lets the user tick which frames of a sprite sheet are used, pick a base frame, and (optionally)
 * preview the currently selected frame.
 *
 * <p>If {@link #setSpriteSheetImage(Image)} is called the pane will show the
 * real pixels; otherwise it falls back to a textual placeholder.</p>
 *
 * @author Jacob You
 */
public class FrameSelectionPane extends VBox {

  /* ------------------------------------------------------------------ */
  /*  state                                                             */
  /* ------------------------------------------------------------------ */

  private final ObservableList<FrameData> allFrames = FXCollections.observableArrayList();
  private final Map<String, FrameData> selectedMap = new HashMap<>();

  private final TableView<FrameRow> table = new TableView<>();
  private final ComboBox<String> baseBox = new ComboBox<>();
  private final ImageView preview = new ImageView();        // right-hand preview
  private Image sheetImage;

  /* ------------------------------------------------------------------ */
  /*  constructor                                                       */
  /* ------------------------------------------------------------------ */

  public FrameSelectionPane() {
    setSpacing(6);
    setPadding(new Insets(8));

    /* ----- table ---------------------------------------------------- */
    TableColumn<FrameRow, String> nameCol = new TableColumn<>("Frame");
    nameCol.setCellValueFactory(c ->
        new SimpleStringProperty(c.getValue().getFrame().name()));
    nameCol.setPrefWidth(180);

    TableColumn<FrameRow, Boolean> useCol = new TableColumn<>("Use");
    // editable check-box cells
    useCol.setCellValueFactory(c -> c.getValue().useProperty());
    useCol.setCellFactory(CheckBoxTableCell.forTableColumn(index ->
        table.getItems().get(index).useProperty()));
    useCol.setPrefWidth(60);
    useCol.setEditable(true);

    table.getColumns().setAll(useCol, nameCol);
    table.setEditable(true);
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    table.setPlaceholder(new Label("Load a sheet to see frames"));
    VBox.setVgrow(table, Priority.ALWAYS);

    /* when the user clicks a row, update preview */
    table.getSelectionModel().selectedItemProperty().addListener(
        (obs, oldRow, newRow) -> updatePreview(newRow != null ? newRow.getFrame() : null));

    /* ----- preview box ---------------------------------------------- */
    preview.setFitWidth(140);
    preview.setFitHeight(140);
    preview.setPreserveRatio(true);
    preview.setSmooth(true);
    preview.setImage(buildPlaceholder());

    VBox previewBox = new VBox(preview);
    previewBox.setAlignment(Pos.TOP_CENTER);
    previewBox.setPadding(new Insets(4));
    previewBox.setPrefWidth(150);

    /* layout – table | preview, then base-frame chooser --------------- */
    HBox centre = new HBox(10, table, previewBox);
    getChildren().addAll(centre, new Label("Base frame:"), baseBox);

    baseBox.setPromptText("Base frame");
    baseBox.setMaxWidth(Double.MAX_VALUE);
  }

  /* ------------------------------------------------------------------ */
  /*  public API                                                        */
  /* ------------------------------------------------------------------ */

  /**
   * Quick variant: nothing pre-selected.
   */
  public void setFrames(List<FrameData> frames) {
    setFrames(frames, new HashSet<>(), null);
  }

  /**
   * Populate the pane, tick the requested frames, and choose a starting base.
   */
  public void setFrames(List<FrameData> frames,
      Set<String> selected,
      String base) {

    /* table rows ------------------------------------------------------ */
    allFrames.setAll(frames);
    table.getItems().setAll(
        allFrames.stream().map(FrameRow::new).toList());

    /* pre-select + listen for changes --------------------------------- */
    selectedMap.clear();
    table.getItems().forEach(row -> {
      boolean startChecked = selected.contains(row.getFrame().name());
      row.useProperty().set(startChecked);
      if (startChecked) {
        selectedMap.put(row.getFrame().name(), row.getFrame());
      }
      row.useProperty().addListener((obs, o, n) -> {
        if (n) {
          selectedMap.put(row.getFrame().name(), row.getFrame());
        } else {
          selectedMap.remove(row.getFrame().name());
        }
        refreshBaseChoices();
      });
    });

    /* base frame combo-box ------------------------------------------- */
    refreshBaseChoices();
    baseBox.setValue(selected.contains(base) ? base : null);
  }

  /**
   * Frame-name → {@link FrameData} of all ticked rows.
   */
  public Map<String, FrameData> getUsedMap() {
    return selectedMap;
  }

  /**
   * @return the currently chosen base frame (may be {@code null}).
   */
  public String getSelectedBase() {
    return baseBox.getValue();
  }

  /**
   * Supply the sprite-sheet image so the preview can show real pixels. Call this once when you load
   * the atlas.
   */
  public void setSpriteSheetImage(Image sheet) {
    this.sheetImage = sheet;
    // refresh preview in case something is already selected
    FrameRow sel = table.getSelectionModel().getSelectedItem();
    updatePreview(sel != null ? sel.getFrame() : null);
  }

  /* ------------------------------------------------------------------ */
  /*  helpers                                                           */
  /* ------------------------------------------------------------------ */

  /**
   * Keep combo-box in sync with the ticked frames.
   */
  private void refreshBaseChoices() {
    var names = selectedMap.keySet().stream().sorted().toList();
    baseBox.setItems(FXCollections.observableArrayList(names));
    if (!selectedMap.containsKey(baseBox.getValue())) {
      baseBox.setValue(null);
    }
  }

  /**
   * Draw or clear the preview pane.
   */
  private void updatePreview(FrameData frame) {
    if (sheetImage == null || frame == null) {
      preview.setImage(buildPlaceholder());
      return;
    }
    try {
      WritableImage sub =
          new WritableImage(sheetImage.getPixelReader(),
              frame.x(), frame.y(), frame.width(), frame.height());
      preview.setImage(sub);
    } catch (Exception e) {
      // coordinates out of bounds – fall back
      preview.setImage(buildPlaceholder());
    }
  }

  /**
   * Simple placeholder (1 × 1 transparent pixel).
   */
  private Image buildPlaceholder() {
    return new WritableImage(1, 1);
  }

  /* ------------------------------------------------------------------ */
  /*  table row                                                          */
  /* ------------------------------------------------------------------ */

  private static final class FrameRow {

    private final FrameData frame;
    private final BooleanProperty use = new SimpleBooleanProperty(false);

    FrameRow(FrameData frame) {
      this.frame = Objects.requireNonNull(frame);
    }

    FrameData getFrame() {
      return frame;
    }

    BooleanProperty useProperty() {
      return use;
    }

    @SuppressWarnings("unused")
    boolean isUsed() {
      return use.get();
    }
  }
}
