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

/**
 * Builds the UI section for managing Outcomes associated with an Event. Handles selecting outcome
 * types, adding/removing outcomes by index, selecting dynamic variables (as potential parameters),
 * and editing specific parameters (String/Double) defined within the outcome's ExecutorData. Uses
 * separate functional interfaces for handlers.
 */
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

  /**
   * Constructs a builder for the outcomes UI section.
   *
   * @param uiBundle                Resource bundle for UI text localization.
   * @param outcomeTypeSupplier     Supplier providing a List of available outcome type names
   *                                (Strings).
   * @param dynamicVariableSupplier Supplier providing a List of available DynamicVariables for
   *                                potential use as parameters.
   * @param addOutcomeHandler       Handler (implementing {@link AddOutcomeHandler}) executed when
   *                                "Add Outcome" is clicked.
   * @param removeOutcomeHandler    IntConsumer executed when "Remove Outcome" is clicked, accepting
   *                                the index of the outcome to remove.
   * @param createParameterHandler  Runnable executed when the "Create Parameter" (+) button (for
   *                                Dynamic Variables) is clicked.
   * @param editOutcomeParamHandler Handler (implementing {@link EditOutcomeParamHandler}) executed
   *                                when an ExecutorData parameter value is modified.
   * @throws NullPointerException if any argument is null.
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
  }

  /**
   * Builds and returns the complete UI Node for the outcomes management section. This includes
   * controls for outcome type selection, dynamic variable selection, adding/removing outcomes,
   * displaying the list of outcomes, and an area for editing the specific parameters
   * (String/Double) defined within the selected outcome's ExecutorData.
   *
   * @return The constructed {@code Node} representing the outcomes UI section.
   */
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
    VBox.setVgrow(parametersSection.getParent(), Priority.SOMETIMES);
    LOG.debug("Outcomes section UI built.");
    return sectionPane;
  }

  /**
   * Creates the horizontal layout (HBox) containing the outcome type ComboBox and the Add/Remove
   * outcome buttons.
   *
   * @return The {@code HBox} node for selecting outcome types and adding/removing outcomes.
   */
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

  /**
   * Initializes and configures the ComboBox used for selecting outcome types. Populates it with
   * values obtained from the {@code outcomeTypeSupplier}.
   */
  private void setupOutcomeTypeComboBox() {
    outcomeTypeComboBox = new ComboBox<>(
        FXCollections.observableArrayList(outcomeTypeSupplier.get()));
    outcomeTypeComboBox.setId("outcomeTypeComboBox");
    outcomeTypeComboBox.setPromptText(PROMPT_SELECT_OUTCOME);
    outcomeTypeComboBox.setMaxWidth(Double.MAX_VALUE);
  }

  /**
   * Creates the horizontal layout (HBox) containing the dynamic variable label, the selection
   * ComboBox for dynamic variables, and the 'Create Parameter' (+) button used to trigger the
   * addition of new dynamic variables globally.
   *
   * @return A {@code Node} (specifically an HBox) representing the dynamic variable selection row.
   */
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

  /**
   * Initializes and configures the ListView that displays the added outcomes. Sets the cell factory
   * to display {@link OutcomeDisplayItem} objects and adds a listener to update the parameters pane
   * whenever the selection changes.
   */
  private void setupOutcomesListView() {
    outcomesListView = new ListView<>(); // Uses separate class
    outcomesListView.setId("outcomesListView");
    outcomesListView.setPrefHeight(LIST_VIEW_HEIGHT);
    outcomesListView.getStyleClass().add("data-list-view");

    outcomesListView.getSelectionModel().selectedItemProperty()
        .addListener((obs, oldVal, newVal) -> {
          updateParametersPane(newVal);
        });
  }

  /**
   * Creates the UI section dedicated to displaying and editing the ExecutorData parameters
   * (String/Double) of the currently selected outcome. Includes a header and a scrollable pane.
   *
   * @return A {@code Node} (specifically a VBox) containing the parameter editing UI for
   * ExecutorData.
   */
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

  /**
   * Handles the action for the "Add Outcome" button. Retrieves the selected outcome type from the
   * ComboBox and invokes the {@code addOutcomeHandler} with the type name.
   */
  private void handleAddOutcomeAction() {
    String selectedType = outcomeTypeComboBox.getSelectionModel().getSelectedItem();
    if (selectedType != null && !selectedType.trim().isEmpty()) {
      addOutcomeHandler.handle(selectedType.trim());
      outcomeTypeComboBox.getSelectionModel().clearSelection();
    } else {
      LOG.warn("No outcome type selected.");
    }
  }

  /**
   * Handles the action for the "Remove Outcome" button. If an outcome is selected in the ListView,
   * invokes the {@code removeOutcomeHandler} with the index of the selected item. Otherwise, logs a
   * warning.
   */
  private void handleRemoveOutcomeAction() {
    OutcomeDisplayItem selected = outcomesListView.getSelectionModel().getSelectedItem();
    if (selected != null) {
      removeOutcomeHandler.accept(selected.index);
    } else {
      LOG.warn("No outcome selected for removal.");
    }
  }


  /**
   * Updates the content of the ExecutorData parameters pane based on the currently selected
   * outcome. Clears the pane if no outcome is selected. Otherwise, dynamically creates
   * Label-TextField pairs for each String and Double parameter found in the selected outcome's
   * {@link ExecutorData}. Attaches listeners to the TextFields to invoke the
   * {@code editOutcomeParamHandler} upon action (Enter key).
   *
   * @param selectedItem The currently selected {@link OutcomeDisplayItem}, or {@code null} if none
   *                     is selected.
   */
  private void updateParametersPane(OutcomeDisplayItem selectedItem) {
    parametersPane.getChildren().clear();
    if (selectedItem == null || selectedItem.data == null) {
      return;
    }

    ExecutorData data = selectedItem.data;
    GridPane grid = new GridPane();
    grid.setHgap(DEFAULT_SPACING / 2);
    grid.setVgap(DEFAULT_SPACING / 2);
    int rowIndex = 0;

    if (data.getStringParams() != null) {
      for (Map.Entry<String, String> entry : data.getStringParams().entrySet()) {
        Label nameLabel = new Label(entry.getKey() + " (String):");
        TextField valueField = new TextField(entry.getValue());

        valueField.setOnAction(event -> {
          String newValue = valueField.getText();
          editOutcomeParamHandler.handle(selectedItem.index, entry.getKey(), newValue);
          LOG.trace("String parameter '{}' updated via ActionEvent to: {}", entry.getKey(),
              newValue);
        });

        grid.add(nameLabel, 0, rowIndex);
        grid.add(valueField, 1, rowIndex++);
        GridPane.setHgrow(valueField, Priority.ALWAYS);
      }
    }

    if (data.getDoubleParams() != null) {
      for (Map.Entry<String, Double> entry : data.getDoubleParams().entrySet()) {
        Label nameLabel = new Label(entry.getKey() + " (Double):");
        TextField valueField = new TextField(String.valueOf(entry.getValue()));

        valueField.setOnAction(event -> {
          String newValText = valueField.getText();
          try {
            Double doubleVal = Double.parseDouble(newValText);
            editOutcomeParamHandler.handle(selectedItem.index, entry.getKey(), doubleVal);
            LOG.trace("Double parameter '{}' updated via ActionEvent to: {}", entry.getKey(),
                doubleVal);
          } catch (NumberFormatException e) {
            LOG.warn("Invalid double format for param '{}' on ActionEvent: {}", entry.getKey(),
                newValText);
            valueField.setText(String.valueOf(entry.getValue()));
          }
        });

        grid.add(nameLabel, 0, rowIndex);
        grid.add(valueField, 1, rowIndex++);
        GridPane.setHgrow(valueField, Priority.ALWAYS);
      }
    }
    parametersPane.getChildren().add(grid);
    LOG.trace("Parameters pane updated for item index {}", selectedItem.index);
  }

  /**
   * Updates the outcomes ListView to display the provided list of outcomes. Converts the List of
   * {@link ExecutorData} into a flat ObservableList of {@link OutcomeDisplayItem}. Clears the
   * ExecutorData parameter pane after updating the list.
   *
   * @param outcomes A {@code List<ExecutorData>} representing the outcomes for the current event.
   *                 Can be null or empty.
   */
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

  /**
   * Updates the items available in the dynamic variable ComboBox using the
   * {@code dynamicVariableSupplier}. Clears existing items and adds the names of the provided
   * {@link DynamicVariable}s. Typically called when the context changes (e.g., new object selected)
   * or variables are added/removed.
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
    dynamicVariableComboBox.setPromptText(PROMPT_SELECT_PARAMETER);
    dynamicVariableComboBox.getSelectionModel().clearSelection();
  }

  /**
   * Creates a styled header label using text retrieved from the resource bundle based on the
   * provided key.
   *
   * @param bundleKey The key corresponding to the header text in the resource bundle.
   * @return A styled {@code Label} configured as a section header.
   */
  private Label createHeaderLabel(String bundleKey) {
    Label label = new Label(uiBundle.getString(bundleKey));
    label.getStyleClass().add("section-header");
    label.setMaxWidth(Double.MAX_VALUE);
    return label;
  }

  /**
   * Creates a styled button with text from the resource bundle and assigns the provided action
   * handler. Handles specific styling and text ('+') for the 'Create Parameter' (Dynamic Variable)
   * button.
   *
   * @param bundleKey The key in the resource bundle for the button's text or an identifier like
   *                  {@code KEY_CREATE_PARAM_BUTTON}.
   * @param handler   The event handler to be executed when the button is clicked.
   * @return A configured and styled {@code Button}.
   */
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