package oogasalad.editor.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
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
import javafx.scene.layout.VBox;
import oogasalad.editor.model.data.object.event.ExecutorData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Builds the UI section for managing Conditions associated with an Event. Handles condition
 * groups, adding/removing conditions by type (String) and index, and provides UI for parameter
 * editing. Uses separate functional interfaces for handlers and a separate class for display
 * items.
 *
 * @author Tatum McKinnis
 */
public class ConditionsSectionBuilder {

  private static final Logger LOG = LogManager.getLogger(ConditionsSectionBuilder.class);

  private static final String IDENTIFIERS_PROPERTIES_PATH = "/oogasalad/editor/view/resources/conditions_identifiers.properties";

  private final ResourceBundle uiBundle;
  private final Properties identifierProps;
  private final Supplier<List<String>> conditionTypeSupplier;
  private final Runnable addGroupHandler;
  private final IntConsumer removeGroupHandler;
  private final AddConditionHandler addConditionHandler;
  private final RemoveConditionHandler removeConditionHandler;
  private final EditConditionParamHandler editConditionParamHandler;

  private ComboBox<String> conditionTypeComboBox;
  private ListView<ConditionDisplayItem> conditionsListView;
  private VBox parametersPane;


  /**
   * Constructs a ConditionsSectionBuilder.
   * Initializes required handlers, suppliers, and resource bundles.
   * Loads identifiers (keys, CSS IDs/classes) from an external properties file.
   *
   * @param uiBundle                 The resource bundle for UI text.
   * @param conditionTypeSupplier    Supplier for available condition types.
   * @param addGroupHandler          Handler for adding a new condition group.
   * @param removeGroupHandler       Handler for removing a condition group by index.
   * @param addConditionHandler      Handler for adding a condition to a group.
   * @param removeConditionHandler   Handler for removing a condition from a group.
   * @param editConditionParamHandler Handler for editing a condition's parameter.
   * @throws RuntimeException if the identifiers properties file cannot be found or loaded.
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

    this.identifierProps = loadIdentifierProperties();
  }

  /**
   * Loads the identifier strings (keys, CSS classes, IDs) from the properties file specified by {@link #IDENTIFIERS_PROPERTIES_PATH}.
   *
   * @return A Properties object containing the loaded identifiers.
   * @throws RuntimeException If the properties file cannot be found or read.
   */
  private Properties loadIdentifierProperties() {
    Properties props = new Properties();
    try (InputStream input = ConditionsSectionBuilder.class.getResourceAsStream(IDENTIFIERS_PROPERTIES_PATH)) {
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
   * Builds the entire UI Node for the conditions management section.
   * This includes headers, buttons for group management, controls for condition selection/management,
   * a list view for conditions, and a pane for editing parameters of the selected condition.
   *
   * @return The root Node of the conditions section UI.
   */
  public Node build() {
    VBox sectionPane = new VBox();
    sectionPane.setId(getId("id.sectionPane"));
    sectionPane.getStyleClass().add(getId("style.inputSubSection"));

    Label header = createHeaderLabel(getId("key.conditionsHeader"));
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
    Button addGroupButton = createButton(getId("key.addGroupButton"), e -> handleAddGroupAction());
    addGroupButton.setId(getId("id.addGroupButton"));

    Button removeGroupButton = createButton(getId("key.removeGroupButton"), e -> handleRemoveGroupAction());
    removeGroupButton.setId(getId("id.removeGroupButton"));
    removeGroupButton.getStyleClass().add(getId("style.removeButton"));

    HBox buttonBox = new HBox();
    buttonBox.setId(getId("id.groupButtonRow"));
    buttonBox.getStyleClass().add(getId("style.buttonBox"));
    buttonBox.setAlignment(Pos.CENTER_LEFT);
    buttonBox.getChildren().addAll(addGroupButton, removeGroupButton);
    return buttonBox;
  }

  /**
   * Creates the HBox containing the condition type ComboBox and the "Add Condition" / "Remove Condition" buttons.
   *
   * @return The HBox Node for condition selection and management.
   */
  private HBox createConditionSelectionRow() {
    HBox selectionBox = new HBox();
    selectionBox.setId(getId("id.conditionSelectionRow"));
    selectionBox.getStyleClass().add(getId("style.selectionBox"));
    selectionBox.setAlignment(Pos.CENTER_LEFT);

    setupConditionTypeComboBox();

    Button addConditionButton = createButton(getId("key.addConditionButton"), e -> handleAddConditionAction());
    addConditionButton.setId(getId("id.addConditionButton"));
    addConditionButton.setMaxWidth(Double.MAX_VALUE);

    Button removeConditionButton = createButton(getId("key.removeConditionButton"), e -> handleRemoveConditionAction());
    removeConditionButton.setId(getId("id.removeConditionButton"));
    removeConditionButton.getStyleClass().add(getId("style.removeButton"));

    selectionBox.getChildren().addAll(conditionTypeComboBox, addConditionButton, removeConditionButton);
    HBox.setHgrow(conditionTypeComboBox, Priority.ALWAYS);

    return selectionBox;
  }

  /**
   * Sets up the ComboBox for selecting condition types.
   * Populates it with types from the supplier and sets the prompt text.
   */
  private void setupConditionTypeComboBox() {
    conditionTypeComboBox = new ComboBox<>(
        FXCollections.observableArrayList(conditionTypeSupplier.get()));
    conditionTypeComboBox.setId(getId("id.conditionTypeComboBox"));
    conditionTypeComboBox.setPromptText(uiBundle.getString(getId("key.promptSelectCondition")));
    conditionTypeComboBox.setMaxWidth(Double.MAX_VALUE);
  }


  /**
   * Sets up the ListView for displaying conditions.
   * Adds a listener to update the parameters pane when the selection changes.
   */
  private void setupConditionsListView() {
    conditionsListView = new ListView<>();
    conditionsListView.setId(getId("id.conditionsListView"));
    conditionsListView.getStyleClass().add(getId("style.dataListView"));

    conditionsListView.getSelectionModel().selectedItemProperty()
        .addListener((obs, oldVal, newVal) -> updateParametersPane(newVal));
  }

  /**
   * Builds the parameters section, including a header and a scrollable pane
   * where parameter editing controls will be displayed.
   *
   * @return The Node representing the parameters section container.
   */
  private Node buildParametersSection() {
    VBox container = new VBox();
    container.setId(getId("id.parametersContainer"));

    Label header = createHeaderLabel(getId("key.parametersHeader"));

    parametersPane = new VBox();
    parametersPane.setId(getId("id.parametersPane"));

    ScrollPane scrollPane = new ScrollPane(parametersPane);
    scrollPane.setId(getId("id.parametersScrollPane"));
    scrollPane.setFitToWidth(true);

    container.getChildren().addAll(header, scrollPane);
    return container;
  }

  /**
   * Handles the action triggered by the "Add Group" button.
   * Delegates the action to the addGroupHandler.
   */
  private void handleAddGroupAction() {
    addGroupHandler.run();
  }

  /**
   * Handles the action triggered by the "Remove Group" button.
   * Determines the group index from the selected item in the list view
   * and delegates the removal action to the removeGroupHandler.
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
   * Gets the selected condition type and the target group index (based on list selection or default)
   * and delegates the addition action to the addConditionHandler.
   */
  private void handleAddConditionAction() {
    String selectedType = conditionTypeComboBox.getSelectionModel().getSelectedItem();
    if (selectedType == null || selectedType.trim().isEmpty()) {
      LOG.warn("No condition type selected.");
      return;
    }

    ConditionDisplayItem selectedItem = conditionsListView.getSelectionModel().getSelectedItem();
    int targetGroupIndex = (selectedItem != null) ? selectedItem.getGroupIndex() : 0;

    addConditionHandler.handle(targetGroupIndex, selectedType.trim());
    conditionTypeComboBox.getSelectionModel().clearSelection();
    conditionTypeComboBox.setPromptText(uiBundle.getString(getId("key.promptSelectCondition")));
  }

  /**
   * Handles the action triggered by the "Remove Condition" button.
   * Gets the selected condition from the list view and delegates the removal
   * action (with group and condition index) to the removeConditionHandler.
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
   * Clears the existing parameters and rebuilds the parameter editing controls
   * (labels and text fields) within a GridPane.
   *
   * @param selectedItem The currently selected ConditionDisplayItem, or null if none selected.
   */
  private void updateParametersPane(ConditionDisplayItem selectedItem) {
    parametersPane.getChildren().clear();
    if (selectedItem == null || selectedItem.getData() == null) {
      return;
    }

    ExecutorData data = selectedItem.getData();
    GridPane grid = createParametersGrid();

    addStringParametersToGrid(grid, data.getStringParams(), selectedItem);
    addDoubleParametersToGrid(grid, data.getDoubleParams(), selectedItem);

    parametersPane.getChildren().add(grid);
    LOG.trace("Parameters pane updated for item Group {}, Index {}", selectedItem.getGroupIndex(), selectedItem.getConditionIndex());
  }

  /**
   * Creates and configures a GridPane used for laying out parameter labels and fields.
   *
   * @return A new GridPane instance.
   */
  private GridPane createParametersGrid() {
    GridPane grid = new GridPane();
    grid.getStyleClass().add(getId("style.parameterGrid"));
    return grid;
  }

  /**
   * Adds rows to the parameters grid for each String parameter found in the condition data.
   *
   * @param grid         The GridPane to add rows to.
   * @param params       A map of String parameter names to their values.
   * @param selectedItem The ConditionDisplayItem whose parameters are being displayed.
   */
  private void addStringParametersToGrid(GridPane grid, Map<String, String> params, ConditionDisplayItem selectedItem) {
    if (params == null) { return; }
    int rowIndex = grid.getRowCount();
    String typeSuffix = uiBundle.getString(getId("key.paramTypeString"));
    for (Map.Entry<String, String> entry : params.entrySet()) {
      addParameterRow(grid, rowIndex++, entry.getKey(), typeSuffix, entry.getValue(),
          (key, valueText) -> handleStringParamUpdate(selectedItem, key, valueText));
    }
  }

  /**
   * Adds rows to the parameters grid for each Double parameter found in the condition data.
   *
   * @param grid         The GridPane to add rows to.
   * @param params       A map of Double parameter names to their values.
   * @param selectedItem The ConditionDisplayItem whose parameters are being displayed.
   */
  private void addDoubleParametersToGrid(GridPane grid, Map<String, Double> params, ConditionDisplayItem selectedItem) {
    if (params == null) { return; }
    int rowIndex = grid.getRowCount();
    String typeSuffix = uiBundle.getString(getId("key.paramTypeDouble"));
    for (Map.Entry<String, Double> entry : params.entrySet()) {
      String originalValueStr = String.valueOf(entry.getValue());
      addParameterRow(grid, rowIndex++, entry.getKey(), typeSuffix,
          originalValueStr,
          (key, valueText) -> handleDoubleParamUpdate(selectedItem, key, valueText,
              originalValueStr));
    }
  }

  /**
   * Functional interface for handling parameter updates initiated from the UI row.
   */
  @FunctionalInterface
  interface ParameterUpdateHandler {
    /**
     * Handles the update event.
     * @param paramName The name of the parameter being updated.
     * @param newValueText The new value entered by the user as text.
     */
    void update(String paramName, String newValueText);
  }

  /**
   * Adds a single row to the parameters grid, consisting of a label (parameter name + type)
   * and a TextField for editing the value. Sets up an action handler on the TextField.
   *
   * @param grid          The GridPane to add the row to.
   * @param rowIndex      The row index where the new row should be added.
   * @param paramName     The name of the parameter.
   * @param typeSuffix    The type suffix (e.g., "(String)", "(Double)") obtained from resources.
   * @param initialValue  The initial value to display in the TextField.
   * @param updateHandler The handler to call when the TextField's action is triggered (e.g., Enter pressed).
   */
  private void addParameterRow(GridPane grid, int rowIndex, String paramName, String typeSuffix, String initialValue, ParameterUpdateHandler updateHandler) {
    Label nameLabel = new Label(String.format("%s %s:", paramName, typeSuffix));
    TextField valueField = new TextField(initialValue);
    valueField.setOnAction(event -> {
      String newValueText = valueField.getText();
      updateHandler.update(paramName, newValueText);
    });
    grid.add(nameLabel, 0, rowIndex);
    grid.add(valueField, 1, rowIndex);
    GridPane.setHgrow(valueField, Priority.ALWAYS);
  }

  /**
   * Handles the update of a String parameter value. Called when the corresponding TextField action occurs.
   * Delegates the update to the {@code editConditionParamHandler}.
   *
   * @param item     The ConditionDisplayItem being edited.
   * @param key      The name (key) of the String parameter.
   * @param newValue The new String value entered by the user.
   */
  private void handleStringParamUpdate(ConditionDisplayItem item, String key, String newValue) {
    editConditionParamHandler.handle(item.getGroupIndex(), item.getConditionIndex(), key,
        newValue);
    LOG.trace("String parameter '{}' updated via ActionEvent to: {}", key, newValue);
  }

  /**
   * Handles the update of a Double parameter value. Called when the corresponding TextField action occurs.
   * Attempts to parse the input text as a Double. If successful, delegates the update to the
   * {@code editConditionParamHandler}. Logs a warning if parsing fails.
   *
   * @param item              The ConditionDisplayItem being edited.
   * @param key               The name (key) of the Double parameter.
   * @param newValueText      The new value entered by the user as text.
   * @param originalValueText The original value as text (can be used for resetting on error, though not currently implemented).
   */
  private void handleDoubleParamUpdate(ConditionDisplayItem item, String key, String newValueText, String originalValueText) {
    try {
      Double doubleVal = Double.parseDouble(newValueText);
      editConditionParamHandler.handle(item.getGroupIndex(), item.getConditionIndex(), key,
          doubleVal);
      LOG.trace("Double parameter '{}' updated via ActionEvent to: {}", key, doubleVal);
    } catch (NumberFormatException e) {
      LOG.warn("Invalid double format for param '{}' on ActionEvent: {}.", key, newValueText);
    }
  }

  /**
   * Updates the conditions ListView with the provided list of condition groups.
   * Clears the current items, processes the new groups into ConditionDisplayItems,
   * and sets the new items on the ListView. Also clears the parameters pane.
   *
   * @param conditionGroups A list where each inner list represents a group of ExecutorData (conditions).
   */
  public void updateConditionsListView(List<List<ExecutorData>> conditionGroups) {
    ObservableList<ConditionDisplayItem> displayItems = FXCollections.observableArrayList();
    if (conditionGroups != null) {
      processConditionGroups(conditionGroups, displayItems);
    }
    conditionsListView.setItems(displayItems);
    updateParametersPane(null);
    LOG.trace("Conditions list view updated with {} items.", displayItems.size());
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
   * Processes a single group of conditions (ExecutorData). Creates a ConditionDisplayItem
   * for each non-null condition within the group and adds it to the display list.
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
   * Text is retrieved from the uiBundle using the provided key.
   *
   * @param bundleKey The key for the header text in the uiBundle.
   * @return A configured Label Node.
   */
  private Label createHeaderLabel(String bundleKey) {
    Label label = new Label(uiBundle.getString(bundleKey));
    label.getStyleClass().add(getId("style.sectionHeader"));
    label.setMaxWidth(Double.MAX_VALUE);
    return label;
  }

  /**
   * Creates a Button with text retrieved from the uiBundle and sets its action handler.
   *
   * @param bundleKey The key for the button text in the uiBundle.
   * @param handler   The EventHandler for the button's action.
   * @return A configured Button Node.
   */
  private Button createButton(String bundleKey, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
    Button button = new Button(uiBundle.getString(bundleKey));
    button.setOnAction(handler);
    button.getStyleClass().add(getId("style.actionButton"));
    return button;
  }
}