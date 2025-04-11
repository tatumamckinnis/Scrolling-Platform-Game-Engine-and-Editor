package oogasalad.editor.view;

import java.util.Objects;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import oogasalad.editor.model.data.object.DynamicVariable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A custom JavaFX Dialog for creating new {@link DynamicVariable} instances.
 */
public class DynamicVariableDialog extends Dialog<DynamicVariable> {

  private static final Logger LOG = LogManager.getLogger(DynamicVariableDialog.class);

  private static final String CSS_PATH = "/css/editorStyles.css";
  private static final String KEY_DIALOG_ADD_VAR_TITLE = "dialogAddVarTitle";
  private static final String KEY_DIALOG_ADD_BUTTON = "dialogAddButton";
  private static final String KEY_DIALOG_VAR_NAME = "dialogVarName";
  private static final String KEY_DIALOG_VAR_TYPE = "dialogVarType";
  private static final String KEY_DIALOG_VAR_VALUE = "dialogVarValue";
  private static final String KEY_DIALOG_VAR_DESC = "dialogVarDesc";
  private static final String KEY_ERROR_INVALID_INPUT_TITLE = "errorInvalidInputTitle";

  private static final double DEFAULT_SPACING = 10.0;
  private static final double SECTION_SPACING = 20.0;
  private static final double DIALOG_INPUT_WIDTH = 200.0;

  private final ResourceBundle uiBundle;
  private TextField nameField;
  private ComboBox<String> typeComboBox;
  private TextField valueField;
  private TextField descriptionField;
  private ButtonType addButtonType;

  /**
   * Constructs a new dialog for creating a {@link DynamicVariable}.
   *
   * @param uiBundle ResourceBundle for localized UI text
   */
  public DynamicVariableDialog(ResourceBundle uiBundle) {
    this.uiBundle = Objects.requireNonNull(uiBundle, "UI Bundle cannot be null.");
    setTitle(uiBundle.getString(KEY_DIALOG_ADD_VAR_TITLE));
    setupDialogPane();
    setupResultConverter();
    setupValidationBinding();
    LOG.debug("DynamicVariableDialog initialized.");
  }

  /**
   * Sets up the dialog pane including styling, buttons, and field references.
   */
  private void setupDialogPane() {
    try {
      getDialogPane().getStylesheets()
          .add(Objects.requireNonNull(getClass().getResource(CSS_PATH)).toExternalForm());
      getDialogPane().getStyleClass().add("dynamic-variable-dialog");
    } catch (Exception e) {
      LOG.warn("Could not load CSS for dynamic variable dialog: {}. Using default styles.", e.getMessage());
    }

    this.addButtonType = new ButtonType(uiBundle.getString(KEY_DIALOG_ADD_BUTTON),
        ButtonBar.ButtonData.OK_DONE);
    getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

    GridPane grid = createDynamicVariableDialogGrid();
    getDialogPane().setContent(grid);

    nameField = (TextField) grid.lookup("#varNameField");
    typeComboBox = (ComboBox<String>) grid.lookup("#varTypeCombo");
    valueField = (TextField) grid.lookup("#varValueField");
    descriptionField = (TextField) grid.lookup("#varDescField");

    if (nameField == null || typeComboBox == null || valueField == null || descriptionField == null) {
      String errorMsg = "Could not find one or more input fields in the dialog grid. Check IDs.";
      LOG.error(errorMsg);
      showErrorAlert("Dialog Initialization Error", errorMsg);
      Node addButtonNode = getDialogPane().lookupButton(addButtonType);
      if (addButtonNode != null) {
        addButtonNode.setDisable(true);
      }
    }
  }

  /**
   * Creates and returns the GridPane layout used in the dialog.
   *
   * @return GridPane containing input fields and labels
   */
  private GridPane createDynamicVariableDialogGrid() {
    GridPane grid = new GridPane();
    grid.setHgap(DEFAULT_SPACING);
    grid.setVgap(DEFAULT_SPACING);
    grid.setPadding(new Insets(SECTION_SPACING));

    nameField = new TextField();
    nameField.setId("varNameField");
    nameField.setPromptText(uiBundle.getString(KEY_DIALOG_VAR_NAME));
    nameField.setPrefWidth(DIALOG_INPUT_WIDTH);

    typeComboBox = new ComboBox<>(FXCollections.observableArrayList("int", "double", "boolean", "string"));
    typeComboBox.setId("varTypeCombo");
    typeComboBox.setValue("double");
    typeComboBox.setPrefWidth(DIALOG_INPUT_WIDTH);

    valueField = new TextField();
    valueField.setId("varValueField");
    valueField.setPromptText(uiBundle.getString(KEY_DIALOG_VAR_VALUE));
    valueField.setPrefWidth(DIALOG_INPUT_WIDTH);

    descriptionField = new TextField();
    descriptionField.setId("varDescField");
    descriptionField.setPromptText(uiBundle.getString(KEY_DIALOG_VAR_DESC) + " (Optional)");
    descriptionField.setPrefWidth(DIALOG_INPUT_WIDTH);

    grid.add(new Label(uiBundle.getString(KEY_DIALOG_VAR_NAME) + ":"), 0, 0);
    grid.add(nameField, 1, 0);
    grid.add(new Label(uiBundle.getString(KEY_DIALOG_VAR_TYPE) + ":"), 0, 1);
    grid.add(typeComboBox, 1, 1);
    grid.add(new Label(uiBundle.getString(KEY_DIALOG_VAR_VALUE) + ":"), 0, 2);
    grid.add(valueField, 1, 2);
    grid.add(new Label(uiBundle.getString(KEY_DIALOG_VAR_DESC) + ":"), 0, 3);
    grid.add(descriptionField, 1, 3);

    return grid;
  }

  /**
   * Binds validation logic to the input fields to control the enabled state of the Add button.
   */
  private void setupValidationBinding() {
    if (nameField == null || typeComboBox == null || valueField == null) {
      LOG.error("Cannot set up validation binding: Input fields are not initialized.");
      return;
    }

    Node addButton = getDialogPane().lookupButton(addButtonType);
    if (addButton != null) {
      addButton.setDisable(true);

      Runnable updateButtonState = () -> {
        boolean disabled = nameField.getText().trim().isEmpty()
            || typeComboBox.getValue() == null
            || valueField.getText().trim().isEmpty();
        addButton.setDisable(disabled);
      };

      nameField.textProperty().addListener((obs, ov, nv) -> updateButtonState.run());
      typeComboBox.valueProperty().addListener((obs, ov, nv) -> updateButtonState.run());
      valueField.textProperty().addListener((obs, ov, nv) -> updateButtonState.run());

      updateButtonState.run();
    } else {
      LOG.error("Could not find the Add button to set up validation binding.");
    }
  }

  /**
   * Configures how the dialog result is constructed based on button input.
   */
  private void setupResultConverter() {
    setResultConverter(dialogButton -> {
      if (dialogButton == addButtonType) {
        try {
          return createVariableFromInput();
        } catch (IllegalArgumentException | NullPointerException | IllegalStateException ex) {
          LOG.warn("Invalid input or error creating dynamic variable: {}", ex.getMessage());
          showErrorAlert(uiBundle.getString(KEY_ERROR_INVALID_INPUT_TITLE), ex.getMessage());
          return null;
        }
      }
      return null;
    });
  }

  /**
   * Creates a {@link DynamicVariable} based on the input field values.
   *
   * @return a new DynamicVariable instance
   * @throws IllegalStateException if required fields are not initialized
   * @throws IllegalArgumentException if required values are empty
   * @throws NullPointerException if type selection is null
   */
  private DynamicVariable createVariableFromInput() {
    if (nameField == null || typeComboBox == null || valueField == null || descriptionField == null) {
      LOG.error("Dialog input fields reference is null during result conversion.");
      throw new IllegalStateException("Internal error: Dialog fields not properly initialized.");
    }

    String name = nameField.getText().trim();
    String type = typeComboBox.getValue();
    String value = valueField.getText().trim();
    String description = descriptionField.getText().trim();

    if (name.isEmpty()) {
      throw new IllegalArgumentException("Variable name cannot be empty.");
    }
    if (type == null) {
      throw new NullPointerException("Variable type must be selected.");
    }
    if (value.isEmpty()) {
      throw new IllegalArgumentException("Variable value cannot be empty.");
    }

    return new DynamicVariable(name, type, value, description);
  }

  /**
   * Displays an error alert to the user.
   *
   * @param title the title of the alert window
   * @param contentText the main error message
   */
  private void showErrorAlert(String title, String contentText) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(contentText);

    try {
      alert.getDialogPane().getStylesheets()
          .add(Objects.requireNonNull(getClass().getResource(CSS_PATH)).toExternalForm());
    } catch (Exception e) {
      LOG.warn("Could not apply CSS to error alert: {}", e.getMessage());
    }

    alert.showAndWait();
  }
}