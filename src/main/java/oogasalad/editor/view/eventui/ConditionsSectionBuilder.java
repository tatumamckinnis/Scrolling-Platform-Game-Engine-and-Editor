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
  public void updateParametersPane(ConditionDisplayItem selectedItem) {
    parametersPane.getChildren().clear();
    if (selectedItem == null || selectedItem.getData() == null) {
      return;
    }
    try {
      GridPane grid = buildParametersGrid(selectedItem);
      parametersPane.getChildren().add(grid);
      LOG.trace("Parameters pane updated for condition: {}", selectedItem.getData().getExecutorName());
    } catch (NumberFormatException e) {
      LOG.error("Invalid number format reading parameter count", e);
      showErrorLabel(String.format(uiBundle.getString(getLocalProp("key.warnInvalidNumber")),
          selectedItem.getData().getExecutorName()));
    } catch (MissingResourceException e) {
      LOG.error("Missing UI text resource", e);
      showErrorLabel("UI Text Error");
    } catch (Exception e) {
      LOG.error("Unexpected error building parameters UI for executor '{}'",
          selectedItem.getData().getExecutorName(), e);
      showErrorLabel(String.format(uiBundle.getString(getLocalProp("key.errorParameterLoad")),
          selectedItem.getData().getExecutorName()));
    }
  }

  private GridPane buildParametersGrid(ConditionDisplayItem selectedItem) {
    ExecutorData data = selectedItem.getData();
    String exec = data.getExecutorName();
    int count = Integer.parseInt(localProps.getProperty(
        exec + getLocalProp("param.baseKey") + getLocalProp("param.countSuffix")
    ));
    GridPane grid = createParametersGrid();
    for (int i = 1; i <= count; i++) {
      String base = exec + getLocalProp("param.baseKey") + "." + i;
      String name = localProps.getProperty(base + getLocalProp("param.nameSuffix"));
      String type = localProps.getProperty(base + getLocalProp("param.typeSuffix"));
      String desc = localProps.getProperty(base + getLocalProp("param.descSuffix"), "");
      String def  = localProps.getProperty(base + getLocalProp("param.defaultSuffix"), "");
      if (name == null || type == null) {
        String msg = String.format(uiBundle.getString(
            getLocalProp("key.errorParameterDefinition")), i, exec);
        grid.add(new Label(msg), 0, i-1, 2, 1);
      } else {
        addParameterRow(grid, i-1, name, type, desc, def, selectedItem);
      }
    }
    return grid;
  }

  private void showErrorLabel(String message) {
    parametersPane.getChildren().add(new Label(message));
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
   * @param row     The row index for the new element.
   * @param name    The display name of the parameter.
   * @param type    The type string (e.g., "String", "Double", "Boolean", "Dropdown:A,B,C") loaded from properties.
   * @param desc  Tooltip description for the parameter.
   * @param def Default value string from properties.
   * @param item         The ConditionDisplayItem being edited.
   */
  private void addParameterRow(GridPane grid,
      int row,
      String name,
      String type,
      String desc,
      String def,
      ConditionDisplayItem item) {
    LOG.debug("addParameterRow: row={}, name='{}', type='{}', default='{}'",
        row, name, type, def);
    ExecutorData data = item.getData();
    String current = getCurrentValueAsString(data, name, type, def);
    Control control = createInputControl(item, name, type, current);
    String humanType = resolveHumanType(type);
    Label label = new Label(String.format("%s (%s):", name, humanType));
    if (!desc.isBlank()) {
      Tooltip tip = new Tooltip(desc);
      label.setTooltip(tip);
      control.setTooltip(tip);
    }
    grid.add(label,   0, row);
    grid.add(control, 1, row);
    GridPane.setHgrow(control, Priority.ALWAYS);
    LOG.debug("  added row {}, control={}", row, control.getClass().getSimpleName());
  }

  private Control createInputControl(ConditionDisplayItem item,
      String param,
      String typeId,
      String current) {
    String boolId     = getLocalProp("param.typeIdBoolean");
    String intId      = getLocalProp("param.typeIdInteger");
    String dblId      = getLocalProp("param.typeIdDouble");
    String ddPrefix   = getLocalProp("param.typeIdDropdownPrefix");
    try {
      if (typeId.equalsIgnoreCase(boolId)) {
        CheckBox cb = new CheckBox();
        cb.setSelected(Boolean.parseBoolean(current));
        cb.setOnAction(e -> handleBooleanParamUpdate(item, param, cb.isSelected()));
        return cb;
      }
      if (typeId.equalsIgnoreCase(intId) || typeId.equalsIgnoreCase(dblId)) {
        TextField tf = new TextField(current);
        tf.setOnAction(e -> handleNumericParamUpdate(item, param, typeId, tf));
        tf.focusedProperty().addListener((o, vOld, vNew) -> {
          if (!vNew) handleNumericParamUpdate(item, param, typeId, tf);
        });
        return tf;
      }
      if (typeId.toUpperCase().startsWith(ddPrefix.toUpperCase())) {
        String[] opts = typeId.substring(ddPrefix.length()).split(",");
        ComboBox<String> cb = new ComboBox<>(FXCollections.observableArrayList(opts));
        cb.setValue(current);
        cb.setOnAction(e -> handleStringParamUpdate(item, param, cb.getValue()));
        return cb;
      }
    } catch (Exception e) {
      LOG.error("Error choosing control for param '{}': {}", param, e.getMessage());
    }
    // fallback to text
    TextField tf = new TextField(current);
    tf.setOnAction(e -> handleStringParamUpdate(item, param, tf.getText()));
    tf.focusedProperty().addListener((o, vOld, vNew) -> {
      if (!vNew) handleStringParamUpdate(item, param, tf.getText());
    });
    return tf;
  }

  private String resolveHumanType(String typeId) {
    String key;
    String boolKey   = getLocalProp("param.typeIdBoolean");
    String intKey    = getLocalProp("param.typeIdInteger");
    String dblKey    = getLocalProp("param.typeIdDouble");
    String ddPrefix  = getLocalProp("param.typeIdDropdownPrefix");
    if (typeId.equalsIgnoreCase(boolKey)) {
      key = "key.paramTypeBoolean";
    } else if (typeId.equalsIgnoreCase(intKey)) {
      key = "key.paramTypeInteger";
    } else if (typeId.equalsIgnoreCase(dblKey)) {
      key = "key.paramTypeDouble";
    } else if (typeId.toUpperCase().startsWith(ddPrefix.toUpperCase())) {
      key = "key.paramTypeDropdown";
    } else {
      key = "key.paramTypeString";
    }
    try {
      return uiBundle.getString(key);
    } catch (MissingResourceException e) {
      LOG.warn("Missing UI text for '{}', falling back", key);
      return typeId;
    }
  }

  /**
   * Retrieves the current value of a parameter from ExecutorData, falling back to defaultValue.
   *
   * @param data The ExecutorData containing parameters.
   * @param name The name of the parameter.
   * @param typeId The expected type from properties.
   * @param defaultVal The default value if not found.
   * @return The current value as a String.
   */
  private String getCurrentValueAsString(ExecutorData data,
      String name,
      String typeId,
      String defaultVal) {
    Map<String,String> sp = data.getStringParams();
    Map<String,Double> dp = data.getDoubleParams();
    if (sp != null && sp.containsKey(name)) {
      return sp.get(name);
    }
    String intKey = getLocalProp("param.typeIdInteger");
    String dblKey = getLocalProp("param.typeIdDouble");
    if ((typeId.equalsIgnoreCase(intKey) || typeId.equalsIgnoreCase(dblKey))
        && dp != null && dp.containsKey(name)) {
      Double v = dp.get(name);
      if (typeId.equalsIgnoreCase(intKey)
          && v != null && v.doubleValue()==Math.floor(v)
          && !v.isInfinite()) {
        return String.valueOf(v.intValue());
      }
      return String.valueOf(v);
    }
    return defaultVal;
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
  /**
  Returns condition list view
   */
  public ListView<ConditionDisplayItem> getConditionsListView() {
    return conditionsListView;
  }
}