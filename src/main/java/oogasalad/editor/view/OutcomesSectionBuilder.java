package oogasalad.editor.view;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import oogasalad.editor.model.data.object.DynamicVariable;
import oogasalad.editor.model.data.object.event.ExecutorData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class OutcomesSectionBuilder {

  private static final Logger LOG = LogManager.getLogger(OutcomesSectionBuilder.class);

  private static final double LIST_VIEW_HEIGHT = 120.0;
  private static final String KEY_OUTCOMES_HEADER = "outcomesHeader";
  private static final String KEY_PARAMETER_LABEL = "parameterLabel";
  private static final String KEY_CREATE_PARAM_BUTTON = "createParamButton";
  private static final String KEY_ADD_OUTCOME_BUTTON = "addOutcomeButton";
  private static final String KEY_REMOVE_OUTCOME_BUTTON = "removeOutcomeButton";
  private static final String KEY_EXECUTOR_PARAMETERS_HEADER = "executorParametersHeader";
  private static final String PROMPT_SELECT_OUTCOME = "Select Outcome Type";
  private static final String PROMPT_SELECT_PARAMETER = "Select Variable Parameter";

  private static final double DEFAULT_PADDING = 12.0;
  private static final double DEFAULT_SPACING = 8.0;

  private final ResourceBundle uiBundle;
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
  }


  public Node build() {
    VBox sectionPane = new VBox(DEFAULT_SPACING);
    sectionPane.getStyleClass().add("input-sub-section");
    sectionPane.setPadding(new Insets(DEFAULT_PADDING));

    Label header = createHeaderLabel(KEY_OUTCOMES_HEADER);
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
    VBox.setVgrow(parametersSection.getParent(),
        Priority.SOMETIMES); // Assuming parametersSection is wrapped
    LOG.debug("Outcomes section UI built.");
    return sectionPane;
  }


  private HBox createOutcomeSelectionRow() {
    HBox selectionBox = new HBox(DEFAULT_SPACING / 2);
    selectionBox.setAlignment(Pos.CENTER_LEFT);

    setupOutcomeTypeComboBox();

    Button addOutcomeButton = createButton(KEY_ADD_OUTCOME_BUTTON, e -> handleAddOutcomeAction());
    addOutcomeButton.setId("addOutcomeButton");
    addOutcomeButton.setMaxWidth(Double.MAX_VALUE);

    Button removeOutcomeButton = createButton(KEY_REMOVE_OUTCOME_BUTTON,
        e -> handleRemoveOutcomeAction());
    removeOutcomeButton.setId("removeOutcomeButton");
    removeOutcomeButton.getStyleClass().add("remove-button");

    selectionBox.getChildren().addAll(outcomeTypeComboBox, addOutcomeButton, removeOutcomeButton);
    HBox.setHgrow(outcomeTypeComboBox, Priority.ALWAYS);

    return selectionBox;
  }


  private void setupOutcomeTypeComboBox() {
    outcomeTypeComboBox = new ComboBox<>(
        FXCollections.observableArrayList(outcomeTypeSupplier.get()));
    outcomeTypeComboBox.setId("outcomeTypeComboBox");
    outcomeTypeComboBox.setPromptText(PROMPT_SELECT_OUTCOME);
    outcomeTypeComboBox.setMaxWidth(Double.MAX_VALUE);
  }


  private Node createDynamicVariableSelectionRow() {
    HBox paramBox = new HBox(DEFAULT_SPACING / 2);
    paramBox.setAlignment(Pos.CENTER_LEFT);

    Label label = new Label(uiBundle.getString(KEY_PARAMETER_LABEL) + ":");
    dynamicVariableComboBox = new ComboBox<>();
    dynamicVariableComboBox.setId("dynamicVariableComboBox");
    dynamicVariableComboBox.setPromptText(PROMPT_SELECT_PARAMETER);
    dynamicVariableComboBox.setMaxWidth(Double.MAX_VALUE);

    Button createButton = createButton(KEY_CREATE_PARAM_BUTTON, e -> createParameterHandler.run());
    createButton.setId("addVariableButton");

    paramBox.getChildren().addAll(label, dynamicVariableComboBox, createButton);
    HBox.setHgrow(dynamicVariableComboBox, Priority.ALWAYS);
    return paramBox;
  }


  private void setupOutcomesListView() {
    outcomesListView = new ListView<>();
    outcomesListView.setId("outcomesListView");
    outcomesListView.setPrefHeight(LIST_VIEW_HEIGHT);
    outcomesListView.getStyleClass().add("data-list-view");

    outcomesListView.getSelectionModel().selectedItemProperty()
        .addListener((obs, oldVal, newVal) -> {
          updateParametersPane(newVal);
        });
  }


  private Node buildParametersSection() {
    VBox container = new VBox(DEFAULT_SPACING / 2);
    Label header = createHeaderLabel(KEY_EXECUTOR_PARAMETERS_HEADER);
    parametersPane = new VBox(DEFAULT_SPACING / 2);
    parametersPane.setId("outcomeParametersPane");
    parametersPane.setPadding(new Insets(DEFAULT_SPACING / 2, 0, 0, 0));

    ScrollPane scrollPane = new ScrollPane(parametersPane);
    scrollPane.setFitToWidth(true);
    scrollPane.setPrefHeight(80);

    container.getChildren().addAll(header, scrollPane);
    return container;
  }


  private void handleAddOutcomeAction() {
    String selectedType = outcomeTypeComboBox.getSelectionModel().getSelectedItem();
    if (selectedType != null && !selectedType.trim().isEmpty()) {
      addOutcomeHandler.handle(selectedType.trim());
      outcomeTypeComboBox.getSelectionModel().clearSelection();
    } else {
      LOG.warn("No outcome type selected.");
    }
  }


  private void handleRemoveOutcomeAction() {
    OutcomeDisplayItem selected = outcomesListView.getSelectionModel().getSelectedItem();
    if (selected != null) {
      removeOutcomeHandler.accept(selected.getIndex());
    } else {
      LOG.warn("No outcome selected for removal.");
    }
  }


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

  // Helper to create the grid layout
  private GridPane createParametersGrid() {
    GridPane grid = new GridPane();
    grid.setHgap(DEFAULT_SPACING / 2);
    grid.setVgap(DEFAULT_SPACING / 2);
    return grid;
  }

  private void addStringParametersToGrid(GridPane grid, Map<String, String> params,
      OutcomeDisplayItem selectedItem) {
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
      OutcomeDisplayItem selectedItem) {
    if (params == null) {
      return;
    }
    int rowIndex = grid.getRowCount();
    for (Map.Entry<String, Double> entry : params.entrySet()) {
      addParameterRow(grid, rowIndex++, entry.getKey(), "(Double)",
          String.valueOf(entry.getValue()),
          (key, valueText) -> handleDoubleParamUpdate(selectedItem, key, valueText,
              String.valueOf(entry.getValue())));
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

  private void handleStringParamUpdate(OutcomeDisplayItem item, String key, String newValue) {
    editOutcomeParamHandler.handle(item.getIndex(), key, newValue);
    LOG.trace("String parameter '{}' updated via ActionEvent to: {}", key, newValue);
  }

  private void handleDoubleParamUpdate(OutcomeDisplayItem item, String key, String newValueText,
      String originalValueText) {
    try {
      Double doubleVal = Double.parseDouble(newValueText);
      editOutcomeParamHandler.handle(item.getIndex(), key, doubleVal);
      LOG.trace("Double parameter '{}' updated via ActionEvent to: {}", key, doubleVal);
    } catch (NumberFormatException e) {
      LOG.warn("Invalid double format for param '{}' on ActionEvent: {}", key, newValueText);
    }
  }


  public void updateOutcomesListView(List<ExecutorData> outcomes) {
    ObservableList<OutcomeDisplayItem> displayItems = FXCollections.observableArrayList();
    if (outcomes != null) {
      for (int i = 0; i < outcomes.size(); i++) {
        ExecutorData outcomeData = outcomes.get(i);
        if (outcomeData != null) {
          displayItems.add(new OutcomeDisplayItem(i, outcomeData));
        }
      }
    }
    outcomesListView.setItems(displayItems);
    updateParametersPane(null);
    LOG.trace("Outcomes list view updated with {} items.", displayItems.size());
  }

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
    dynamicVariableComboBox.setPromptText(PROMPT_SELECT_PARAMETER);
    dynamicVariableComboBox.getSelectionModel().clearSelection();
  }


  private Label createHeaderLabel(String bundleKey) {
    Label label = new Label(uiBundle.getString(bundleKey));
    label.getStyleClass().add("section-header");
    label.setMaxWidth(Double.MAX_VALUE);
    return label;
  }

  private Button createButton(String bundleKey,
      javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
    String buttonText =
        bundleKey.equals(KEY_CREATE_PARAM_BUTTON) ? "+" : uiBundle.getString(bundleKey);
    Button button = new Button(buttonText);
    button.setOnAction(handler);
    button.getStyleClass().add("action-button");

    if (bundleKey.equals(KEY_CREATE_PARAM_BUTTON)) {
      button.getStyleClass().add("small-button");
      button.setMaxWidth(Region.USE_PREF_SIZE);
    }
    return button;
  }
}