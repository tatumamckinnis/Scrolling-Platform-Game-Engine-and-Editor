package oogasalad.editor.view.eventui;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import oogasalad.editor.model.data.object.event.ExecutorData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Builds the UI section for managing game event "Outcomes" within the editor.
 * Facilitates selecting outcome types, configuring their parameters, and managing the list.
 *
 * @author Tatum McKinnis
 */
public class OutcomesSectionBuilder {

  private static final Logger LOG = LogManager.getLogger(OutcomesSectionBuilder.class);
  private static final String IDENTIFIERS_PROPERTIES_PATH = "/oogasalad/config/editor/resources/outcomes_section_builder_identifiers.properties";

  private final ResourceBundle uiBundle;
  private final Properties localProps;
  private final Supplier<List<String>> outcomeTypeSupplier;
  private final AddOutcomeHandler addOutcomeHandler;
  private final IntConsumer removeOutcomeHandler;
  private final EditOutcomeParamHandler editOutcomeParamHandler;

  private ComboBox<String> outcomeTypeComboBox;
  private ListView<OutcomeDisplayItem> outcomesListView;
  private VBox parametersPane;

  /**
   * Constructs the OutcomesSectionBuilder with necessary data sources and handler callbacks.
   * Loads local identifiers and parameter definitions from its specific properties file.
   *
   * @param uiBundle                 The resource bundle for UI text.
   * @param outcomeTypeSupplier     Supplies available outcome types (e.g., "Move", "Attack").
   * @param addOutcomeHandler       Handler for creating new outcomes.
   * @param removeOutcomeHandler    Handler for removing the selected outcome by index.
   * @param editOutcomeParamHandler Handler for editing existing outcome parameters.
   * @throws NullPointerException if uiBundle or any handler/supplier is null.
   * @throws RuntimeException if the local properties file cannot be loaded.
   */
  public OutcomesSectionBuilder(ResourceBundle uiBundle,
      Supplier<List<String>> outcomeTypeSupplier,
      AddOutcomeHandler addOutcomeHandler,
      IntConsumer removeOutcomeHandler,
      EditOutcomeParamHandler editOutcomeParamHandler) {
    this.uiBundle = Objects.requireNonNull(uiBundle);
    this.outcomeTypeSupplier = Objects.requireNonNull(outcomeTypeSupplier);
    this.addOutcomeHandler = Objects.requireNonNull(addOutcomeHandler);
    this.removeOutcomeHandler = Objects.requireNonNull(removeOutcomeHandler);
    this.editOutcomeParamHandler = Objects.requireNonNull(editOutcomeParamHandler);
    this.localProps = loadLocalProperties();
  }

  /**
   * Loads the local identifier strings and parameter definitions from the properties file.
   * @return A Properties object containing the loaded definitions.
   * @throws RuntimeException If the properties file cannot be found or read.
   */
  private Properties loadLocalProperties() {
    Properties props = new Properties();
    try (InputStream input = OutcomesSectionBuilder.class.getResourceAsStream(IDENTIFIERS_PROPERTIES_PATH)) {
      if (input == null) {
        LOG.error("Unable to find local properties file: {}", IDENTIFIERS_PROPERTIES_PATH);
        throw new RuntimeException("Missing required local properties file: " + IDENTIFIERS_PROPERTIES_PATH);
      }
      props.load(input);
    } catch (IOException ex) {
      LOG.error("Error loading local properties file: {}", IDENTIFIERS_PROPERTIES_PATH, ex);
      throw new RuntimeException("Error loading local properties file", ex);
    }
    return props;
  }

  /**
   * Retrieves an identifier value (e.g., CSS class name, UI key name) from the loaded local properties,
   * stripping any trailing comments starting with '#'.
   * @param key The key for the identifier.
   * @return The identifier string, without comments.
   * @throws RuntimeException If the key is not found.
   */
  private String getLocalProp(String key) {
    String value = localProps.getProperty(key);
    if (value == null) {
      LOG.error("Missing identifier in OutcomesSectionBuilder local properties file for key: {}", key);
      throw new RuntimeException("Missing identifier in OutcomesSectionBuilder local properties file for key: " + key);
    }

    int commentIndex = value.indexOf('#');
    if (commentIndex != -1) {
      value = value.substring(0, commentIndex);
    }
    value = value.trim();

    if (value.isEmpty()) {
      LOG.error("Identifier value is empty after processing in OutcomesSectionBuilder for key: {}", key);
      throw new RuntimeException("Identifier value is empty after processing in OutcomesSectionBuilder for key: " + key);
    }
    return value;
  }

  /**
   * Builds the full UI node representing the "Outcomes" section of the editor.
   *
   * @return A VBox node containing all outcome-related controls and displays.
   */
  public Node build() {
    VBox sectionPane = new VBox();
    sectionPane.setId(getLocalProp("id.sectionVbox"));
    sectionPane.getStyleClass().add(getLocalProp("style.inputSubSection"));

    Label header = createHeaderLabel("key.outcomesHeader");
    HBox outcomeSelectionRow = createOutcomeSelectionRow();
    setupOutcomesListView();
    Node parametersSection = buildParametersSection();

    sectionPane.getChildren().addAll(
        header,
        outcomeSelectionRow,
        outcomesListView,
        parametersSection
    );
    VBox.setVgrow(outcomesListView, Priority.SOMETIMES);
    if (parametersSection.getParent() instanceof ScrollPane) {
      VBox.setVgrow(parametersSection.getParent(), Priority.SOMETIMES);
    }


    LOG.debug("Outcomes section UI built.");
    return sectionPane;
  }

  /**
   * Creates the HBox row containing controls for selecting an outcome type and adding/removing outcomes.
   *
   * @return HBox node for outcome selection.
   */
  private HBox createOutcomeSelectionRow() {
    HBox selectionBox = new HBox();
    selectionBox.setId(getLocalProp("id.outcomeSelectionRow"));
    selectionBox.setAlignment(Pos.CENTER_LEFT);

    setupOutcomeTypeComboBox();

    Button addOutcomeButton = createButton("key.addOutcomeButton", e -> handleAddOutcomeAction());
    addOutcomeButton.setId(getLocalProp("id.addOutcomeButton"));
    addOutcomeButton.setMaxWidth(Double.MAX_VALUE);

    Button removeOutcomeButton = createButton("key.removeOutcomeButton", e -> handleRemoveOutcomeAction());
    removeOutcomeButton.setId(getLocalProp("id.removeOutcomeButton"));

    selectionBox.getChildren().addAll(outcomeTypeComboBox, addOutcomeButton, removeOutcomeButton);
    HBox.setHgrow(outcomeTypeComboBox, Priority.ALWAYS);

    return selectionBox;
  }

  /**
   * Initializes and configures the ComboBox used for selecting the type of outcome to add.
   */
  private void setupOutcomeTypeComboBox() {
    outcomeTypeComboBox = new ComboBox<>(
        FXCollections.observableArrayList(outcomeTypeSupplier.get()));
    outcomeTypeComboBox.setId(getLocalProp("id.outcomeTypeComboBox"));
    outcomeTypeComboBox.setPromptText(uiBundle.getString(getLocalProp("key.promptSelectOutcome")));
    outcomeTypeComboBox.setMaxWidth(Double.MAX_VALUE);
  }

  /**
   * Initializes and configures the ListView used for displaying the added outcomes.
   */
  private void setupOutcomesListView() {
    outcomesListView = new ListView<>();
    outcomesListView.setId(getLocalProp("id.outcomesListView"));
    outcomesListView.getStyleClass().add(getLocalProp("style.dataListView"));

    outcomesListView.getSelectionModel().selectedItemProperty().addListener(
        (obs, oldVal, newVal) -> updateParametersPane(newVal)
    );
  }

  /**
   * Builds the parameters section UI container.
   *
   * @return Node representing the container for the parameters section.
   */
  private Node buildParametersSection() {
    VBox container = new VBox();
    container.setId(getLocalProp("id.parametersContainer"));
    Label header = createHeaderLabel("key.executorParametersHeader");

    parametersPane = new VBox();
    parametersPane.setId(getLocalProp("id.parametersPane"));

    ScrollPane scrollPane = new ScrollPane(parametersPane);
    scrollPane.setFitToWidth(true);
    scrollPane.setId(getLocalProp("id.parametersScrollPane"));

    container.getChildren().addAll(header, scrollPane);
    return container;
  }

  /**
   * Handles the action event triggered by the "Add Outcome" button.
   */
  private void handleAddOutcomeAction() {
    String selectedType = outcomeTypeComboBox.getSelectionModel().getSelectedItem();
    if (selectedType != null && !selectedType.trim().isEmpty()) {
      addOutcomeHandler.handle(selectedType.trim());
      outcomeTypeComboBox.getSelectionModel().clearSelection();
      outcomeTypeComboBox.setPromptText(uiBundle.getString(getLocalProp("key.promptSelectOutcome")));
    } else {
      LOG.warn(uiBundle.getString(getLocalProp("key.warnNoOutcomeType")));
    }
  }

  /**
   * Handles the action event triggered by the "Remove Outcome" button.
   */
  private void handleRemoveOutcomeAction() {
    OutcomeDisplayItem selected = outcomesListView.getSelectionModel().getSelectedItem();
    if (selected != null) {
      removeOutcomeHandler.accept(selected.getIndex());
    } else {
      LOG.warn(uiBundle.getString(getLocalProp("key.warnNoOutcomeSelected")));
    }
  }

  /**
   * Updates the parameters editing pane based on the currently selected outcome item.
   * Rebuilds the parameter grid dynamically based on local properties file definitions.
   *
   * @param selectedItem The currently selected {@link OutcomeDisplayItem}, or null if none selected.
   */
  private void updateParametersPane(OutcomeDisplayItem selectedItem) {
    parametersPane.getChildren().clear();
    if (selectedItem == null || selectedItem.getData() == null) {
      return;
    }

    ExecutorData data = selectedItem.getData();
    String executorName = data.getExecutorName();
    if (executorName == null) {
      LOG.warn("Selected outcome has null executor name. Cannot display parameters.");
      return;
    }

    GridPane grid = createParametersGrid();
    String paramCountKey = executorName + getLocalProp("param.baseKey") + getLocalProp("param.countSuffix");
    String paramCountStr = localProps.getProperty(paramCountKey);

    if (paramCountStr != null) {
      int commentIdx = paramCountStr.indexOf('#');
      if (commentIdx != -1) {
        paramCountStr = paramCountStr.substring(0, commentIdx).trim();
      }
    }


    if (paramCountStr == null || paramCountStr.trim().isEmpty()) {
      LOG.warn("Parameter count definition missing or empty for executor '{}' using key '{}'. Parameter pane will be empty.",
          executorName, paramCountKey);
      return;
    }

    try {
      int paramCount = Integer.parseInt(paramCountStr);

      for (int i = 1; i <= paramCount; i++) {
        String paramBaseKeyStr = executorName + getLocalProp("param.baseKey") + i;
        String name = getLocalProp(paramBaseKeyStr + getLocalProp("param.nameSuffix"));
        String type = getLocalProp(paramBaseKeyStr + getLocalProp("param.typeSuffix"));
        String rawDescription = localProps.getProperty(paramBaseKeyStr + getLocalProp("param.descSuffix"), "");
        String rawDefaultValue = localProps.getProperty(paramBaseKeyStr + getLocalProp("param.defaultSuffix"), "");

        String description = cleanPropertyValue(rawDescription);
        String defaultValue = cleanPropertyValue(rawDefaultValue);


        if (name == null || type == null) {
          LOG.error("Missing name or type definition for parameter index {} of executor {}", i, executorName);
          String errorMsgKey = getLocalProp("key.errorParameterDefinition");
          String errorMsg = String.format(uiBundle.getString(errorMsgKey), i, executorName);
          Label errorLabel = new Label(errorMsg);
          grid.add(errorLabel, 0, i-1, 2, 1);
          continue;
        }
        addParameterRow(grid, i - 1, name, type, description, defaultValue, selectedItem);
      }
      parametersPane.getChildren().add(grid);
      LOG.trace("Parameters pane updated for outcome: {}", executorName);

    } catch (NumberFormatException e) {
      LOG.error("Invalid number format for parameter count for key '{}' in {}: {}", paramCountKey, IDENTIFIERS_PROPERTIES_PATH, paramCountStr, e);
      String errorMsgKey = getLocalProp("key.warnInvalidNumber");
      String errorMsg = String.format(uiBundle.getString(errorMsgKey), executorName);
      Label errorLabel = new Label(errorMsg);
      parametersPane.getChildren().add(errorLabel);
    } catch (MissingResourceException e) {
      LOG.error("Missing UI text resource for parameter UI construction.", e);
      Label errorLabel = new Label("UI Text Error: " + e.getKey());
      parametersPane.getChildren().add(errorLabel);
    } catch (Exception e) {
      LOG.error("Unexpected error building parameters UI for executor '{}'", executorName, e);
      String errorMsgKey = getLocalProp("key.errorParameterLoad");
      String errorMsg = String.format(uiBundle.getString(errorMsgKey), executorName);
      Label errorLabel = new Label(errorMsg);
      parametersPane.getChildren().add(errorLabel);
    }
  }


  /**
   * Helper to clean property values by removing comments and trimming.
   */
  private String cleanPropertyValue(String rawValue) {
    if (rawValue == null) return "";
    int commentIndex = rawValue.indexOf('#');
    if (commentIndex != -1) {
      rawValue = rawValue.substring(0, commentIndex);
    }
    return rawValue.trim();
  }

  /**
   * Creates and configures a standard GridPane for displaying parameters.
   *
   * @return A new GridPane instance.
   */
  private GridPane createParametersGrid() {
    GridPane grid = new GridPane();
    grid.setId(getLocalProp("id.parametersGrid"));
    grid.getStyleClass().add("parameter-grid");
    return grid;
  }

  /**
   * Adds a single parameter row (Label + Input Control) to the specified GridPane.
   *
   * @param grid         The GridPane to add the row to.
   * @param rowIndex     The row index for the new row.
   * @param paramName    The display name of the parameter.
   * @param paramType    The type string (e.g., "String", "Double", "Boolean", "Dropdown:A,B,C") loaded from properties.
   * @param description  Tooltip description for the parameter.
   * @param defaultValue Default value string from properties.
   * @param item         The OutcomeDisplayItem being edited.
   */
  private void addParameterRow(GridPane grid, int rowIndex, String paramName, String paramType, String description, String defaultValue, OutcomeDisplayItem item) {
    String typeDisplayKey;
    Control inputControl;
    String currentValue = getCurrentValueAsString(item.getData(), paramName, paramType, defaultValue);
    String typeIdBoolean = getLocalProp("param.typeIdBoolean");
    String typeIdInteger = getLocalProp("param.typeIdInteger");
    String typeIdDouble = getLocalProp("param.typeIdDouble");
    String typeIdDropdownPrefix = getLocalProp("param.typeIdDropdownPrefix");

    try {
      if (paramType.equalsIgnoreCase(typeIdBoolean)) {
        typeDisplayKey = getLocalProp("key.paramTypeBoolean");
        CheckBox checkBox = new CheckBox();
        try {
          checkBox.setSelected(Boolean.parseBoolean(currentValue));
        } catch (Exception e) {
          LOG.warn("Could not parse boolean value '{}' for param '{}'. Defaulting to false.", currentValue, paramName);
          checkBox.setSelected(false);
        }
        checkBox.setOnAction(e -> handleBooleanParamUpdate(item, paramName, checkBox.isSelected()));
        inputControl = checkBox;
      } else if (paramType.equalsIgnoreCase(typeIdInteger) || paramType.equalsIgnoreCase(typeIdDouble)) {
        typeDisplayKey = paramType.equalsIgnoreCase(typeIdInteger) ? getLocalProp("key.paramTypeInteger") : getLocalProp("key.paramTypeDouble");
        TextField textField = new TextField(currentValue);
        textField.setOnAction(e -> handleNumericParamUpdate(item, paramName, paramType, textField));
        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
          if (!newVal) handleNumericParamUpdate(item, paramName, paramType, textField);
        });
        inputControl = textField;
      } else if (paramType.toUpperCase().startsWith(typeIdDropdownPrefix.toUpperCase())) {
        typeDisplayKey = getLocalProp("key.paramTypeDropdown");
        String optionsString = paramType.substring(typeIdDropdownPrefix.length());
        String[] options = optionsString.split(",");
        ComboBox<String> comboBox = new ComboBox<>(FXCollections.observableArrayList(options));
        if (List.of(options).contains(currentValue)) {
          comboBox.setValue(currentValue);
        } else if (!currentValue.isEmpty()){
          LOG.warn("Current value '{}' for dropdown param '{}' is not in options [{}]. Setting empty.", currentValue, paramName, optionsString);
          comboBox.setValue(null);
        } else {
          comboBox.setValue(null);
        }

        comboBox.setOnAction(e -> handleStringParamUpdate(item, paramName, comboBox.getValue()));
        inputControl = comboBox;
      } else {
        typeDisplayKey = getLocalProp("key.paramTypeString");
        TextField textField = new TextField(currentValue);
        textField.setOnAction(e -> handleStringParamUpdate(item, paramName, textField.getText()));
        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
          if (!newVal) handleStringParamUpdate(item, paramName, textField.getText());
        });
        inputControl = textField;
      }
    } catch (MissingResourceException e) {
      LOG.error("Missing resource key for parameter type display: {}", e.getKey(), e);
      typeDisplayKey = "key.paramTypeError";
      inputControl = new Label(uiBundle.getString(getLocalProp("key.labelError")));
      inputControl.setTooltip(new Tooltip("Missing UI text: " + e.getKey()));
    } catch (IllegalArgumentException e) {
      LOG.error("Error parsing current value for parameter '{}' of type '{}': {}", paramName, paramType, currentValue, e);
      String errorMsgKey = getLocalProp("key.errorParameterParse");
      String errorMsg = String.format(uiBundle.getString(errorMsgKey), paramName, paramType, currentValue);
      inputControl = new Label(uiBundle.getString(getLocalProp("key.labelError")));
      inputControl.setTooltip(new Tooltip(errorMsg));
      typeDisplayKey = getLocalProp("key.paramTypeError");
    } catch (Exception e) {
      LOG.error("Unexpected error creating input control for param '{}', type '{}'", paramName, paramType, e);
      typeDisplayKey = getLocalProp("key.paramTypeError");
      inputControl = new Label(uiBundle.getString(getLocalProp("key.labelError")));
      inputControl.setTooltip(new Tooltip("Error creating control: " + e.getMessage()));
    }

    Label nameLabel = new Label(String.format("%s %s:", paramName, uiBundle.getString(typeDisplayKey)));
    if (description != null && !description.isEmpty()) {
      Tooltip tip = new Tooltip(description);
      nameLabel.setTooltip(tip);
      if (inputControl != null) inputControl.setTooltip(tip);
    }

    grid.add(nameLabel, 0, rowIndex);
    grid.add(inputControl, 1, rowIndex);
    GridPane.setHgrow(inputControl, Priority.ALWAYS);
  }


  /**
   * Retrieves the current value of a parameter from ExecutorData, falling back to defaultValue.
   */
  private String getCurrentValueAsString(ExecutorData data, String paramName, String paramType, String defaultValue) {
    Map<String, String> stringParams = data.getStringParams();
    Map<String, Double> doubleParams = data.getDoubleParams();
    String typeIdDouble = getLocalProp("param.typeIdDouble");
    String typeIdInteger = getLocalProp("param.typeIdInteger");
    String typeIdBoolean = getLocalProp("param.typeIdBoolean");


    if (stringParams != null && stringParams.containsKey(paramName)) {
      if (paramType.equalsIgnoreCase(typeIdBoolean)) {
        return String.valueOf(Boolean.parseBoolean(stringParams.get(paramName)));
      }
      return stringParams.get(paramName);
    }
    if ((paramType.equalsIgnoreCase(typeIdDouble) || paramType.equalsIgnoreCase(typeIdInteger)) && doubleParams != null && doubleParams.containsKey(paramName)) {
      Double val = doubleParams.get(paramName);
      if (val != null) {
        if (paramType.equalsIgnoreCase(typeIdInteger) && val == Math.floor(val) && !val.isInfinite()) {
          return String.valueOf(val.intValue());
        }
        return String.valueOf(val);
      }
    }
    if (paramType.equalsIgnoreCase(typeIdBoolean)) {
      return String.valueOf(Boolean.parseBoolean(defaultValue));
    }
    return defaultValue;
  }

  /**
   * Handles the update for a String parameter (or Dropdown).
   */
  private void handleStringParamUpdate(OutcomeDisplayItem item, String key, String newValue) {
    if (newValue == null) newValue = "";
    editOutcomeParamHandler.handle(item.getIndex(), key, newValue);
    LOG.trace("String parameter '{}' updated to: {}", key, newValue);
  }

  /**
   * Handles the update for a Boolean parameter. Stores as String "true"/"false".
   */
  private void handleBooleanParamUpdate(OutcomeDisplayItem item, String key, boolean newValue) {
    editOutcomeParamHandler.handle(item.getIndex(), key, String.valueOf(newValue));
    LOG.trace("Boolean parameter '{}' updated to: {}", key, newValue);
  }

  /**
   * Handles the update for a numeric (Double/Integer) parameter.
   */
  private void handleNumericParamUpdate(OutcomeDisplayItem item, String key, String paramType, TextField textField) {
    String newValueText = textField.getText();
    String typeIdInteger = getLocalProp("param.typeIdInteger");
    String typeIdDouble = getLocalProp("param.typeIdDouble");

    try {
      if (paramType.equalsIgnoreCase(typeIdInteger)) {
        Integer intVal = Integer.parseInt(newValueText);
        editOutcomeParamHandler.handle(item.getIndex(), key, intVal.doubleValue());
        LOG.trace("Integer parameter '{}' updated to: {}", key, intVal);
      } else if (paramType.equalsIgnoreCase(typeIdDouble)) {
        Double doubleVal = Double.parseDouble(newValueText);
        editOutcomeParamHandler.handle(item.getIndex(), key, doubleVal);
        LOG.trace("Double parameter '{}' updated to: {}", key, doubleVal);
      } else {
        LOG.error("handleNumericParamUpdate called with non-numeric type: {}", paramType);
        return;
      }
      textField.setStyle("");
      textField.setTooltip(null);
    } catch (NumberFormatException e) {
      LOG.warn("Invalid {} format for param '{}': {}", paramType, key, newValueText);
      textField.setStyle("-fx-text-fill: red; -fx-border-color: red;");
      String errorMsgKey = getLocalProp("key.warnInvalidNumericInput");
      String errorTooltip = String.format(uiBundle.getString(errorMsgKey), paramType, newValueText);
      textField.setTooltip(new Tooltip(errorTooltip));
    } catch (Exception e) {
      LOG.error("Error handling numeric parameter update for '{}': {}", key, e.getMessage(), e);
      textField.setStyle("-fx-text-fill: red; -fx-border-color: red;");
      textField.setTooltip(new Tooltip("Error processing value: " + e.getMessage()));
    }
  }


  /**
   * Updates the list view of outcomes shown in the UI with a new list of {@code ExecutorData}.
   *
   * @param outcomes A list of {@link ExecutorData} representing the outcomes to display, or null/empty to clear.
   */
  public void updateOutcomesListView(List<ExecutorData> outcomes) {
    ObservableList<OutcomeDisplayItem> displayItems = FXCollections.observableArrayList();
    if (outcomes != null) {
      for (int i = 0; i < outcomes.size(); i++) {
        ExecutorData outcomeData = outcomes.get(i);
        if (outcomeData != null) {
          displayItems.add(new OutcomeDisplayItem(i, outcomeData));
        } else {
          LOG.warn("Encountered null outcome data at index {}", i);
        }
      }
    }
    outcomesListView.setItems(displayItems);
    LOG.trace("Outcomes list view updated with {} items.", displayItems.size());
  }

  /**
   * Creates a styled header label using text retrieved from the resource bundle.
   *
   * @param localPropKey The key corresponding to the header text identifier in the local properties,
   * which maps to the key in the uiBundle.
   * @return A styled {@code Label}.
   */
  private Label createHeaderLabel(String localPropKey) {
    String uiBundleKey = getLocalProp(localPropKey);
    Label label = new Label(uiBundle.getString(uiBundleKey));
    label.getStyleClass().add(getLocalProp("style.sectionHeader"));
    label.setMaxWidth(Double.MAX_VALUE);
    return label;
  }

  /**
   * Creates a styled {@code Button} with text and action handler.
   *
   * @param localPropKey The key in the local properties file for the button's UI text key.
   * @param handler   The event handler for the button click.
   * @return A configured and styled {@code Button}.
   */
  private Button createButton(String localPropKey, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
    String buttonText;
    String actualUiKey = getLocalProp(localPropKey);

    buttonText = uiBundle.getString(actualUiKey);


    Button button = new Button(buttonText);
    button.setOnAction(handler);
    button.getStyleClass().add(getLocalProp("style.actionButton"));

    if (actualUiKey.equals(getLocalProp("key.removeOutcomeButton"))) {
      button.getStyleClass().add(getLocalProp("style.removeButton"));
      button.setMaxWidth(Region.USE_PREF_SIZE);
    } else {
      button.setMaxWidth(Double.MAX_VALUE);
      HBox.setHgrow(button, Priority.ALWAYS);
    }

    return button;
  }
}