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
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import oogasalad.editor.model.data.object.DynamicVariable;
import oogasalad.editor.model.data.object.event.ExecutorData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Builds the UI section for managing game event "Outcomes" within the editor.
 * This component facilitates selecting outcome types, configuring their parameters
 * (including linking to dynamic variables), and managing the list of outcomes for a given event.
 * Configuration values (identifiers, keys, layout hints) are loaded from external properties.
 * Layout and styling details (padding, spacing, heights, colors) are controlled via CSS.
 *
 * @author Tatum McKinnis
 */
public class OutcomesSectionBuilder {

  private static final Logger LOG = LogManager.getLogger(OutcomesSectionBuilder.class);
  private static final String IDENTIFIERS_PROPERTIES_PATH = "/oogasalad/editor/view/resources/outcomes_section_builder_identifiers.properties";

  private final ResourceBundle uiBundle;
  private final Properties identifierProps;
  private final Supplier<List<String>> outcomeTypeSupplier;
  private final Supplier<List<DynamicVariable>> dynamicVariableSupplier;
  private final AddOutcomeHandler addOutcomeHandler;
  private final IntConsumer removeOutcomeHandler;
  private final Runnable createParameterHandler;
  private final EditOutcomeParamHandler editOutcomeParamHandler;

  private ComboBox<String> outcomeTypeComboBox;
  private ComboBox<String> dynamicVariableComboBox;
  private ListView<OutcomeDisplayItem> outcomesListView;
  private VBox parametersPane;

  /**
   * Constructs the OutcomesSectionBuilder with necessary data sources and handler callbacks.
   * Loads identifiers from an external properties file.
   *
   * @param uiBundle                The resource bundle for internationalized UI text.
   * @param outcomeTypeSupplier     Supplies available outcome types (e.g., "Move", "Attack").
   * @param dynamicVariableSupplier Supplies current dynamic variables available in the game context.
   * @param addOutcomeHandler       Handler for creating new outcomes.
   * @param removeOutcomeHandler    Handler for removing the selected outcome by index.
   * @param createParameterHandler  Handler for initiating the creation of a new dynamic variable.
   * @param editOutcomeParamHandler Handler for editing existing outcome parameters.
   * @throws NullPointerException if any required handler, supplier, or bundle is null.
   * @throws RuntimeException     if the identifiers properties file cannot be loaded.
   */
  public OutcomesSectionBuilder(ResourceBundle uiBundle,
      Supplier<List<String>> outcomeTypeSupplier,
      Supplier<List<DynamicVariable>> dynamicVariableSupplier,
      AddOutcomeHandler addOutcomeHandler,
      IntConsumer removeOutcomeHandler,
      Runnable createParameterHandler,
      EditOutcomeParamHandler editOutcomeParamHandler) {
    this.uiBundle = Objects.requireNonNull(uiBundle);
    this.outcomeTypeSupplier = Objects.requireNonNull(outcomeTypeSupplier);
    this.dynamicVariableSupplier = Objects.requireNonNull(dynamicVariableSupplier);
    this.addOutcomeHandler = Objects.requireNonNull(addOutcomeHandler);
    this.removeOutcomeHandler = Objects.requireNonNull(removeOutcomeHandler);
    this.createParameterHandler = Objects.requireNonNull(createParameterHandler);
    this.editOutcomeParamHandler = Objects.requireNonNull(editOutcomeParamHandler);
    this.identifierProps = loadIdentifierProperties();
  }

  /**
   * Loads the identifier strings (keys, CSS classes, IDs, etc.) from the properties file.
   * @return A Properties object containing the loaded identifiers.
   * @throws RuntimeException If the properties file cannot be found or read.
   */
  private Properties loadIdentifierProperties() {
    Properties props = new Properties();
    try (InputStream input = OutcomesSectionBuilder.class.getResourceAsStream(IDENTIFIERS_PROPERTIES_PATH)) {
      if (input == null) {
        LOG.error("CRITICAL: Unable to find identifiers properties file: {}", IDENTIFIERS_PROPERTIES_PATH);
        throw new RuntimeException("Missing required identifiers properties file: " + IDENTIFIERS_PROPERTIES_PATH);
      }
      props.load(input);
    } catch (IOException ex) {
      LOG.error("CRITICAL: Error loading identifiers properties file: {}", IDENTIFIERS_PROPERTIES_PATH, ex);
      throw new RuntimeException("Error loading identifiers properties file", ex);
    }
    return props;
  }

  /**
   * Retrieves an identifier value from the loaded identifier properties.
   * @param key The key for the identifier.
   * @return The identifier string.
   * @throws RuntimeException If the key is not found.
   */
  private String getId(String key) {
    String value = identifierProps.getProperty(key);
    if (value == null || value.trim().isEmpty()) {
      LOG.error("Missing identifier in OutcomesSectionBuilder properties file for key: {}", key);
      throw new RuntimeException("Missing identifier in OutcomesSectionBuilder properties file for key: " + key);
    }
    return value;
  }

  /**
   * Builds the full UI node representing the "Outcomes" section of the editor.
   * Includes controls for selecting outcome types, managing dynamic variables,
   * viewing outcomes, and editing parameters. Layout is controlled via CSS.
   *
   * @return A VBox node containing all outcome-related controls and displays.
   */
  public Node build() {
    VBox sectionPane = new VBox();
    sectionPane.setId(getId("id.sectionVbox"));
    sectionPane.getStyleClass().add(getId("style.inputSubSection"));

    Label header = createHeaderLabel(getId("key.outcomesHeader"));
    HBox outcomeSelectionRow = createOutcomeSelectionRow();
    Node dynamicVariableRow = createDynamicVariableSelectionRow();
    setupOutcomesListView();
    Node parametersSection = buildParametersSection();

    sectionPane.getChildren().addAll(
        header,
        outcomeSelectionRow,
        dynamicVariableRow,
        outcomesListView,
        parametersSection
    );
    VBox.setVgrow(outcomesListView, Priority.SOMETIMES);
    VBox.setVgrow(parametersSection.getParent(), Priority.SOMETIMES);

    LOG.debug("Outcomes section UI built.");
    return sectionPane;
  }

  /**
   * Creates the HBox row containing controls for selecting an outcome type and adding/removing outcomes.
   * Layout managed by CSS.
   *
   * @return HBox node for outcome selection.
   */
  private HBox createOutcomeSelectionRow() {
    HBox selectionBox = new HBox();
    selectionBox.setId(getId("id.outcomeSelectionRow"));
    selectionBox.setAlignment(Pos.CENTER_LEFT);

    setupOutcomeTypeComboBox();

    Button addOutcomeButton = createButton(getId("key.addOutcomeButton"), e -> handleAddOutcomeAction());
    addOutcomeButton.setId(getId("id.addOutcomeButton"));
    addOutcomeButton.setMaxWidth(Double.MAX_VALUE);

    Button removeOutcomeButton = createButton(getId("key.removeOutcomeButton"), e -> handleRemoveOutcomeAction());
    removeOutcomeButton.setId(getId("id.removeOutcomeButton"));
    removeOutcomeButton.getStyleClass().add(getId("style.removeButton"));

    selectionBox.getChildren().addAll(outcomeTypeComboBox, addOutcomeButton, removeOutcomeButton);
    HBox.setHgrow(outcomeTypeComboBox, Priority.ALWAYS);

    return selectionBox;
  }

  /**
   * Initializes and configures the ComboBox used for selecting the type of outcome to add.
   * Populates items from the outcomeTypeSupplier and sets prompt text.
   */
  private void setupOutcomeTypeComboBox() {
    outcomeTypeComboBox = new ComboBox<>(
        FXCollections.observableArrayList(outcomeTypeSupplier.get()));
    outcomeTypeComboBox.setId(getId("id.outcomeTypeComboBox"));
    outcomeTypeComboBox.setPromptText(uiBundle.getString(getId("key.promptSelectOutcome")));
    outcomeTypeComboBox.setMaxWidth(Double.MAX_VALUE);
  }

  /**
   * Creates the HBox row containing the label, ComboBox for selecting dynamic variables (parameters),
   * and a button to trigger the creation of new variables. Layout managed by CSS.
   *
   * @return Node representing the dynamic variable selection row.
   */
  private Node createDynamicVariableSelectionRow() {
    HBox paramBox = new HBox();
    paramBox.setId(getId("id.dynamicVariableRow"));
    paramBox.setAlignment(Pos.CENTER_LEFT);

    Label label = new Label(uiBundle.getString(getId("key.parameterLabel")) + ":");
    dynamicVariableComboBox = new ComboBox<>();
    dynamicVariableComboBox.setId(getId("id.dynamicVariableComboBox"));
    dynamicVariableComboBox.setPromptText(uiBundle.getString(getId("key.promptSelectParameter")));
    dynamicVariableComboBox.setMaxWidth(Double.MAX_VALUE);

    Button createButton = createButton(getId("key.createParamButton"), e -> createParameterHandler.run());
    createButton.setId(getId("id.addVariableButton"));

    paramBox.getChildren().addAll(label, dynamicVariableComboBox, createButton);
    HBox.setHgrow(dynamicVariableComboBox, Priority.ALWAYS);
    return paramBox;
  }

  /**
   * Initializes and configures the ListView used for displaying the added outcomes.
   * Sets up a listener to update the parameters pane when the selection changes.
   * Height is controlled via CSS.
   */
  private void setupOutcomesListView() {
    outcomesListView = new ListView<>();
    outcomesListView.setId(getId("id.outcomesListView"));
    outcomesListView.getStyleClass().add(getId("style.dataListView"));

    outcomesListView.getSelectionModel().selectedItemProperty().addListener(
        (obs, oldVal, newVal) -> updateParametersPane(newVal)
    );
  }

  /**
   * Builds the parameters section UI, which includes a header label and a scrollable pane
   * containing a VBox where parameter editing controls (GridPane) will be dynamically added.
   * Height of the scrollable area is controlled via CSS.
   *
   * @return Node representing the container for the parameters section.
   */
  private Node buildParametersSection() {
    VBox container = new VBox();
    container.setId(getId("id.parametersContainer"));
    Label header = createHeaderLabel(getId("key.executorParametersHeader"));

    parametersPane = new VBox();
    parametersPane.setId(getId("id.parametersPane"));

    ScrollPane scrollPane = new ScrollPane(parametersPane);
    scrollPane.setFitToWidth(true);
    scrollPane.setId(getId("id.parametersScrollPane"));

    container.getChildren().addAll(header, scrollPane);
    return container;
  }

  /**
   * Handles the action event triggered by the "Add Outcome" button.
   * Retrieves the selected outcome type and delegates the addition to the addOutcomeHandler.
   */
  private void handleAddOutcomeAction() {
    String selectedType = outcomeTypeComboBox.getSelectionModel().getSelectedItem();
    if (selectedType != null && !selectedType.trim().isEmpty()) {
      addOutcomeHandler.handle(selectedType.trim());
      outcomeTypeComboBox.getSelectionModel().clearSelection();
      outcomeTypeComboBox.setPromptText(uiBundle.getString(getId("key.promptSelectOutcome")));
    } else {
      LOG.warn(uiBundle.getString(getId("key.warnNoOutcomeType")));
    }
  }

  /**
   * Handles the action event triggered by the "Remove Outcome" button.
   * Retrieves the selected outcome item and delegates its removal (by index) to the removeOutcomeHandler.
   */
  private void handleRemoveOutcomeAction() {
    OutcomeDisplayItem selected = outcomesListView.getSelectionModel().getSelectedItem();
    if (selected != null) {
      removeOutcomeHandler.accept(selected.getIndex());
    } else {
      LOG.warn(uiBundle.getString(getId("key.warnNoOutcomeSelected")));
    }
  }

  /**
   * Updates the parameters editing pane based on the currently selected outcome item.
   * Clears previous controls and rebuilds the parameter grid if an item is selected.
   *
   * @param selectedItem The currently selected {@link OutcomeDisplayItem}, or null if none selected.
   */
  private void updateParametersPane(OutcomeDisplayItem selectedItem) {
    parametersPane.getChildren().clear();
    if (selectedItem == null || selectedItem.getData() == null) {
      return;
    }

    ExecutorData data = selectedItem.getData();
    GridPane grid = createParametersGrid();

    addStringParametersToGrid(grid, data.getStringParams(), selectedItem);
    addDoubleParametersToGrid(grid, data.getDoubleParams(), selectedItem);

    parametersPane.getChildren().add(grid);
    LOG.trace("Parameters pane updated for outcome item index {}", selectedItem.getIndex());
  }

  /**
   * Helper method to create a standard GridPane for displaying parameters.
   * Gap properties are set via CSS.
   *
   * @return A new GridPane instance.
   */
  private GridPane createParametersGrid() {
    GridPane grid = new GridPane();
    grid.setId(getId("id.parametersGrid"));
    return grid;
  }

  /**
   * Adds rows to the parameters grid for each String parameter found in the outcome data.
   * Uses the generic addParameterRow helper.
   *
   * @param grid         The GridPane to add rows to.
   * @param params       A map of String parameter names to their values.
   * @param selectedItem The OutcomeDisplayItem whose parameters are being displayed.
   */
  private void addStringParametersToGrid(GridPane grid, Map<String, String> params, OutcomeDisplayItem selectedItem) {
    if (params == null) return;
    int rowIndex = grid.getRowCount();
    String typeSuffix = uiBundle.getString(getId("key.paramTypeString"));
    for (Map.Entry<String, String> entry : params.entrySet()) {
      addParameterRow(grid, rowIndex++, entry.getKey(), typeSuffix, entry.getValue(),
          (key, valueText) -> handleStringParamUpdate(selectedItem, key, valueText));
    }
  }

  /**
   * Adds rows to the parameters grid for each Double parameter found in the outcome data.
   * Uses the generic addParameterRow helper.
   *
   * @param grid         The GridPane to add rows to.
   * @param params       A map of Double parameter names to their values.
   * @param selectedItem The OutcomeDisplayItem whose parameters are being displayed.
   */
  private void addDoubleParametersToGrid(GridPane grid, Map<String, Double> params, OutcomeDisplayItem selectedItem) {
    if (params == null) return;
    int rowIndex = grid.getRowCount();
    String typeSuffix = uiBundle.getString(getId("key.paramTypeDouble"));
    for (Map.Entry<String, Double> entry : params.entrySet()) {
      String originalValueStr = String.valueOf(entry.getValue());
      addParameterRow(grid, rowIndex++, entry.getKey(), typeSuffix, originalValueStr,
          (key, valueText) -> handleDoubleParamUpdate(selectedItem, key, valueText, originalValueStr));
    }
  }

  /**
   * Adds a single parameter row (Label + TextField) to the specified GridPane.
   * Configures the TextField to trigger an update via the provided handler on action (Enter key).
   *
   * @param grid          The GridPane to add the row to.
   * @param rowIndex      The row index for the new row.
   * @param paramName     The name of the parameter.
   * @param typeSuffix    The display suffix indicating the parameter type (e.g., "(String)").
   * @param initialValue  The initial value to display in the TextField.
   * @param updateHandler The handler to call when the value is updated.
   */
  private void addParameterRow(GridPane grid, int rowIndex, String paramName, String typeSuffix, String initialValue, ParameterUpdateHandler updateHandler) {
    Label nameLabel = new Label(paramName + " " + typeSuffix + ":");
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
   * Handles the update for a String parameter, delegating to the {@code editOutcomeParamHandler}.
   *
   * @param item     The {@link OutcomeDisplayItem} being edited.
   * @param key      The name/key of the String parameter.
   * @param newValue The new String value from the TextField.
   */
  private void handleStringParamUpdate(OutcomeDisplayItem item, String key, String newValue) {
    editOutcomeParamHandler.handle(item.getIndex(), key, newValue);
    LOG.trace("String parameter '{}' updated via ActionEvent to: {}", key, newValue);
  }

  /**
   * Handles the update for a Double parameter, attempting to parse the input and delegating
   * to the {@code editOutcomeParamHandler}. Logs a warning on parsing failure.
   *
   * @param item              The {@link OutcomeDisplayItem} being edited.
   * @param key               The name/key of the Double parameter.
   * @param newValueText      The new value text from the TextField.
   * @param originalValueText The original value as text (currently unused, could be for reset).
   */
  private void handleDoubleParamUpdate(OutcomeDisplayItem item, String key, String newValueText, String originalValueText) {
    try {
      Double doubleVal = Double.parseDouble(newValueText);
      editOutcomeParamHandler.handle(item.getIndex(), key, doubleVal);
      LOG.trace("Double parameter '{}' updated via ActionEvent to: {}", key, doubleVal);
    } catch (NumberFormatException e) {
      LOG.warn(String.format(uiBundle.getString(getId("key.warnInvalidDouble")), key, newValueText));
    }
  }

  /**
   * Updates the list view of outcomes shown in the UI with a new list of {@code ExecutorData}.
   * Clears the existing items and populates with new {@link OutcomeDisplayItem} wrappers.
   * Also clears the parameters pane until a new selection is made.
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
    updateParametersPane(null);
    LOG.trace("Outcomes list view updated with {} items.", displayItems.size());
  }

  /**
   * Updates the ComboBox containing available dynamic variable names.
   * Fetches the latest list of variables from the supplier, clears the ComboBox,
   * populates it with distinct, sorted variable names, and resets the selection/prompt.
   */
  public void updateDynamicVariableComboBox() {
    List<DynamicVariable> variables = dynamicVariableSupplier.get();
    dynamicVariableComboBox.getItems().clear();

    if (variables != null && !variables.isEmpty()) {
      List<String> varNames = variables.stream()
          .map(DynamicVariable::getName)
          .filter(Objects::nonNull)
          .distinct()
          .sorted()
          .collect(Collectors.toList());
      dynamicVariableComboBox.getItems().addAll(varNames);
      LOG.debug("Updated dynamic variable combo box with {} variables.", varNames.size());
    } else {
      LOG.debug("Dynamic variable combo box updated with empty list or null variables.");
    }
    dynamicVariableComboBox.setPromptText(uiBundle.getString(getId("key.promptSelectParameter")));
    dynamicVariableComboBox.getSelectionModel().clearSelection();
  }

  /**
   * Creates a styled header label using text retrieved from the resource bundle based on the
   * provided identifier key.
   *
   * @param identifierKey The key corresponding to the header text identifier in the properties file.
   * @return A styled {@code Label} configured as a section header.
   */
  private Label createHeaderLabel(String identifierKey) {
    Label label = new Label(uiBundle.getString(identifierKey));
    label.getStyleClass().add(getId("style.sectionHeader"));
    label.setMaxWidth(Double.MAX_VALUE);
    return label;
  }

  /**
   * Creates a styled {@code Button} with text configured based on the identifier key and resource bundle.
   * Assigns the provided action handler. Handles special case for the '+' button text.
   *
   * @param identifierKey The key in the identifier properties file for the button's configuration/resource key.
   * @param handler   The event handler to be executed when the button is clicked.
   * @return A configured and styled {@code Button}.
   */
  private Button createButton(String identifierKey, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
    String buttonText;
    if (identifierKey.equals(getId("key.createParamButton"))) {
      buttonText = getId("text.createParamButton");
    } else {
      buttonText = uiBundle.getString(identifierKey);
    }

    Button button = new Button(buttonText);
    button.setOnAction(handler);
    button.getStyleClass().add(getId("style.actionButton"));

    if (identifierKey.equals(getId("key.createParamButton"))) {
      button.getStyleClass().add(getId("style.smallButton"));
      button.setMaxWidth(Region.USE_PREF_SIZE);
    } else {
      button.setMaxWidth(Double.MAX_VALUE);
      HBox.setHgrow(button, Priority.ALWAYS);
    }

    return button;
  }
}