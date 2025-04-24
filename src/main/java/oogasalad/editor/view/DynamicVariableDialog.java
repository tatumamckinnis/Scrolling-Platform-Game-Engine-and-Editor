package oogasalad.editor.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
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
 * Configuration like identifiers and CSS paths are loaded externally.
 * UI text (labels, errors) is loaded from a {@link ResourceBundle}.
 * Styling and layout constants are controlled via external CSS.
 * @author Tatum McKinnis
 */
public class DynamicVariableDialog extends Dialog<DynamicVariable> {

  private static final Logger LOG = LogManager.getLogger(DynamicVariableDialog.class);

  private static final String IDENTIFIERS_PROPERTIES_PATH = "/oogasalad/config/editor/resources/dynamic_variable_dialog_identifiers.properties";

  private final ResourceBundle uiBundle;
  private final Properties identifierProps;
  private TextField nameField;
  private ComboBox<String> typeComboBox;
  private TextField valueField;
  private TextField descriptionField;
  private ButtonType addButtonType;

  /**
   * Constructs a new dialog for creating a {@link DynamicVariable}.
   * Loads identifiers and UI text from external resources.
   * Sets up the dialog's layout, controls, validation, and result conversion logic.
   *
   * @param uiBundle ResourceBundle for localized UI text (errors, labels, button text, etc.).
   * @throws RuntimeException if the identifiers properties file cannot be loaded.
   * @throws NullPointerException if uiBundle is null.
   */
  public DynamicVariableDialog(ResourceBundle uiBundle) {
    this.uiBundle = Objects.requireNonNull(uiBundle, "UI Bundle cannot be null.");
    this.identifierProps = loadIdentifierProperties();

    setTitle(uiBundle.getString(getId("key.dialogAddVarTitle")));
    setupDialogPane();
    setupResultConverter();
    setupValidationBinding();
    LOG.debug("DynamicVariableDialog initialized.");
  }

  /**
   * Loads the identifier strings (keys, CSS classes, IDs, paths) from the properties file specified by {@link #IDENTIFIERS_PROPERTIES_PATH}.
   *
   * @return A Properties object containing the loaded identifiers.
   * @throws RuntimeException If the properties file cannot be found or read.
   */
  private Properties loadIdentifierProperties() {
    Properties props = new Properties();
    try (InputStream input = DynamicVariableDialog.class.getResourceAsStream(IDENTIFIERS_PROPERTIES_PATH)) {
      if (input == null) {
        LOG.error("Unable to find identifiers properties file: {}", IDENTIFIERS_PROPERTIES_PATH);
        throw new RuntimeException("Missing required identifiers properties file: " + IDENTIFIERS_PROPERTIES_PATH);
      }
      props.load(input);
    } catch (IOException ex) {
      LOG.error("Error loading identifiers properties file: {}", IDENTIFIERS_PROPERTIES_PATH, ex);
      throw new RuntimeException("Error loading identifiers properties file", ex);
    }
    return props;
  }

  /**
   * Retrieves an identifier value (e.g., CSS class name, resource key) from the loaded properties.
   * Throws a RuntimeException if the key is not found, ensuring that missing identifiers are caught early.
   *
   * @param key The key corresponding to the identifier in the properties file.
   * @return The identifier string associated with the key.
   * @throws RuntimeException If the key is not found in the loaded properties.
   */
  private String getId(String key) {
    String value = identifierProps.getProperty(key);
    if (value == null || value.trim().isEmpty()) {
      LOG.error("Missing identifier in properties file for key: {}", key);
      throw new RuntimeException("Missing identifier in properties file for key: " + key);
    }
    return value;
  }

  /**
   * Sets up the main dialog pane, including applying CSS, adding standard buttons (Add, Cancel),
   * creating the content grid with input fields, and looking up references to those fields by ID.
   * Handles potential errors during CSS loading or field lookup.
   */
  private void setupDialogPane() {
    String cssPath = getId("css.path");
    try {
      String cssUri = Objects.requireNonNull(getClass().getResource(cssPath)).toExternalForm();
      getDialogPane().getStylesheets().add(cssUri);
      getDialogPane().getStyleClass().add(getId("style.dialogPane"));
    } catch (Exception e) {
      LOG.warn("Could not load CSS from path '{}': {}. Using default styles.", cssPath, e.getMessage());
    }

    this.addButtonType = new ButtonType(uiBundle.getString(getId("key.dialogAddButton")),
        ButtonBar.ButtonData.OK_DONE);
    getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

    GridPane grid = createDynamicVariableDialogGrid();
    getDialogPane().setContent(grid);

    nameField = (TextField) grid.lookup("#" + getId("id.nameField"));
    typeComboBox = (ComboBox<String>) grid.lookup("#" + getId("id.typeCombo"));
    valueField = (TextField) grid.lookup("#" + getId("id.valueField"));
    descriptionField = (TextField) grid.lookup("#" + getId("id.descField"));

    if (nameField == null || typeComboBox == null || valueField == null || descriptionField == null) {
      String errorMsg = uiBundle.getString(getId("key.errorDialogInitContent"));
      LOG.error(errorMsg);
      showErrorAlert(uiBundle.getString(getId("key.errorDialogInitTitle")), errorMsg);
      Node addButtonNode = getDialogPane().lookupButton(addButtonType);
      if (addButtonNode != null) {
        addButtonNode.setDisable(true);
      }
    }
  }

  /**
   * Creates and populates the GridPane layout containing input fields (TextFields, ComboBox)
   * and their corresponding Labels for the dialog form. Field IDs and ComboBox content
   * are sourced from the loaded identifier properties. Layout properties like spacing and
   * padding are expected to be controlled via external CSS.
   *
   * @return A fully populated GridPane containing the dialog's input form controls.
   */
  private GridPane createDynamicVariableDialogGrid() {
    GridPane grid = new GridPane();
    grid.setId(getId("id.gridPane"));

    nameField = new TextField();
    nameField.setId(getId("id.nameField"));
    nameField.setPromptText(uiBundle.getString(getId("key.dialogVarName")));

    List<String> types = Arrays.stream(getId("combobox.types").split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toList());
    typeComboBox = new ComboBox<>(FXCollections.observableArrayList(types));
    typeComboBox.setId(getId("id.typeCombo"));
    typeComboBox.setValue(getId("combobox.defaultType"));

    valueField = new TextField();
    valueField.setId(getId("id.valueField"));
    valueField.setPromptText(uiBundle.getString(getId("key.dialogVarValue")));

    descriptionField = new TextField();
    descriptionField.setId(getId("id.descField"));
    String descPrompt = uiBundle.getString(getId("key.dialogVarDesc"));
    String optionalSuffix = uiBundle.getString(getId("key.labelOptionalSuffix"));
    descriptionField.setPromptText(descPrompt + optionalSuffix);

    grid.add(new Label(uiBundle.getString(getId("key.dialogVarName")) + ":"), 0, 0);
    grid.add(nameField, 1, 0);
    grid.add(new Label(uiBundle.getString(getId("key.dialogVarType")) + ":"), 0, 1);
    grid.add(typeComboBox, 1, 1);
    grid.add(new Label(uiBundle.getString(getId("key.dialogVarValue")) + ":"), 0, 2);
    grid.add(valueField, 1, 2);
    grid.add(new Label(uiBundle.getString(getId("key.dialogVarDesc")) + ":"), 0, 3);
    grid.add(descriptionField, 1, 3);

    return grid;
  }

  /**
   * Sets up listeners on the required input fields (name, type, value) to dynamically
   * enable or disable the 'Add' button. The button is disabled if any of these fields are empty.
   * Handles cases where field references might be null (e.g., if lookup failed).
   */
  private void setupValidationBinding() {
    if (nameField == null || typeComboBox == null || valueField == null) {
      LOG.error("Cannot set up validation binding: Input fields are not initialized or found.");
      return;
    }

    Node addButton = getDialogPane().lookupButton(addButtonType);
    if (addButton != null) {
      updateButtonState(addButton);

      nameField.textProperty().addListener((obs, ov, nv) -> updateButtonState(addButton));
      typeComboBox.valueProperty().addListener((obs, ov, nv) -> updateButtonState(addButton));
      valueField.textProperty().addListener((obs, ov, nv) -> updateButtonState(addButton));

    } else {
      LOG.error("Could not find the Add button node to set up validation binding.");
    }
  }

  /**
   * Updates the disabled state of the provided 'Add' button Node.
   * The button is disabled if the name field is empty, the type ComboBox has no selection,
   * or the value field is empty. Checks for null field references before accessing properties.
   *
   * @param addButton The Node representing the Add button whose state needs updating.
   */
  private void updateButtonState(Node addButton) {
    if (nameField == null || typeComboBox == null || valueField == null) {
      addButton.setDisable(true);
      return;
    }
    boolean disabled = nameField.getText().trim().isEmpty()
        || typeComboBox.getValue() == null
        || valueField.getText().trim().isEmpty();
    addButton.setDisable(disabled);
  }


  /**
   * Configures the dialog's result converter. This logic is executed when a dialog button
   * (like 'Add' or 'Cancel') is pressed. If the 'Add' button was pressed, it attempts
   * to create a {@link DynamicVariable} from the current input field values.
   * Catches exceptions during variable creation, shows an error alert, and returns null
   * to keep the dialog open for correction. Returns null for 'Cancel' or dialog closure.
   */
  private void setupResultConverter() {
    setResultConverter(dialogButton -> {
      if (dialogButton == addButtonType) {
        try {
          return createVariableFromInput();
        } catch (IllegalArgumentException | NullPointerException | IllegalStateException ex) {
          LOG.warn("Invalid input or error creating dynamic variable: {}", ex.getMessage());
          showErrorAlert(uiBundle.getString(getId("key.errorInvalidInputTitle")), ex.getMessage());
          return null;
        }
      }
      return null;
    });
  }

  /**
   * Creates a {@link DynamicVariable} instance using the current values entered into the
   * dialog's input fields. It first ensures field references are valid, then retrieves
   * and trims the input values, performs validation via {@link #validateVariableInputs},
   * and finally constructs the DynamicVariable object.
   *
   * @return The newly created DynamicVariable based on the dialog inputs.
   * @throws IllegalStateException if any required input field reference (nameField, typeComboBox, etc.) is null,
   * indicating an internal dialog initialization issue.
   * @throws IllegalArgumentException if validation fails for name or value (e.g., empty).
   * @throws NullPointerException if validation fails for type (e.g., no selection).
   */
  private DynamicVariable createVariableFromInput() {
    if (nameField == null || typeComboBox == null || valueField == null || descriptionField == null) {
      LOG.error("Dialog input fields reference is null during result conversion.");
      throw new IllegalStateException(uiBundle.getString(getId("key.errorInternalFields")));
    }

    String name = nameField.getText().trim();
    String type = typeComboBox.getValue();
    String value = valueField.getText().trim();
    String description = descriptionField.getText().trim();

    validateVariableInputs(name, type, value);

    return new DynamicVariable(name, type, value, description);
  }

  /**
   * Validates the essential inputs required for creating a DynamicVariable (name, type, value).
   * Throws specific exceptions with localized error messages if validation fails.
   *
   * @param name  The variable name entered by the user (must not be empty).
   * @param type  The variable type selected by the user (must not be null).
   * @param value The variable value entered by the user (must not be empty).
   * @throws IllegalArgumentException if name or value is empty. Uses localized messages.
   * @throws NullPointerException     if type is null. Uses localized message.
   */
  private void validateVariableInputs(String name, String type, String value) {
    if (name.isEmpty()) {
      throw new IllegalArgumentException(uiBundle.getString(getId("key.errorVarNameEmpty")));
    }
    if (type == null) {
      throw new NullPointerException(uiBundle.getString(getId("key.errorVarTypeNull")));
    }
    if (value.isEmpty()) {
      throw new IllegalArgumentException(uiBundle.getString(getId("key.errorVarValueEmpty")));
    }
  }

  /**
   * Displays a standard JavaFX error alert dialog to the user with the specified title and content text.
   * Attempts to apply the application's CSS styling (loaded via identifier properties) to the alert pane.
   *
   * @param title       The title to display in the error alert window bar.
   * @param contentText The main error message text to display in the alert body.
   */
  private void showErrorAlert(String title, String contentText) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(contentText);

    String cssPath = getId("css.path");
    try {
      String cssUri = Objects.requireNonNull(getClass().getResource(cssPath)).toExternalForm();
      alert.getDialogPane().getStylesheets().add(cssUri);
    } catch (Exception e) {
      LOG.warn("Could not apply CSS from path '{}' to error alert: {}", cssPath, e.getMessage());
    }

    alert.showAndWait();
  }
}