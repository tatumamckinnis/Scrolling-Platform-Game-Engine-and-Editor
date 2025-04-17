package oogasalad.editor.view;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
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
 * @author Tatum McKinnis
 */
public class ConditionsSectionBuilder {

  private static final Logger LOG = LogManager.getLogger(ConditionsSectionBuilder.class);

  private static final double LIST_VIEW_HEIGHT = 120.0;
  private static final String KEY_CONDITIONS_HEADER = "conditionsHeader";
  private static final String KEY_ADD_GROUP_BUTTON = "addGroupButton";
  private static final String KEY_REMOVE_GROUP_BUTTON = "removeGroupButton";
  private static final String KEY_ADD_CONDITION_BUTTON = "addConditionButton";
  private static final String KEY_REMOVE_CONDITION_BUTTON = "removeConditionButton";
  private static final String KEY_PARAMETERS_HEADER = "parametersHeader";
  private static final String PROMPT_SELECT_CONDITION = "Select Condition Type";

  private static final double DEFAULT_PADDING = 12.0;
  private static final double DEFAULT_SPACING = 8.0;


  private final ResourceBundle uiBundle;
  private final Supplier<List<String>> conditionTypeSupplier;
  private final Runnable addGroupHandler;
  private final IntConsumer removeGroupHandler;
  private final AddConditionHandler addConditionHandler;
  private final RemoveConditionHandler removeConditionHandler;
  private final EditConditionParamHandler editConditionParamHandler;

  private ComboBox<String> conditionTypeComboBox;
  private ListView<ConditionDisplayItem> conditionsListView;
  private VBox parametersPane;


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
  }

  public Node build() {
    VBox sectionPane = new VBox(DEFAULT_SPACING);
    sectionPane.getStyleClass().add("input-sub-section");
    sectionPane.setPadding(new Insets(DEFAULT_PADDING));

    Label header = createHeaderLabel(KEY_CONDITIONS_HEADER);
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
    VBox.setVgrow(parametersSection.getParent(), Priority.SOMETIMES); // Assuming parametersSection is wrapped
    LOG.debug("Conditions section UI built.");
    return sectionPane;
  }

  private HBox createGroupButtonRow() {
    Button addGroupButton = createButton(KEY_ADD_GROUP_BUTTON, e -> handleAddGroupAction());
    addGroupButton.setId("addGroupButton");

    Button removeGroupButton = createButton(KEY_REMOVE_GROUP_BUTTON,
        e -> handleRemoveGroupAction());
    removeGroupButton.setId("removeGroupButton");
    removeGroupButton.getStyleClass().add("remove-button");

    return createButtonBox(DEFAULT_SPACING, addGroupButton, removeGroupButton);
  }

  private HBox createConditionSelectionRow() {
    HBox selectionBox = new HBox(DEFAULT_SPACING / 2);
    selectionBox.setAlignment(Pos.CENTER_LEFT);

    setupConditionTypeComboBox();

    Button addConditionButton = createButton(KEY_ADD_CONDITION_BUTTON,
        e -> handleAddConditionAction());
    addConditionButton.setId("addConditionButton");
    addConditionButton.setMaxWidth(Double.MAX_VALUE);

    Button removeConditionButton = createButton(KEY_REMOVE_CONDITION_BUTTON,
        e -> handleRemoveConditionAction());
    removeConditionButton.setId("removeConditionButton");
    removeConditionButton.getStyleClass().add("remove-button");

    selectionBox.getChildren()
        .addAll(conditionTypeComboBox, addConditionButton, removeConditionButton);
    HBox.setHgrow(conditionTypeComboBox, Priority.ALWAYS);

    return selectionBox;
  }

  private void setupConditionTypeComboBox() {
    conditionTypeComboBox = new ComboBox<>(
        FXCollections.observableArrayList(conditionTypeSupplier.get()));
    conditionTypeComboBox.setId("conditionTypeComboBox");
    conditionTypeComboBox.setPromptText(PROMPT_SELECT_CONDITION);
    conditionTypeComboBox.setMaxWidth(Double.MAX_VALUE);
  }


  private void setupConditionsListView() {
    conditionsListView = new ListView<>();
    conditionsListView.setId("conditionsListView");
    conditionsListView.setPrefHeight(LIST_VIEW_HEIGHT);
    conditionsListView.getStyleClass().add("data-list-view");

    conditionsListView.getSelectionModel().selectedItemProperty()
        .addListener((obs, oldVal, newVal) -> updateParametersPane(newVal));
  }

  private Node buildParametersSection() {
    VBox container = new VBox(DEFAULT_SPACING / 2);
    Label header = createHeaderLabel(KEY_PARAMETERS_HEADER);
    parametersPane = new VBox(DEFAULT_SPACING / 2);
    parametersPane.setId("conditionParametersPane");
    parametersPane.setPadding(new Insets(DEFAULT_SPACING / 2, 0, 0, 0));

    ScrollPane scrollPane = new ScrollPane(parametersPane);
    scrollPane.setFitToWidth(true);
    scrollPane.setPrefHeight(80);

    container.getChildren().addAll(header, scrollPane);
    return container;
  }

  private void handleAddGroupAction() {
    addGroupHandler.run();
  }


  private void handleRemoveGroupAction() {
    ConditionDisplayItem selected = conditionsListView.getSelectionModel().getSelectedItem();
    if (selected != null) {
      removeGroupHandler.accept(selected.getGroupIndex());
    } else {
      LOG.warn("No condition selected, cannot determine group to remove.");
    }
  }

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
  }


  private void handleRemoveConditionAction() {
    ConditionDisplayItem selected = conditionsListView.getSelectionModel().getSelectedItem();
    if (selected != null) {
      removeConditionHandler.handle(selected.getGroupIndex(), selected.getConditionIndex());
    } else {
      LOG.warn("No condition selected for removal.");
    }
  }

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
    LOG.trace("Parameters pane updated for item Group {}, Index {}", selectedItem.getGroupIndex(),
        selectedItem.getConditionIndex());
  }

  private GridPane createParametersGrid() {
    GridPane grid = new GridPane();
    grid.setHgap(DEFAULT_SPACING / 2);
    grid.setVgap(DEFAULT_SPACING / 2);
    return grid;
  }

  private void addStringParametersToGrid(GridPane grid, Map<String, String> params,
      ConditionDisplayItem selectedItem) {
    if (params == null) {
      return;
    }
    int rowIndex = grid.getRowCount();
    for (Map.Entry<String, String> entry : params.entrySet()) {
      addParameterRow(grid, rowIndex++, entry.getKey(), "(String)", entry.getValue(),
          (key, valueText) -> handleStringParamUpdate(selectedItem, key, valueText));
    }
  }

  private void addDoubleParametersToGrid(GridPane grid, Map<String, Double> params,
      ConditionDisplayItem selectedItem) {
    if (params == null) {
      return;
    }
    int rowIndex = grid.getRowCount();
    for (Map.Entry<String, Double> entry : params.entrySet()) {
      addParameterRow(grid, rowIndex++, entry.getKey(), "(Double)",
          String.valueOf(entry.getValue()),
          (key, valueText) -> handleDoubleParamUpdate(selectedItem, key, valueText,
              String.valueOf(entry.getValue()))); // Pass original value for reset
    }
  }

  private void addParameterRow(GridPane grid, int rowIndex, String paramName, String typeSuffix,
      String initialValue, ParameterUpdateHandler updateHandler) {
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

  private void handleStringParamUpdate(ConditionDisplayItem item, String key, String newValue) {
    editConditionParamHandler.handle(item.getGroupIndex(), item.getConditionIndex(), key,
        newValue);
    LOG.trace("String parameter '{}' updated via ActionEvent to: {}", key, newValue);
  }

  private void handleDoubleParamUpdate(ConditionDisplayItem item, String key, String newValueText,
      String originalValueText) {
    try {
      Double doubleVal = Double.parseDouble(newValueText);
      editConditionParamHandler.handle(item.getGroupIndex(), item.getConditionIndex(), key,
          doubleVal);
      LOG.trace("Double parameter '{}' updated via ActionEvent to: {}", key, doubleVal);
    } catch (NumberFormatException e) {
      LOG.warn("Invalid double format for param '{}' on ActionEvent: {}", key, newValueText);
    }
  }

  public void updateConditionsListView(List<List<ExecutorData>> conditionGroups) {
    ObservableList<ConditionDisplayItem> displayItems = FXCollections.observableArrayList();
    if (conditionGroups != null) {
      processConditionGroups(conditionGroups, displayItems);
    }
    conditionsListView.setItems(displayItems);
    updateParametersPane(null);
    LOG.trace("Conditions list view updated with {} items.", displayItems.size());
  }

  private void processConditionGroups(List<List<ExecutorData>> conditionGroups,
      ObservableList<ConditionDisplayItem> displayItems) {
    for (int groupIndex = 0; groupIndex < conditionGroups.size(); groupIndex++) {
      List<ExecutorData> group = conditionGroups.get(groupIndex);
      processSingleGroup(group, groupIndex, displayItems);
    }
  }

  private void processSingleGroup(List<ExecutorData> group, int groupIndex,
      ObservableList<ConditionDisplayItem> displayItems) {
    if (group == null) {
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

  private Label createHeaderLabel(String bundleKey) {
    Label label = new Label(uiBundle.getString(bundleKey));
    label.getStyleClass().add("section-header");
    label.setMaxWidth(Double.MAX_VALUE);
    return label;
  }

  private Button createButton(String bundleKey,
      javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
    Button button = new Button(uiBundle.getString(bundleKey));
    button.setOnAction(handler);
    button.getStyleClass().add("action-button");
    return button;
  }

  private HBox createButtonBox(double spacing, Button... buttons) {
    HBox buttonBox = new HBox(spacing);
    buttonBox.setAlignment(Pos.CENTER_LEFT);
    buttonBox.getChildren().addAll(buttons);
    return buttonBox;
  }
}