package oogasalad.editor.view.panes.spriteCreation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.VBox;
import oogasalad.editor.model.data.object.sprite.FrameData;

/**
 * Pane for selecting which frames to include from a sprite sheet and choosing a base frame.
 *
 * @author Jacob You
 */
public class FrameSelectionPane extends VBox {

  private final ObservableList<FrameData> allFrames = FXCollections.observableArrayList();
  private final Map<String, FrameData> selectedMap = new HashMap<>();
  private final TableView<FrameRow> table = new TableView<>();
  private final ComboBox<String> baseBox = new ComboBox<>();

  public FrameSelectionPane() {
    setSpacing(5);
    setPadding(new Insets(5));

    table.setEditable(true);
    table.setPlaceholder(new Label("Load a sprite‑sheet first"));

    TableColumn<FrameRow, Boolean> useCol = new TableColumn<>("Use");
    useCol.setCellValueFactory(cd -> cd.getValue().useProperty());
    useCol.setCellFactory(CheckBoxTableCell.forTableColumn(useCol));
    useCol.setPrefWidth(60);

    TableColumn<FrameRow, String> nameCol = new TableColumn<>("Frame Name");
    nameCol.setCellValueFactory(cd ->
        new SimpleStringProperty(cd.getValue().getFrame().name()));
    nameCol.setPrefWidth(260);

    table.getColumns().setAll(useCol, nameCol);

    getChildren().addAll(table, new Label("Base Frame:"), baseBox);
  }

  /**
   * Populate with the sheet’s frames.
   */
  public void setFrames(List<FrameData> frames) {
    var rows = frames.stream().map(FrameRow::new).toList();
    var items = FXCollections.observableArrayList(rows);
    table.setItems(items);

    // whenever a row's “use” toggles, update the base-frame combo
    rows.forEach(r -> r.useProperty().addListener((o, ov, nv) -> refreshBaseBox()));
    refreshBaseBox();
  }

  public void setFrames(List<FrameData> frames, Set<String> selected, String base) {
    setFrames(frames);
    table.getItems().forEach(row ->
        row.useProperty().set(selected.contains(row.getFrame().name())));
    refreshBaseBox();
    baseBox.setValue(base);
  }

  private void refreshBaseBox() {
    var names = table.getItems().stream()
        .filter(FrameRow::isUsed)
        .map(r -> r.getFrame().name())
        .toList();
    baseBox.setItems(FXCollections.observableArrayList(names));
    baseBox.setDisable(names.isEmpty());
    if (!names.contains(baseBox.getValue())) {
      baseBox.getSelectionModel().clearSelection();
    }
  }

  /**
   * Returns a map of selected frame‐name → FrameData.
   */
  public Map<String, FrameData> getUsedMap() {
    return table.getItems().stream()
        .filter(FrameRow::isUsed)
        .map(FrameRow::getFrame)
        .collect(Collectors.toMap(FrameData::name, f -> f));
  }

  /**
   * Returns the currently chosen base‐frame name (or null).
   */
  public String getSelectedBase() {
    return baseBox.getValue();
  }

  // ─────────────────────────────────
  private static class FrameRow {

    private final FrameData frame;
    private final BooleanProperty use = new SimpleBooleanProperty(false);

    FrameRow(FrameData f) {
      this.frame = f;
    }

    FrameData getFrame() {
      return frame;
    }

    BooleanProperty useProperty() {
      return use;
    }

    boolean isUsed() {
      return use.get();
    }
  }
}
