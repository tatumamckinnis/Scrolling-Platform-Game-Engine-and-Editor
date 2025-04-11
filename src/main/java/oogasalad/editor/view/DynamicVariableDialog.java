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
 * A custom JavaFX Dialog for creating new {@link DynamicVariable} instances. This dialog prompts
 * the user for the variable's name, type, initial value, and description. It performs basic input
 * validation (ensuring required fields are filled) and converts the input into a DynamicVariable
 * object upon successful completion. Usage:
 * <pre>
 * {@code
 * DynamicVariableDialog dialog = new DynamicVariableDialog(resourceBundle);
 * Optional<DynamicVariable> result = dialog.showAndWait();
 * result.ifPresent(variable -> {
 * // process the newly created variable
 * });
 * }
 * </pre>
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
  private static final double DIALOG_INPUT_WIDTH = 150.0;

  private final ResourceBundle uiBundle;
  private TextField nameField;
  private ComboBox<String> typeComboBox;
  private TextField valueField;
  private TextField descriptionField;


  /**
   * Constructs a new dialog for creating dynamic variables. Initializes the dialog's title,
   * appearance, content grid, and result conversion logic.
   *
   * @param uiBundle The resource bundle used for localizing dialog text (title, labels, buttons).
   *                 Must not be null.
   * @throws NullPointerException if uiBundle is null.
   */
  public DynamicVariableDialog(ResourceBundle uiBundle) {
    this.uiBundle = Objects.requireNonNull(uiBundle, "UI Bundle cannot be null.");
    setTitle(uiBundle.getString(KEY_DIALOG_ADD_VAR_TITLE));
    setupDialogPane();
    setupResultConverter();
    LOG.debug("DynamicVariableDialog initialized.");
  }

  /**
   * Configures the main DialogPane: applies CSS styling, adds standard buttons (Add, Cancel), sets
   * the content grid, finds references to the input fields within the grid, and sets up validation
   * logic to enable/disable the 'Add' button based on required field input.
   */
  private void setupDialogPane() {
    try {
      getDialogPane().getStylesheets()
          .add(Objects.requireNonNull(getClass().getResource(CSS_PATH)).toExternalForm());
      getDialogPane().getStyleClass().add("dynamic-variable-dialog");
    } catch (Exception e) {
      LOG.warn("Could not load CSS for dynamic variable dialog: {}", e.getMessage());
    }

    ButtonType addButtonType = new ButtonType(uiBundle.getString(KEY_DIALOG_ADD_BUTTON),
        ButtonBar.ButtonData.OK_DONE);
    getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

    GridPane grid = createDynamicVariableDialogGrid();
    getDialogPane().setContent(grid);

    nameField = (TextField) grid.lookup("#varNameField");
    typeComboBox = (ComboBox<String>) grid.lookup("#varTypeCombo");
    valueField = (TextField) grid.lookup("#varValueField");
    descriptionField = (TextField) grid.lookup("#varDescField");

    if (nameField == null || typeComboBox == null || valueField == null
        || descriptionField == null) {
      LOG.error("Could not find one or more input fields in the dialog grid. Check IDs.");
      showErrorAlert("Dialog Initialization Error", "Could not find required input fields.");
      getDialogPane().lookupButton(addButtonType).setDisable(true);
      return;
    }

    Node addButton = getDialogPane().lookupButton(addButtonType);
    addButton.setDisable(true);

    Runnable updateButtonState = () -> {
      boolean disabled = nameField.getText().trim().isEmpty() ||
          typeComboBox.getValue() == null ||
          valueField.getText().trim().isEmpty();
      addButton.setDisable(disabled);
    };

    nameField.textProperty().addListener((obs, ov, nv) -> updateButtonState.run());
    typeComboBox.valueProperty().addListener((obs, ov, nv) -> updateButtonState.run());
    valueField.textProperty().addListener((obs, ov, nv) -> updateButtonState.run());
  }


  /**
   * Creates the GridPane layout containing labels and input fields for the dynamic variable
   * properties. Sets IDs on the input fields so they can be looked up later.
   *
   * @return A GridPane populated with the dialog's input controls.
   */
  private GridPane createDynamicVariableDialogGrid() {
    GridPane grid = new GridPane();
    grid.setHgap(DEFAULT_SPACING);
    grid.setVgap(DEFAULT_SPACING);
    grid.setPadding(new Insets(SECTION_SPACING));

    TextField nameField = new TextField();
    nameField.setId("varNameField");
    nameField.setPromptText(uiBundle.getString(KEY_DIALOG_VAR_NAME));
    nameField.setPrefWidth(DIALOG_INPUT_WIDTH);

    ComboBox<String> typeComboBox = new ComboBox<>(
        FXCollections.observableArrayList("int", "double", "boolean", "string"));
    typeComboBox.setId("varTypeCombo");
    typeComboBox.setValue("double");
    typeComboBox.setPrefWidth(DIALOG_INPUT_WIDTH);

    TextField valueField = new TextField();
    valueField.setId("varValueField");
    valueField.setPromptText(uiBundle.getString(KEY_DIALOG_VAR_VALUE));
    valueField.setPrefWidth(DIALOG_INPUT_WIDTH);

    TextField descriptionField = new TextField();
    descriptionField.setId("varDescField");
    descriptionField.setPromptText(uiBundle.getString(KEY_DIALOG_VAR_DESC));
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
   * Sets up the result converter for the dialog to handle when the user presses OK. Attempts to
   * create a DynamicVariable from the input; shows an error alert if input is invalid.
   */
  private void setupResultConverter() {
    setResultConverter(dialogButton -> {
      if (dialogButton.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
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
   * Attempts to create a DynamicVariable from the current state of the input fields.
   *
   * @return the created DynamicVariable.
   * @throws IllegalStateException    if any input field is unexpectedly null.
   * @throws IllegalArgumentException if variable creation fails due to invalid data.
   * @throws NullPointerException     if required input (like type) is missing.
   */
  private DynamicVariable createVariableFromInput()
      throws IllegalStateException, IllegalArgumentException, NullPointerException {
    if (nameField == null || typeComboBox == null || valueField == null
        || descriptionField == null) {
      LOG.error("Dialog input fields were not initialized correctly during result conversion.");
      throw new IllegalStateException("Dialog fields not ready. Cannot create variable.");
    }

    return new DynamicVariable(
        nameField.getText().trim(),
        Objects.requireNonNull(typeComboBox.getValue(), "Variable type cannot be null."),
        valueField.getText().trim(),
        descriptionField.getText().trim()
    );
  }


  /**
   * Utility method to display an error alert dialog to the user within the context of this dialog.
   * Applies standard CSS styling.
   *
   * @param title       The title for the error alert window.
   * @param contentText The main error message to display to the user.
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