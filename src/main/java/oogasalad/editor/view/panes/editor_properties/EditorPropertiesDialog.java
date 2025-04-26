package oogasalad.editor.view.panes.editor_properties;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.controller.level.EditorDataAPI;
import oogasalad.editor.model.data.EditorLevelData;

/**
 * Modal window that lets the designer tweak editor-wide settings like cell size and snap to grid.
 * It writes straight back through the EditorController.
 *
 * @author Billy McCune
 */
public class EditorPropertiesDialog extends Stage {

  private static final Logger LOG = LogManager.getLogger(EditorPropertiesDialog.class);
  private static final String NUMERIC_REGEX = "\\d*";

  /* --------------- dependencies & model ---------------- */
  private final EditorController controller;
  private final EditorDataAPI editorAPI;
  private final EditorLevelData level;

  /* ------------------- UI controls -------------------- */
  private final TextField cellSizeField = new TextField();
  private final CheckBox snapToGridCheckBox = new CheckBox("Enable");

  /**
   * Creates a new EditorPropertiesDialog.
   *
   * @param controller the EditorController used to get and set properties
   * @param owner the owner Window for this modal dialog
   */
  public EditorPropertiesDialog(EditorController controller, Window owner) {
    this.controller = Objects.requireNonNull(controller);
    this.editorAPI = controller.getEditorDataAPI();
    this.level = editorAPI.getLevel();

    initModality(Modality.APPLICATION_MODAL);
    initOwner(owner);
    setTitle("Editor Properties");

    setScene(new Scene(buildUI(), 400, 200));
    populateFromModel();
  }

  /**
   * Builds the main UI layout for the dialog.
   *
   * @return the root Parent node containing all UI controls
   */
  private Parent buildUI() {
    VBox root = new VBox(12);
    root.setPadding(new Insets(15));

    // Grid Settings Section
    VBox gridSection = buildGridSection();

    // Dialog Buttons
    HBox buttons = buildDialogButtons();
    buttons.setPadding(new Insets(10));
    buttons.setAlignment(Pos.CENTER_RIGHT);

    root.getChildren().addAll(
        section("Grid Settings", gridSection),
        buttons
    );

    return root;
  }

  /**
   * Builds the Grid Settings section containing cell size and snap-to-grid controls.
   *
   * @return a VBox containing grid setting controls
   */
  private VBox buildGridSection() {
    VBox section = new VBox(8);
    section.getStyleClass().add("input-sub-section");

    // Cell Size
    Label cellSizeLabel = new Label("Cell Size (pixels):");
    cellSizeField.setPromptText("Enter cell size");
    cellSizeField.setTextFormatter(new TextFormatter<>(c ->
        c.getControlNewText().matches(NUMERIC_REGEX) ? c : null));

    // Snap to Grid
    Label snapToGridLabel = new Label("Snap to Grid:");
    snapToGridCheckBox.setSelected(true);

    section.getChildren().addAll(
        cellSizeLabel, cellSizeField,
        snapToGridLabel, snapToGridCheckBox
    );

    return section;
  }

  /**
   * Builds the dialog button bar with "Save" and "Cancel" buttons.
   *
   * @return an HBox with configured action buttons
   */
  private HBox buildDialogButtons() {
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

    return new HBox(10, ok, cancel);
  }

  /**
   * Wraps a content section with a titled label.
   *
   * @param title the section title
   * @param content the Node to display below the title
   * @return a Node grouping the title and content
   */
  private Node section(String title, Node content) {
    VBox box = new VBox(4, new Label(title), content);
    return box;
  }

  /**
   * Populates the UI fields from the current model data.
   * Displays an error alert if population fails.
   */
  private void populateFromModel() {
    try {
      // Grid Settings
      cellSizeField.setText(String.valueOf(controller.getCellSize()));
      snapToGridCheckBox.setSelected(controller.isSnapToGrid());
    } catch (Exception e) {
      LOG.error("Error populating editor properties from model", e);
      new Alert(Alert.AlertType.ERROR,
          "Could not load editor properties:\n" + e.getMessage())
          .showAndWait();
    }
  }

  /**
   * Validates and pushes the user's input back to the model.
   * Shows error alerts on invalid input or save failures.
   *
   * @return true if the model was successfully updated; false otherwise
   */
  private boolean pushToModel() {
    try {
      // Grid Settings
      int cellSize = Integer.parseInt(cellSizeField.getText());
      if (cellSize <= 0) {
        throw new IllegalArgumentException("Cell size must be positive");
      }
      controller.setCellSize(cellSize);
      controller.setSnapToGrid(snapToGridCheckBox.isSelected());

      return true;
    } catch (NumberFormatException e) {
      new Alert(Alert.AlertType.ERROR,
          "Cell size must be a valid number.")
          .showAndWait();
    } catch (IllegalArgumentException e) {
      new Alert(Alert.AlertType.ERROR, e.getMessage())
          .showAndWait();
    } catch (Exception e) {
      LOG.error("Failed to save editor properties", e);
      new Alert(Alert.AlertType.ERROR,
          "Could not save editor properties:\n" + e.getMessage())
          .showAndWait();
    }
    return false;
  }
}
