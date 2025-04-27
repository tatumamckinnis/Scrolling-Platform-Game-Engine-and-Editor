package oogasalad.editor.view.panes.sprite_creation;

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
 * A JavaFX component that allows users to select frames from a sprite sheet and choose a base frame
 * from the selected frames.
 *
 * <p>{@code FrameSelectionPane} displays a table listing all available frames with a checkbox
 * to indicate usage, and a dropdown to select the base frame among those selected.</p>
 *
 * @author Jacob You
 */
public class FrameSelectionPane extends VBox {

  private final ObservableList<FrameData> allFrames = FXCollections.observableArrayList();
  private final Map<String, FrameData> selectedMap = new HashMap<>();
  private final TableView<FrameRow> table = new TableView<>();
  private final ComboBox<String> baseBox = new ComboBox<>();

  /**
   * Constructs a new {@code FrameSelectionPane} with an empty table and base-frame selector.
   */
  public FrameSelectionPane() {
    // setup code...
  }

  /**
   * Populates the table with frames from the given list.
   *
   * <p>Each frame appears with a checkbox to toggle selection, and the base-frame
   * dropdown is updated accordingly.</p>
   *
   * @param frames the list of {@link FrameData} to display
   */
  public void setFrames(List<FrameData> frames) {
    // method body...
  }

  /**
   * Populates the table with frames and applies pre-selected frames and a chosen base frame.
   *
   * @param frames   the list of frames to display
   * @param selected the set of frame names to mark as selected
   * @param base     the name of the frame to set as the initial base frame
   */
  public void setFrames(List<FrameData> frames, Set<String> selected, String base) {
    // method body...
  }

  /**
   * Returns a map of all frames currently marked as selected.
   *
   * @return a map where the key is the frame name and the value is the {@link FrameData}
   */
  public Map<String, FrameData> getUsedMap() {
    return table.getItems().stream()
        .filter(FrameRow::isUsed)
        .map(FrameRow::getFrame)
        .collect(Collectors.toMap(FrameData::name, f -> f));
  }

  /**
   * Returns the name of the currently selected base frame.
   *
   * @return the base frame name, or {@code null} if none is selected
   */
  public String getSelectedBase() {
    return baseBox.getValue();
  }

  // ─────────────────────────────────

  /**
   * Helper class representing a single row in the frame selection table.
   *
   * <p>Each {@code FrameRow} holds a reference to a {@link FrameData}
   * and a boolean property indicating whether it is selected for use.</p>
   *
   * @author Jacob You
   */
  private static class FrameRow {

    private final FrameData frame;
    private final BooleanProperty use = new SimpleBooleanProperty(false);

    /**
     * Constructs a {@code FrameRow} for the specified frame.
     *
     * @param f the frame data
     */
    FrameRow(FrameData f) {
      this.frame = f;
    }

    /**
     * Returns the {@link FrameData} associated with this row.
     *
     * @return the frame data
     */
    FrameData getFrame() {
      return frame;
    }

    /**
     * Returns the boolean property representing whether the frame is selected.
     *
     * @return the {@link BooleanProperty} for frame usage
     */
    BooleanProperty useProperty() {
      return use;
    }

    /**
     * Returns whether the frame is currently marked as used.
     *
     * @return {@code true} if selected, {@code false} otherwise
     */
    boolean isUsed() {
      return use.get();
    }
  }
}

