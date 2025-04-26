package oogasalad.editor.view;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import oogasalad.editor.controller.listeners.EditorListenerNotifier;

/**
 * Frontend window for editing properties like cell size and snap to grid settings.
 */
public class EditorPropertiesWindow extends VBox {
  private static final Logger LOG = LogManager.getLogger(EditorPropertiesWindow.class);
  private static final String NUMERIC_REGEX = "\\d*";

  private final EditorListenerNotifier myNotifier;
  private TextField cellSizeField;
  private CheckBox snapToGridCheckBox;

  public EditorPropertiesWindow(EditorListenerNotifier notifier) {
    myNotifier = notifier;
    setupUI();
  }

  private void setupUI() {
    setSpacing(15);
    setPadding(new Insets(15));
    setAlignment(Pos.TOP_LEFT);
    getStyleClass().add("input-section");

    // Cell Size Section
    VBox cellSizeSection = new VBox(5);
    Label cellSizeLabel = new Label("Cell Size (pixels):");
    cellSizeField = new TextField();
    cellSizeField.setPromptText("Enter cell size");
    cellSizeField.textProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal.matches(NUMERIC_REGEX)) {
        try {
          int cellSize = Integer.parseInt(newVal);
          if (cellSize > 0) {
            changeCellSize(cellSize);
          }
        } catch (NumberFormatException e) {
          LOG.warn("Invalid cell size input: {}", newVal);
        }
      }
    });
    cellSizeSection.getChildren().addAll(cellSizeLabel, cellSizeField);

    // Snap to Grid Section
    VBox snapToGridSection = new VBox(5);
    Label snapToGridLabel = new Label("Snap to Grid:");
    snapToGridCheckBox = new CheckBox("Enable");
    snapToGridCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
      changeSnapToGrid(newVal);
    });
    snapToGridSection.getChildren().addAll(snapToGridLabel, snapToGridCheckBox);

    getChildren().addAll(cellSizeSection, snapToGridSection);
  }

  private void changeCellSize(int cellSize) {
    myNotifier.notifyCellSizeChanged(cellSize);
  }

  private void changeSnapToGrid(boolean doSnap) {
    myNotifier.notifySnapToGridChanged(doSnap);
  }

  /**
   * Updates the cell size field with the given value.
   * @param cellSize The new cell size value
   */
  public void setCellSize(int cellSize) {
    if (cellSizeField != null) {
      cellSizeField.setText(String.valueOf(cellSize));
    }
  }

  /**
   * Updates the snap to grid checkbox with the given value.
   * @param doSnap Whether snap to grid should be enabled
   */
  public void setSnapToGrid(boolean doSnap) {
    if (snapToGridCheckBox != null) {
      snapToGridCheckBox.setSelected(doSnap);
    }
  }
}
