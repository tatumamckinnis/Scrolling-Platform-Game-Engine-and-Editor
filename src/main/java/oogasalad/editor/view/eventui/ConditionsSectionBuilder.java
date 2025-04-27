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
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
 * Builds the UI section for managing Conditions associated with an Event.
 * Handles condition groups, adding/removing conditions by type, provides a dropdown
 * to select the target group for adding conditions, and provides UI for parameter
 * editing based on definitions in properties files.
 *
 * @author Tatum McKinnis
 */
public class ConditionsSectionBuilder {

  private static final Logger LOG = LogManager.getLogger(ConditionsSectionBuilder.class);

  private static final String IDENTIFIERS_PROPERTIES_PATH = "/oogasalad/config/editor/resources/conditions_identifiers.properties";

  private final ResourceBundle uiBundle;
  private final Properties localProps;
  private final Supplier<List<String>> conditionTypeSupplier;
  private final Runnable addGroupHandler;
  private final IntConsumer removeGroupHandler;
  private final AddConditionHandler addConditionHandler;
  private final RemoveConditionHandler removeConditionHandler;
  private final EditConditionParamHandler editConditionParamHandler;

  private ComboBox<Integer> conditionGroupComboBox;
  private ComboBox<String> conditionTypeComboBox;
  private ListView<ConditionDisplayItem> conditionsListView;
  private VBox parametersPane;


  /**
   * Constructs a ConditionsSectionBuilder.
   * Initializes required handlers, suppliers, and the UI resource bundle.
   * Loads local identifiers and parameter definitions from its specific properties file.
   *
   * @param uiBundle                 The resource bundle for UI text.
   * @param conditionTypeSupplier    Supplier for available condition types.
   * @param addGroupHandler          Handler for adding a new condition group.
   * @param removeGroupHandler       Handler for removing a condition group by index.
   * @param addConditionHandler      Handler for adding a condition to a group.
   * @param removeConditionHandler   Handler for removing a condition from a group.
   * @param editConditionParamHandler Handler for editing a condition's parameter.
   * @throws NullPointerException if uiBundle or any handler/supplier is null.
   * @throws RuntimeException if the local properties file cannot be found or loaded.
   */
  public ConditionsSectionBuilder(ResourceBundle uiBundle,
      Supplier<List<String>> conditionTypeSupplier,
      Runnable addGroupHandler,
      IntConsumer removeGroupHandler,
      AddConditionHandler addConditionHandler,
      RemoveConditionHandler removeConditionHandler,
      EditConditionParamHandler editConditionParamHandler) {
    this.uiBundle = Objects.requireNonNull(uiBundle);
    this.conditionTypeSupplier = Objects.requireNonNull(conditionTypeSupplier);
    this.addGroupHandler = Objects.requireNonNull(addGroupHandler);
    this.removeGroupHandler = Objects.requireNonNull(removeGroupHandler);
    this.addConditionHandler = Objects.requireNonNull(addConditionHandler);
    this.removeConditionHandler = Objects.requireNonNull(removeConditionHandler);
    this.editConditionParamHandler = Objects.requireNonNull(editConditionParamHandler);
    this.localProps = loadLocalProperties();
  }

  /**
   * Loads the local identifier strings and parameter definitions from the properties file
   * specified by {@link #IDENTIFIERS_PROPERTIES_PATH}.
   *
   * @return A Properties object containing the loaded definitions.
   * @throws RuntimeException If the properties file cannot be found or read.
   */
  private Properties loadLocalProperties() {
    Properties props = new Properties();
    try (InputStream input = ConditionsSectionBuilder.class.getResourceAsStream(IDENTIFIERS_PROPERTIES_PATH)) {
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
   * Retrieves an identifier value (e.g., CSS class name, UI key name) from the loaded local properties.
   * Throws a RuntimeException if the key is not found.
   *
   * @param key The key corresponding to the identifier in the properties file.
   * @return The identifier string associated with the key.
   * @throws RuntimeException If the key is not found in the loaded properties.
   */
  private String getLocalProp(String key) {
    String value = localProps.getProperty(key);
    if (value == null || value.trim().isEmpty()) {
      LOG.error("Missing identifier in local properties file for key: {}", key);
      throw new RuntimeException("Missing identifier in local properties file for key: " + key);
    }
    return value;
  }


  /**
   * Builds the entire UI Node for the conditions management section.
   *
   * @return The root Node of the conditions section UI.
   */
  public Node build() {
    VBox sectionPane = new VBox();
    sectionPane.setId(getLocalProp("id.sectionPane"));
    sectionPane.getStyleClass().add(getLocalProp("style.inputSubSection"));

    Label header = createHeaderLabel("key.conditionsHeader");
    HBox groupButtonRow = createGroupButtonRow();
    HBox conditionSelectionRow = createConditionSelectionRow();
    setupConditionsListView();
    Node parametersSection = buildParametersSection();

    sectionPane.getChildren().addAll(
        header,
        groupButtonRow,
        conditionSelectionRow,
        conditionsListView,
        parametersSection
    );

    VBox.setVgrow(conditionsListView, Priority.SOMETIMES);
    VBox.setVgrow(parametersSection, Priority.SOMETIMES);

    LOG.debug("Conditions section UI built.");
    return sectionPane;
  }

  /**
   * Creates the HBox containing the "Add Group" and "Remove Group" buttons.
   *
   * @return The HBox Node with group management buttons.
   */
  private HBox createGroupButtonRow() {
    Button addGroupButton = createButton("key.addGroupButton", e -> handleAddGroupAction());
    addGroupButton.setId(getLocalProp("id.addGroupButton"));

    Button removeGroupButton = createButton("key.removeGroupButton", e -> handleRemoveGroupAction());
    removeGroupButton.setId(getLocalProp("id.removeGroupButton"));
    removeGroupButton.getStyleClass().add(getLocalProp("style.removeButton"));

    HBox buttonBox = new HBox();
    buttonBox.setId(getLocalProp("id.groupButtonRow"));
    buttonBox.getStyleClass().add(getLocalProp("style.buttonBox"));
    buttonBox.setAlignment(Pos.CENTER_LEFT);
    buttonBox.getChildren().addAll(addGroupButton, removeGroupButton);
    return buttonBox;
  }

  /**
   * Creates the HBox containing the target group ComboBox, condition type ComboBox,
   * and the "Add Condition" / "Remove Condition" buttons.
   *
   * @return The HBox Node for condition selection and management.
   */
  private HBox createConditionSelectionRow() {
    HBox selectionBox = new HBox();
    selectionBox.setId(getLocalProp("id.conditionSelectionRow"));
    selectionBox.getStyleClass().add(getLocalProp("style.selectionBox"));
    selectionBox.setAlignment(Pos.CENTER_LEFT);

    Label groupLabel = new Label(uiBundle.getString(getLocalProp("key.conditionGroupLabel")));
    setupConditionGroupComboBox();
    setupConditionTypeComboBox();

    Button addConditionButton = createButton("key.addConditionButton", e -> handleAddConditionAction());
    addConditionButton.setId(getLocalProp("id.addConditionButton"));
    addConditionButton.setMaxWidth(Double.MAX_VALUE);

    Button removeConditionButton = createButton("key.removeConditionButton", e -> handleRemoveConditionAction());
    removeConditionButton.setId(getLocalProp("id.removeConditionButton"));
    removeConditionButton.getStyleClass().add(getLocalProp("style.removeButton"));

    selectionBox.getChildren().addAll(groupLabel, conditionGroupComboBox, conditionTypeComboBox, addConditionButton, removeConditionButton);
    HBox.setHgrow(conditionTypeComboBox, Priority.ALWAYS);

    return selectionBox;
  }

  /**
   * Sets up the ComboBox for selecting the target condition group.
   */
  private void setupConditionGroupComboBox() {
    conditionGroupComboBox = new ComboBox<>();
    conditionGroupComboBox.setId(getLocalProp("id.conditionGroupComboBox"));
    conditionGroupComboBox.setPromptText(uiBundle.getString(getLocalProp("key.promptSelectGroup")));
    conditionGroupComboBox.setMinWidth(Region.USE_PREF_SIZE);
  }

  /**
   * Sets up the ComboBox for selecting condition types.
   */
  private void setupConditionTypeComboBox() {
    conditionTypeComboBox = new ComboBox<>(
        FXCollections.observableArrayList(conditionTypeSupplier.get()));
    conditionTypeComboBox.setId(getLocalProp("id.conditionTypeComboBox"));
    conditionTypeComboBox.setPromptText(uiBundle.getString(getLocalProp("key.promptSelectCondition")));
    conditionTypeComboBox.setMaxWidth(Double.MAX_VALUE);
  }


  /**
   * Sets up the ListView for displaying conditions.
   */
  private void setupConditionsListView() {
    conditionsListView = new ListView<>();
    conditionsListView.setId(getLocalProp("id.conditionsListView"));
    conditionsListView.getStyleClass().add(getLocalProp("style.dataListView"));

    conditionsListView.getSelectionModel().selectedItemProperty()
        .addListener((obs, oldVal, newVal) -> updateParametersPane(newVal));
  }

  /**
   * Builds the parameters section container.
   *
   * @return The Node representing the parameters section container.
   */
  private Node buildParametersSection() {
    VBox container = new VBox();
    container.setId(getLocalProp("id.parametersContainer"));

    Label header = createHeaderLabel("key.parametersHeader");

    parametersPane = new VBox();
    parametersPane.setId(getLocalProp("id.parametersPane"));

    ScrollPane scrollPane = new ScrollPane(parametersPane);
    scrollPane.setId(getLocalProp("id.parametersScrollPane"));
    scrollPane.setFitToWidth(true);

    container.getChildren().addAll(header, scrollPane);
    return container;
  }

  /**
   * Handles the action triggered by the "Add Group" button.
   */
  private void handleAddGroupAction() {
    addGroupHandler.run();
  }

  /**
   * Handles the action triggered by the "Remove Group" button.
   */
  private void handleRemoveGroupAction() {
    ConditionDisplayItem selected = conditionsListView.getSelectionModel().getSelectedItem();
    if (selected != null) {
      removeGroupHandler.accept(selected.getGroupIndex());
    } else {
      LOG.warn("No condition selected, cannot determine group to remove.");
    }
  }

  /**
   * Handles the action triggered by the "Add Condition" button.
   * Reads the target group from the condition group combo box.
   */
  private void handleAddConditionAction() {
    String selectedType = conditionTypeComboBox.getSelectionModel().getSelectedItem();
    Integer targetGroupIndex = conditionGroupComboBox.getValue();

    if (selectedType == null || selectedType.trim().isEmpty()) {
      LOG.warn("No condition type selected.");
      return;
    }
    if (targetGroupIndex == null) {
      LOG.warn("No target condition group selected. Defaulting to group 0 if available, otherwise skipping add.");
      if (conditionGroupComboBox.getItems().isEmpty()) {
        LOG.error("Cannot add condition: No groups exist.");
        return;
      }
      targetGroupIndex = 0; // Default to the first group if available
    }


    addConditionHandler.handle(targetGroupIndex, selectedType.trim());
    conditionTypeComboBox.getSelectionModel().clearSelection();
    conditionTypeComboBox.setPromptText(uiBundle.getString(getLocalProp("key.promptSelectCondition")));
    conditionGroupComboBox.getSelectionModel().clearSelection(); // Optionally clear group selection too
    conditionGroupComboBox.setPromptText(uiBundle.getString(getLocalProp("key.promptSelectGroup")));
  }

  /**
   * Handles the action triggered by the "Remove Condition" button.
   */
  private void handleRemoveConditionAction() {
    ConditionDisplayItem selected = conditionsListView.getSelectionModel().getSelectedItem();
    if (selected != null) {
      removeConditionHandler.handle(selected.getGroupIndex(), selected.getConditionIndex());
    } else {
      LOG.warn("No condition selected for removal.");
    }
  }

  /**
   * Updates the parameters pane based on the currently selected condition item.
   * Rebuilds the parameter editing controls dynamically based on local properties file definitions.
   *
   * @param selectedItem The currently selected ConditionDisplayItem, or null if none selected.
   */
  private void updateParametersPane(ConditionDisplayItem selectedItem) {
    parametersPane.getChildren().clear();
    if (selectedItem == null || selectedItem.getData() == null) {
      return;
    }

    ExecutorData data = selectedItem.getData();
    String executorName = data.getExecutorName();
    if (executorName == null) {
      LOG.warn("Selected condition has null executor name. Cannot display parameters.");
      return;
    }

    GridPane grid = createParametersGrid();
    String paramCountKey = executorName + getLocalProp("param.baseKey") + getLocalProp("param.countSuffix");
    int paramCount = Integer.parseInt(localProps.getProperty(paramCountKey));

    /**
     * if (paramCountStr == null) {
     *       LOG.warn("Parameter count definition missing for executor '{}' using key '{}' in {}",
     *           executorName, paramCountKey, IDENTIFIERS_PROPERTIES_PATH);
     *       String errorMsg = String.format(uiBundle.getString(getLocalProp("key.warnMissingParameter")), executorName);
     *       Label errorLabel = new Label(errorMsg);
     *       parametersPane.getChildren().add(errorLabel);
     *       return;
     *     }
     */


    try {

      for (int i = 1; i <= paramCount; i++) {
        String paramBaseKeyStr = executorName + getLocalProp("param.baseKey") + "." + i;
        String name = localProps.getProperty(paramBaseKeyStr + getLocalProp("param.nameSuffix"));
        String type = localProps.getProperty(paramBaseKeyStr + getLocalProp("param.typeSuffix"));
        String description = localProps.getProperty(paramBaseKeyStr + getLocalProp("param.descSuffix"), "");
        String defaultValue = localProps.getProperty(paramBaseKeyStr + getLocalProp("param.defaultSuffix"), "");

        if (name == null || type == null) {
          LOG.error("Missing name or type definition for parameter index {} of executor {}", i, executorName);
          String errorMsg = String.format(uiBundle.getString(getLocalProp("key.errorParameterDefinition")), i, executorName);
          Label errorLabel = new Label(errorMsg);
          grid.add(errorLabel, 0, i-1, 2, 1);
          continue;
        }

        addParameterRow(grid, i - 1, name, type, description, defaultValue, selectedItem);
      }
      parametersPane.getChildren().add(grid);
      LOG.trace("Parameters pane updated for condition: {}", executorName);

    } catch (NumberFormatException e) {
      LOG.error("Invalid number format for parameter count for key '{}' in {}: {}", paramCountKey, IDENTIFIERS_PROPERTIES_PATH, paramCount, e);
      String errorMsg = String.format(uiBundle.getString(getLocalProp("key.warnInvalidNumber")), executorName);
      Label errorLabel = new Label(errorMsg);
      parametersPane.getChildren().add(errorLabel);
    } catch (MissingResourceException e) {
      LOG.error("Missing UI text resource for parameter UI construction.", e);
      Label errorLabel = new Label("UI Text Error");
      parametersPane.getChildren().add(errorLabel);
    } catch (Exception e) {
      LOG.error("Unexpected error building parameters UI for executor '{}'", executorName, e);
      String errorMsg = String.format(uiBundle.getString(getLocalProp("key.errorParameterLoad")), executorName);
      Label errorLabel = new Label(errorMsg);
      parametersPane.getChildren().add(errorLabel);
    }
  }

  /**
   * Creates and configures a GridPane used for laying out parameter labels and fields.
   *
   * @return A new GridPane instance.
   */
  private GridPane createParametersGrid() {
    GridPane grid = new GridPane();
    grid.getStyleClass().add(getLocalProp("style.parameterGrid"));
    return grid;
  }


  /**
   * Adds a single row to the parameters grid based on parameter definition from properties.
   *
   * @param grid         The GridPane to add the row to.
   * @param rowIndex     The row index for the new element.
   * @param paramName    The display name of the parameter.
   * @param paramType    The type string (e.g., "String", "Double", "Boolean", "Dropdown:A,B,C") loaded from properties.
   * @param description  Tooltip description for the parameter.
   * @param defaultValue Default value string from properties.
   * @param item         The ConditionDisplayItem being edited.
   */
  private void addParameterRow(GridPane grid,
      int rowIndex,
      String paramName,
      String paramType,
      String description,
      String defaultValue,
      ConditionDisplayItem item) {
    LOG.debug("addParameterRow: row={}, name='{}', type='{}', default='{}'",
        rowIndex, paramName, paramType, defaultValue);

    // 1) figure out the "human‐readable" type key
    String typeDisplayKey;
    Control inputControl;
    String currentValue = getCurrentValueAsString(item.getData(), paramName, paramType, defaultValue);
    LOG.debug("  currentValue = '{}'", currentValue);

    String typeIdBool     = getLocalProp("param.typeIdBoolean");
    String typeIdInt      = getLocalProp("param.typeIdInteger");
    String typeIdDouble   = getLocalProp("param.typeIdDouble");
    String typeIdDropdown = getLocalProp("param.typeIdDropdownPrefix");
    String typeIdString   = getLocalProp("param.typeIdString");

    try {
      if (paramType.equalsIgnoreCase(typeIdBool)) {
        typeDisplayKey = getLocalProp("key.paramTypeBoolean");
        LOG.debug("  branch=Boolean, key={}", typeDisplayKey);
        CheckBox cb = new CheckBox();
        cb.setSelected(Boolean.parseBoolean(currentValue));
        cb.setOnAction(e -> handleBooleanParamUpdate(item, paramName, cb.isSelected()));
        inputControl = cb;

      } else if (paramType.equalsIgnoreCase(typeIdInt)
          || paramType.equalsIgnoreCase(typeIdDouble)) {
        typeDisplayKey = paramType.equalsIgnoreCase(typeIdInt)
            ? getLocalProp("key.paramTypeInteger")
            : getLocalProp("key.paramTypeDouble");
        LOG.debug("  branch=Numeric, key={}", typeDisplayKey);
        TextField tf = new TextField(currentValue);
        tf.setOnAction(e -> handleNumericParamUpdate(item, paramName, paramType, tf));
        tf.focusedProperty().addListener((obs, ov, nv) -> {
          if (!nv) handleNumericParamUpdate(item, paramName, paramType, tf);
        });
        inputControl = tf;

      } else if (paramType.toUpperCase().startsWith(typeIdDropdown.toUpperCase())) {
        typeDisplayKey = getLocalProp("key.paramTypeDropdown");
        LOG.debug("  branch=Dropdown, key={}", typeDisplayKey);
        String[] opts = paramType.substring(typeIdDropdown.length()).split(",");
        ComboBox<String> cb = new ComboBox<>(FXCollections.observableArrayList(opts));
        cb.setValue(currentValue);
        cb.setOnAction(e -> handleStringParamUpdate(item, paramName, cb.getValue()));
        inputControl = cb;

      } else {
        typeDisplayKey = getLocalProp("key.paramTypeString");
        LOG.debug("  branch=String, key={}", typeDisplayKey);
        TextField tf = new TextField(currentValue);
        tf.setOnAction(e -> handleStringParamUpdate(item, paramName, tf.getText()));
        tf.focusedProperty().addListener((obs, ov, nv) -> {
          if (!nv) handleStringParamUpdate(item, paramName, tf.getText());
        });
        inputControl = tf;
      }

    } catch (Exception e) {
      LOG.error("Error choosing control for param '{}': {}", paramName, e.getMessage());
      // fallback to a simple label so the row still appears
      typeDisplayKey = "key.paramTypeError";
      inputControl  = new Label(currentValue);
    }

    // 2) resolve the human‐readable type from uiBundle, but safely
    String humanType;
    try {
      humanType = uiBundle.getString(typeDisplayKey);
    } catch (MissingResourceException mre) {
      LOG.warn("No UI text for key '{}', falling back to raw paramType", typeDisplayKey);
      humanType = paramType;
    }

    // 3) build the label + tooltip
    Label nameLabel = new Label(String.format("%s (%s):", paramName, humanType));
    if (description != null && !description.isEmpty()) {
      Tooltip tip = new Tooltip(description);
      nameLabel.setTooltip(tip);
      inputControl.setTooltip(tip);
    }

    // 4) put it on the grid
    grid.add(nameLabel, 0, rowIndex);
    grid.add(inputControl, 1, rowIndex);
    GridPane.setHgrow(inputControl, Priority.ALWAYS);

    LOG.debug("  added row {}, label='{}', control={}", rowIndex, nameLabel.getText(), inputControl.getClass().getSimpleName());
  }


  /**
   * Retrieves the current value of a parameter from ExecutorData, falling back to defaultValue.
   *
   * @param data The ExecutorData containing parameters.
   * @param paramName The name of the parameter.
   * @param paramType The expected type from properties.
   * @param defaultValue The default value if not found.
   * @return The current value as a String.
   */
  private String getCurrentValueAsString(ExecutorData data, String paramName, String paramType, String defaultValue) {
    Map<String, String> stringParams = data.getStringParams();
    Map<String, Double> doubleParams = data.getDoubleParams();
    String typeIdDouble = getLocalProp("param.typeIdDouble");
    String typeIdInteger = getLocalProp("param.typeIdInteger");


    if (stringParams != null && stringParams.containsKey(paramName)) {
      return stringParams.get(paramName);
    }
    if ((paramType.equalsIgnoreCase(typeIdDouble) || paramType.equalsIgnoreCase(typeIdInteger)) && doubleParams != null && doubleParams.containsKey(paramName)) {
      Double val = doubleParams.get(paramName);
      if (paramType.equalsIgnoreCase(typeIdInteger) && val != null && val == Math.floor(val) && !val.isInfinite()) {
        return String.valueOf(val.intValue());
      }
      return String.valueOf(val);
    }

    return defaultValue;
  }

  /**
   * Handles the update of a String parameter value (or Dropdown selection).
   *
   * @param item      The ConditionDisplayItem being edited.
   * @param key       The parameter key (name).
   * @param newValue  The new string value.
   */
  private void handleStringParamUpdate(ConditionDisplayItem item, String key, String newValue) {
    editConditionParamHandler.handle(item.getGroupIndex(), item.getConditionIndex(), key, newValue);
    LOG.trace("String parameter '{}' updated to: {}", key, newValue);
  }

  /**
   * Handles the update of a Boolean parameter value.
   *
   * @param item      The ConditionDisplayItem being edited.
   * @param key       The parameter key (name).
   * @param newValue  The new boolean value.
   */
  private void handleBooleanParamUpdate(ConditionDisplayItem item, String key, boolean newValue) {
    editConditionParamHandler.handle(item.getGroupIndex(), item.getConditionIndex(), key, String.valueOf(newValue));
    LOG.trace("Boolean parameter '{}' updated to: {}", key, newValue);
  }

  /**
   * Handles the update of a numeric (Double/Integer) parameter value.
   *
   * @param item      The ConditionDisplayItem being edited.
   * @param key       The parameter key (name).
   * @param paramType The expected numeric type ("Integer" or "Double") from properties.
   * @param textField The TextField containing the new numeric value.
   */
  private void handleNumericParamUpdate(ConditionDisplayItem item, String key, String paramType, TextField textField) {
    String newValueText = textField.getText();
    String typeIdInteger = getLocalProp("param.typeIdInteger");
    try {
      if (paramType.equalsIgnoreCase(typeIdInteger)) {
        Integer intVal = Integer.parseInt(newValueText);
        editConditionParamHandler.handle(item.getGroupIndex(), item.getConditionIndex(), key, intVal.doubleValue());
        LOG.trace("Integer parameter '{}' updated to: {}", key, intVal);
      } else {
        Double doubleVal = Double.parseDouble(newValueText);
        editConditionParamHandler.handle(item.getGroupIndex(), item.getConditionIndex(), key, doubleVal);
        LOG.trace("Double parameter '{}' updated to: {}", key, doubleVal);
      }
      textField.setStyle("");
      textField.setTooltip(null);
    } catch (NumberFormatException e) {
      LOG.warn("Invalid {} format for param '{}': {}", paramType, key, newValueText);
      textField.setStyle("-fx-text-fill: red; -fx-border-color: red;");
      String errorTooltip = String.format(uiBundle.getString(getLocalProp("key.warnInvalidNumericInput")), paramType, newValueText);
      textField.setTooltip(new Tooltip(errorTooltip));
    }
  }


  /**
   * Updates the conditions ListView and the condition group ComboBox
   * with the provided list of condition groups.
   *
   * @param conditionGroups A list where each inner list represents a group of ExecutorData (conditions).
   */
  public void updateConditionsListView(List<List<ExecutorData>> conditionGroups) {
    ObservableList<ConditionDisplayItem> displayItems = FXCollections.observableArrayList();
    int numGroups = 0;
    if (conditionGroups != null) {
      processConditionGroups(conditionGroups, displayItems);
      numGroups = conditionGroups.size();
    }
    conditionsListView.setItems(displayItems);
    updateConditionGroupComboBox(numGroups);
    updateParametersPane(null); // Clear parameters when list changes
    LOG.trace("Conditions list view updated with {} items across {} groups.", displayItems.size(), numGroups);
  }

  /**
   * Updates the items available in the condition group ComboBox based on the current number of groups.
   *
   * @param numGroups The current number of condition groups.
   */
  private void updateConditionGroupComboBox(int numGroups) {
    if (conditionGroupComboBox == null) {
      LOG.warn("Condition group combo box is null, cannot update.");
      return;
    }

    Integer selectedGroup = conditionGroupComboBox.getValue();
    List<Integer> groupIndices = IntStream.range(0, numGroups)
        .boxed()
        .collect(Collectors.toList());
    conditionGroupComboBox.setItems(FXCollections.observableArrayList(groupIndices));

    if (selectedGroup != null && selectedGroup < numGroups) {
      conditionGroupComboBox.setValue(selectedGroup); // Preserve selection if still valid
    } else {
      conditionGroupComboBox.getSelectionModel().clearSelection(); // Clear selection if invalid or no groups
    }

    conditionGroupComboBox.setDisable(numGroups == 0); // Disable if there are no groups
    conditionGroupComboBox.setPromptText(uiBundle.getString(getLocalProp("key.promptSelectGroup")));
  }


  /**
   * Iterates through the outer list of condition groups and processes each group.
   *
   * @param conditionGroups The list of condition groups.
   * @param displayItems    The ObservableList to add ConditionDisplayItems to.
   */
  private void processConditionGroups(List<List<ExecutorData>> conditionGroups, ObservableList<ConditionDisplayItem> displayItems) {
    for (int groupIndex = 0; groupIndex < conditionGroups.size(); groupIndex++) {
      List<ExecutorData> group = conditionGroups.get(groupIndex);
      processSingleGroup(group, groupIndex, displayItems);
    }
  }

  /**
   * Processes a single group of conditions (ExecutorData).
   *
   * @param group        The list of ExecutorData representing a single condition group.
   * @param groupIndex   The index of this group.
   * @param displayItems The ObservableList to add ConditionDisplayItems to.
   */
  private void processSingleGroup(List<ExecutorData> group, int groupIndex, ObservableList<ConditionDisplayItem> displayItems) {
    if (group == null) {
      LOG.warn("Encountered null condition group at index {}", groupIndex);
      return;
    }
    for (int conditionIndex = 0; conditionIndex < group.size(); conditionIndex++) {
      ExecutorData conditionData = group.get(conditionIndex);
      if (conditionData != null) {
        displayItems.add(new ConditionDisplayItem(groupIndex, conditionIndex, conditionData));
      } else {
        LOG.warn("Found null condition data at group {}, index {}", groupIndex, conditionIndex);
      }
    }
  }

  /**
   * Creates a Label configured as a section header.
   *
   * @param localPropKey The key in the local properties file for the header text's uiBundle key.
   * @return A configured Label Node.
   */
  private Label createHeaderLabel(String localPropKey) {
    String uiBundleKey = getLocalProp(localPropKey);
    Label label = new Label(uiBundle.getString(uiBundleKey));
    label.getStyleClass().add(getLocalProp("style.sectionHeader"));
    label.setMaxWidth(Double.MAX_VALUE);
    return label;
  }

  /**
   * Creates a Button with text retrieved from the uiBundle and sets its action handler.
   *
   * @param localPropKey The key in the local properties file for the button text's uiBundle key.
   * @param handler   The EventHandler for the button's action.
   * @return A configured Button Node.
   */
  private Button createButton(String localPropKey, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
    String uiBundleKey = getLocalProp(localPropKey);
    Button button = new Button(uiBundle.getString(uiBundleKey));
    button.setOnAction(handler);
    button.getStyleClass().add(getLocalProp("style.actionButton"));
    return button;
  }
}