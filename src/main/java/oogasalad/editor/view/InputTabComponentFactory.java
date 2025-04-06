package oogasalad.editor.view;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.model.data.event_enum.ConditionType;
import oogasalad.editor.model.data.event_enum.OutcomeType;
import oogasalad.editor.model.data.object.DynamicVariable;
import oogasalad.editor.view.resources.EditorResourceLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Factory for the "Input" tab UI. Implements EditorViewListener to react
 * to selection changes and dynamic variable updates notified by the controller.
 * Delegates user actions to the EditorController.
 * (DESIGN-01, DESIGN-07: Decomposed, DESIGN-09: MVC, DESIGN-15, DESIGN-20: Factory Pattern, Observer)
 */
public class InputTabComponentFactory implements EditorViewListener {

  private static final Logger LOG = LogManager.getLogger(InputTabComponentFactory.class);

  private static final String UI_BUNDLE_NAME = "InputTabUI";
  private static final String CSS_PATH = "/css/editorStyles.css";

  private static final String KEY_EVENTS_HEADER = "eventsHeader";
  private static final String KEY_EVENT_ID_LABEL = "eventIdLabel";
  private static final String KEY_ADD_EVENT_BUTTON = "addEventButton";
  private static final String KEY_REMOVE_EVENT_BUTTON = "removeEventButton";
  private static final String KEY_CONDITIONS_HEADER = "conditionsHeader";
  private static final String KEY_ADD_CONDITION_BUTTON = "addConditionButton";
  private static final String KEY_REMOVE_CONDITION_BUTTON = "removeConditionButton";
  private static final String KEY_OUTCOMES_HEADER = "outcomesHeader";
  private static final String KEY_PARAMETER_LABEL = "parameterLabel";
  private static final String KEY_CREATE_PARAM_BUTTON = "createParamButton";
  private static final String KEY_ADD_OUTCOME_BUTTON = "addOutcomeButton";
  private static final String KEY_REMOVE_OUTCOME_BUTTON = "removeOutcomeButton";
  private static final String KEY_DIALOG_ADD_VAR_TITLE = "dialogAddVarTitle";
  private static final String KEY_DIALOG_ADD_BUTTON = "dialogAddButton";
  private static final String KEY_DIALOG_VAR_NAME = "dialogVarName";
  private static final String KEY_DIALOG_VAR_TYPE = "dialogVarType";
  private static final String KEY_DIALOG_VAR_VALUE = "dialogVarValue";
  private static final String KEY_DIALOG_VAR_DESC = "dialogVarDesc";
  private static final String KEY_ERROR_INVALID_INPUT_TITLE = "errorInvalidInputTitle";
  private static final String KEY_ERROR_SELECTION_NEEDED = "errorSelectionNeeded";
  private static final String KEY_ERROR_API_FAILURE = "errorApiFailureTitle";
  private static final String KEY_ERROR_ADD_VAR_FAILED = "errorFailedToAddVariable";
  private static final String KEY_ERROR_LOAD_EVENTS_FAILED = "errorFailedToLoadEvents";
  private static final String KEY_ERROR_LOAD_CONDITIONS_FAILED = "errorFailedToLoadConditions";
  private static final String KEY_ERROR_LOAD_OUTCOMES_FAILED = "errorFailedToLoadOutcomes";
  private static final String KEY_ERROR_LOAD_PARAMS_FAILED = "errorFailedToLoadParameters";

  private static final double DEFAULT_PADDING = 10.0;
  private static final double DEFAULT_SPACING = 10.0;
  private static final double SECTION_SPACING = 20.0;
  private static final double LIST_VIEW_HEIGHT = 150.0;
  private static final double EVENTS_SECTION_WIDTH = 180.0;
  private static final double DIALOG_INPUT_WIDTH = 150.0;

  private final EditorController editorController;

  private final ResourceBundle uiBundle;
  private ListView<String> eventListView;
  private ListView<String> conditionsListView;
  private ListView<String> outcomesListView;
  private ComboBox<ConditionType> conditionComboBox;
  private ComboBox<OutcomeType> outcomeComboBox;
  private ComboBox<String> parameterComboBox;
  private TextField eventIdField;

  private UUID currentObjectId;
  private String currentEventId;

  /**
   * Initializes the factory with the EditorController and loads the UI resources.
   *
   * @param editorController The main controller for editor actions and data access. Must not be null.
   * @throws NullPointerException if editorController is null.
   * @throws RuntimeException if the UI resource bundle cannot be loaded.
   */
  public InputTabComponentFactory(EditorController editorController) {
    this.editorController = Objects.requireNonNull(editorController, "EditorController cannot be null.");
    try {
      this.uiBundle = EditorResourceLoader.loadResourceBundle(UI_BUNDLE_NAME);
    } catch (Exception e) {
      LOG.fatal("Failed to load essential UI resource bundle {}. Cannot continue.", UI_BUNDLE_NAME, e);
      throw new RuntimeException("Failed to load UI resource bundle.", e);
    }
    LOG.info("InputTabComponentFactory initialized.");
  }

  /**
   * Creates the main content pane for the Input tab, assembling the
   * events, conditions, and outcomes sections.
   *
   * @return A Pane containing the fully assembled UI for the Input tab.
   */
  public Pane createInputTabPanel() {
    BorderPane mainPane = new BorderPane();
    mainPane.setId("input-tab-main");
    mainPane.setPadding(new Insets(DEFAULT_PADDING));

    Node eventsSection = createEventsSection();
    Node conditionsOutcomesSection = createConditionsOutcomesSection();

    BorderPane centerPane = new BorderPane();
    centerPane.setLeft(eventsSection);
    centerPane.setCenter(conditionsOutcomesSection);
    BorderPane.setMargin(eventsSection, new Insets(0, SECTION_SPACING, 0, 0));

    mainPane.setCenter(centerPane);

    clearAllLists();
    LOG.debug("Input tab panel created.");
    return mainPane;
  }

  /**
   * Creates the UI section for managing events associated with the selected object.
   * Includes a list view for events, input field for adding new events, and buttons.
   *
   * @return A Node representing the events section UI.
   */
  private Node createEventsSection() {
    VBox section = new VBox(DEFAULT_SPACING);
    section.setPadding(new Insets(DEFAULT_PADDING));
    section.setPrefWidth(EVENTS_SECTION_WIDTH);
    section.getStyleClass().add("input-section");

    Label header = createHeaderLabel(KEY_EVENTS_HEADER);
    HBox inputRow = createEventInputRow();
    eventListView = createListView(LIST_VIEW_HEIGHT);
    HBox buttonRow = createEventButtonRow();

    eventListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
      this.currentEventId = newVal;
      LOG.debug("Internal state: Event selection changed to: {}", currentEventId);
      refreshConditionsList();
      refreshOutcomesList();
    });

    section.getChildren().addAll(header, inputRow, eventListView, buttonRow);
    return section;
  }

  /**
   * Creates the input row for entering a new event ID.
   *
   * @return An HBox containing the label and text field for event ID input.
   */
  private HBox createEventInputRow() {
    HBox inputBox = new HBox(DEFAULT_SPACING / 2);
    Label label = new Label(uiBundle.getString(KEY_EVENT_ID_LABEL));
    eventIdField = new TextField();
    eventIdField.setPromptText(uiBundle.getString(KEY_EVENT_ID_LABEL));
    HBox.setHgrow(eventIdField, Priority.ALWAYS);
    inputBox.getChildren().addAll(label, eventIdField);
    return inputBox;
  }

  /**
   * Creates the button row for adding and removing events.
   *
   * @return An HBox containing the 'Add' and 'Remove' buttons for events.
   */
  private HBox createEventButtonRow() {
    Button addButton = createButton(KEY_ADD_EVENT_BUTTON, e -> handleAddEvent());
    Button removeButton = createButton(KEY_REMOVE_EVENT_BUTTON, e -> handleRemoveEvent());
    return createCenteredButtonBox(addButton, removeButton);
  }

  /**
   * Creates the UI section containing both the conditions and outcomes panes.
   *
   * @return An HBox holding the conditions and outcomes UI sections side-by-side.
   */
  private Node createConditionsOutcomesSection() {
    HBox section = new HBox(SECTION_SPACING);
    section.setPadding(new Insets(DEFAULT_PADDING));
    section.getStyleClass().add("input-section");

    Node conditionsPane = createConditionsPane();
    Node outcomesPane = createOutcomesPane();

    section.getChildren().addAll(conditionsPane, outcomesPane);
    HBox.setHgrow(conditionsPane, Priority.ALWAYS);
    HBox.setHgrow(outcomesPane, Priority.ALWAYS);
    return section;
  }

  /**
   * Creates the UI pane for managing conditions associated with the selected event.
   * Includes a combo box for condition types, a list view, and add/remove buttons.
   *
   * @return A Node representing the conditions management UI pane.
   */
  private Node createConditionsPane() {
    VBox pane = new VBox(DEFAULT_SPACING);
    pane.getStyleClass().add("input-sub-section");

    Label header = createHeaderLabel(KEY_CONDITIONS_HEADER);
    conditionComboBox = new ComboBox<>(FXCollections.observableArrayList(ConditionType.values()));
    conditionComboBox.setPromptText("Select Condition Type");
    conditionComboBox.setMaxWidth(Double.MAX_VALUE);
    conditionsListView = createListView(LIST_VIEW_HEIGHT);
    Button addButton = createButton(KEY_ADD_CONDITION_BUTTON, e -> handleAddCondition());
    Button removeButton = createButton(KEY_REMOVE_CONDITION_BUTTON, e -> handleRemoveCondition());
    HBox buttonRow = createCenteredButtonBox(addButton, removeButton);

    pane.getChildren().addAll(header, conditionComboBox, conditionsListView, buttonRow);
    VBox.setVgrow(conditionsListView, Priority.ALWAYS);
    return pane;
  }

  /**
   * Creates the UI pane for managing outcomes associated with the selected event.
   * Includes a combo box for outcome types, parameter selection, a list view, and add/remove buttons.
   *
   * @return A Node representing the outcomes management UI pane.
   */
  private Node createOutcomesPane() {
    VBox pane = new VBox(DEFAULT_SPACING);
    pane.getStyleClass().add("input-sub-section");

    Label header = createHeaderLabel(KEY_OUTCOMES_HEADER);
    outcomeComboBox = new ComboBox<>(FXCollections.observableArrayList(OutcomeType.values()));
    outcomeComboBox.setPromptText("Select Outcome Type");
    outcomeComboBox.setMaxWidth(Double.MAX_VALUE);
    Node parameterRow = createParameterSelectionRow();
    outcomesListView = createListView(LIST_VIEW_HEIGHT);
    Button addButton = createButton(KEY_ADD_OUTCOME_BUTTON, e -> handleAddOutcome());
    Button removeButton = createButton(KEY_REMOVE_OUTCOME_BUTTON, e -> handleRemoveOutcome());
    HBox buttonRow = createCenteredButtonBox(addButton, removeButton);

    pane.getChildren().addAll(header, outcomeComboBox, parameterRow, outcomesListView, buttonRow);
    VBox.setVgrow(outcomesListView, Priority.ALWAYS);
    return pane;
  }

  /**
   * Creates the UI row for selecting or creating a parameter (Dynamic Variable) for an outcome.
   *
   * @return An HBox containing the parameter label, combo box, and create button.
   */
  private Node createParameterSelectionRow() {
    HBox paramBox = new HBox(DEFAULT_SPACING / 2);
    paramBox.setAlignment(Pos.CENTER_LEFT);
    Label label = new Label(uiBundle.getString(KEY_PARAMETER_LABEL));
    parameterComboBox = new ComboBox<>();
    parameterComboBox.setPromptText("Select Parameter (Optional)");
    parameterComboBox.setMaxWidth(Double.MAX_VALUE);
    Button createButton = createButton(KEY_CREATE_PARAM_BUTTON, e -> openAddDynamicVariableDialog());
    createButton.getStyleClass().add("small-button");

    paramBox.getChildren().addAll(label, parameterComboBox, createButton);
    HBox.setHgrow(parameterComboBox, Priority.ALWAYS);
    return paramBox;
  }

  /**
   * Handles the action of adding a new event. Validates input and delegates
   * the request to the {@link EditorController}.
   */
  private void handleAddEvent() {
    LOG.debug("Add Event button clicked.");
    if (currentObjectId == null) {
      showErrorAlert(KEY_ERROR_SELECTION_NEEDED, "No object selected to add event to.");
      return;
    }
    String eventId = eventIdField.getText().trim();
    if (eventId.isEmpty()) {
      showErrorAlert(KEY_ERROR_INVALID_INPUT_TITLE, "Event ID cannot be empty.");
      return;
    }
    try {
      editorController.addEvent(currentObjectId, eventId);
      eventIdField.clear();
      LOG.info("Delegated add event '{}' for object {}", eventId, currentObjectId);
    } catch (Exception e) {
      LOG.error("Error delegating add event: {}", e.getMessage(), e);
    }
  }

  /**
   * Handles the action of removing the selected event. Validates selection
   * and delegates the request to the {@link EditorController}.
   */
  private void handleRemoveEvent() {
    LOG.debug("Remove Event button clicked.");
    String selectedEvent = eventListView.getSelectionModel().getSelectedItem();
    if (currentObjectId == null || selectedEvent == null) {
      showErrorAlert(KEY_ERROR_SELECTION_NEEDED, "No object or event selected for removal.");
      return;
    }
    try {
      editorController.removeEvent(currentObjectId, selectedEvent);
      LOG.info("Delegated remove event '{}' for object {}", selectedEvent, currentObjectId);
    } catch (Exception e) {
      LOG.error("Error delegating remove event: {}", e.getMessage(), e);
    }
  }

  /**
   * Handles the action of adding a new condition to the selected event.
   * Validates selection and delegates the request to the {@link EditorController}.
   */
  private void handleAddCondition() {
    LOG.debug("Add Condition button clicked.");
    ConditionType selectedCondition = conditionComboBox.getSelectionModel().getSelectedItem();
    if (currentObjectId == null || currentEventId == null || selectedCondition == null) {
      showErrorAlert(KEY_ERROR_SELECTION_NEEDED, "Object, event, and condition type must be selected.");
      return;
    }
    try {
      editorController.addCondition(currentObjectId, currentEventId, selectedCondition);
      LOG.info("Delegated add condition '{}' to event '{}'", selectedCondition, currentEventId);
    } catch (Exception e) {
      LOG.error("Error delegating add condition: {}", e.getMessage(), e);
    }
  }

  /**
   * Handles the action of removing the selected condition from the selected event.
   * Validates selection, parses the condition type, and delegates the request
   * to the {@link EditorController}.
   */
  private void handleRemoveCondition() {
    LOG.debug("Remove Condition button clicked.");
    String selectedConditionStr = conditionsListView.getSelectionModel().getSelectedItem();
    if (currentObjectId == null || currentEventId == null || selectedConditionStr == null) {
      showErrorAlert(KEY_ERROR_SELECTION_NEEDED, "Object, event, and condition must be selected.");
      return;
    }
    try {
      ConditionType condition = ConditionType.valueOf(selectedConditionStr);
      editorController.removeCondition(currentObjectId, currentEventId, condition);
      LOG.info("Delegated remove condition '{}' from event '{}'", condition, currentEventId);
    } catch (IllegalArgumentException e) {
      LOG.error("Failed to parse selected condition string for removal: {}", selectedConditionStr, e);
      showErrorAlert(KEY_ERROR_INVALID_INPUT_TITLE, "Invalid condition selected for removal.");
    } catch (Exception e) {
      LOG.error("Error delegating remove condition: {}", e.getMessage(), e);
    }
  }

  /**
   * Handles the action of adding a new outcome to the selected event.
   * Validates selection (including optional parameter) and delegates the request
   * to the {@link EditorController}.
   */
  private void handleAddOutcome() {
    LOG.debug("Add Outcome button clicked.");
    OutcomeType selectedOutcome = outcomeComboBox.getSelectionModel().getSelectedItem();
    String selectedParameter = parameterComboBox.getValue();
    if (currentObjectId == null || currentEventId == null || selectedOutcome == null) {
      showErrorAlert(KEY_ERROR_SELECTION_NEEDED, "Object, event, and outcome type must be selected.");
      return;
    }
    try {
      editorController.addOutcome(currentObjectId, currentEventId, selectedOutcome, selectedParameter);
      LOG.info("Delegated add outcome '{}' (param: '{}') to event '{}'", selectedOutcome, selectedParameter, currentEventId);
    } catch (Exception e) {
      LOG.error("Error delegating add outcome: {}", e.getMessage(), e);
    }
  }

  /**
   * Handles the action of removing the selected outcome from the selected event.
   * Validates selection, parses the outcome type, and delegates the request
   * to the {@link EditorController}.
   */
  private void handleRemoveOutcome() {
    LOG.debug("Remove Outcome button clicked.");
    String selectedOutcomeStrWithParam = outcomesListView.getSelectionModel().getSelectedItem();
    if (currentObjectId == null || currentEventId == null || selectedOutcomeStrWithParam == null) {
      showErrorAlert(KEY_ERROR_SELECTION_NEEDED, "Object, event, and outcome must be selected.");
      return;
    }
    try {
      String outcomeStr = selectedOutcomeStrWithParam.split(" ")[0];
      OutcomeType outcome = OutcomeType.valueOf(outcomeStr);
      editorController.removeOutcome(currentObjectId, currentEventId, outcome);
      LOG.info("Delegated remove outcome '{}' from event '{}'", outcome, currentEventId);
    } catch (IllegalArgumentException e) {
      LOG.error("Failed to parse selected outcome string for removal: {}", selectedOutcomeStrWithParam, e);
      showErrorAlert(KEY_ERROR_INVALID_INPUT_TITLE, "Invalid outcome selected for removal.");
    } catch (Exception e) {
      LOG.error("Error delegating remove outcome: {}", e.getMessage(), e);
    }
  }

  /**
   * Handles the successful creation of a new dynamic variable from the dialog.
   * Delegates the addition request to the {@link EditorController}.
   *
   * @param dynamicVar The newly created DynamicVariable object.
   */
  private void handleAddDynamicVariable(DynamicVariable dynamicVar) {
    try {
      editorController.addDynamicVariable(dynamicVar);
      LOG.info("Delegated add dynamic variable: {}", dynamicVar.getName());
    } catch (Exception e) {
      LOG.error("Error delegating add dynamic variable: {}", e.getMessage(), e);
    }
  }

  /**
   * Utility method to run a given action on the JavaFX Application Thread.
   * If called from the FX thread, runs immediately; otherwise, uses Platform.runLater().
   *
   * @param action The Runnable action to execute on the FX thread.
   */
  private void runOnFxThread(Runnable action) {
    if (Platform.isFxApplicationThread()) {
      action.run();
    } else {
      Platform.runLater(action);
    }
  }

  /**
   * Refreshes all lists (events, conditions, outcomes) and the parameter combo box
   * based on the currently selected object ID (`currentObjectId`).
   * Ensures the update runs on the JavaFX Application Thread.
   */
  private void refreshAllListsForObject() {
    runOnFxThread(() -> {
      refreshEventsListInternal();
      updateParameterComboBoxInternal();
    });
  }

  /**
   * Internal logic to refresh the events list view based on `currentObjectId`.
   * Also clears the dependent conditions and outcomes lists.
   * Must be called on the JavaFX Application Thread.
   */
  private void refreshEventsListInternal() {
    eventListView.getItems().clear();
    conditionsListView.getItems().clear();
    outcomesListView.getItems().clear();
    this.currentEventId = null;

    if (currentObjectId != null) {
      try {
        Map<String, ?> events = editorController.getEventsForObject(currentObjectId);
        if (events != null && !events.isEmpty()) {
          eventListView.getItems().addAll(events.keySet());
          LOG.debug("Refreshed events list for object {}: {} events.", currentObjectId, events.size());
        } else {
          LOG.debug("No events found for object {}.", currentObjectId);
        }
      } catch (Exception e) {
        LOG.error("Controller failed to get events for object {}: {}", currentObjectId, e.getMessage(), e);
      }
    } else {
      LOG.debug("Events list cleared (no object selected).");
    }
  }

  /**
   * Internal logic to refresh the conditions list view based on `currentObjectId` and `currentEventId`.
   * Must be called on the JavaFX Application Thread.
   */
  private void refreshConditionsListInternal() {
    conditionsListView.getItems().clear();
    if (currentObjectId != null && currentEventId != null) {
      try {
        List<ConditionType> conditions = editorController.getConditionsForEvent(currentObjectId, currentEventId);
        if (conditions != null) {
          conditions.forEach(condition -> conditionsListView.getItems().add(condition.toString()));
          LOG.debug("Refreshed conditions list for event '{}': {} conditions.", currentEventId, conditions.size());
        } else {
          LOG.debug("No conditions found for event '{}'.", currentEventId);
        }
      } catch (Exception e) {
        LOG.error("Controller failed to get conditions for event '{}': {}", currentEventId, e.getMessage(), e);
      }
    } else {
      LOG.trace("Conditions list cleared (no object/event selected).");
    }
  }

  /**
   * Refreshes the conditions list view, ensuring the update occurs on the JavaFX Application Thread.
   */
  private void refreshConditionsList() {
    runOnFxThread(this::refreshConditionsListInternal);
  }

  /**
   * Internal logic to refresh the outcomes list view based on `currentObjectId` and `currentEventId`.
   * Fetches outcome parameters to display them alongside the outcome type.
   * Must be called on the JavaFX Application Thread.
   */
  private void refreshOutcomesListInternal() {
    outcomesListView.getItems().clear();
    if (currentObjectId != null && currentEventId != null) {
      try {
        List<OutcomeType> outcomes = editorController.getOutcomesForEvent(currentObjectId, currentEventId);
        if (outcomes != null) {
          outcomes.forEach(outcome -> {
            String parameter = editorController.getOutcomeParameter(currentObjectId, currentEventId, outcome);
            String displayString = (parameter != null && !parameter.isEmpty())
                ? String.format("%s (%s)", outcome.toString(), parameter)
                : outcome.toString();
            outcomesListView.getItems().add(displayString);
          });
          LOG.debug("Refreshed outcomes list for event '{}': {} outcomes.", currentEventId, outcomes.size());
        } else {
          LOG.debug("No outcomes found for event '{}'.", currentEventId);
        }
      } catch (Exception e) {
        LOG.error("Controller failed to get outcomes for event '{}': {}", currentEventId, e.getMessage(), e);
      }
    } else {
      LOG.trace("Outcomes list cleared (no object/event selected).");
    }
  }

  /**
   * Refreshes the outcomes list view, ensuring the update occurs on the JavaFX Application Thread.
   */
  private void refreshOutcomesList() {
    runOnFxThread(this::refreshOutcomesListInternal);
  }

  /**
   * Internal logic to update the parameter combo box with available dynamic variables.
   * Fetches variables from the controller based on the `currentObjectId`.
   * Must be called on the JavaFX Application Thread.
   */
  private void updateParameterComboBoxInternal() {
    parameterComboBox.getItems().clear();
    try {
      List<DynamicVariable> variables = editorController.getAvailableDynamicVariables(currentObjectId);
      if (variables != null) {
        variables.forEach(var -> parameterComboBox.getItems().add(var.getName()));
        LOG.debug("Updated parameter combo box with {} variables.", variables.size());
      }
      if (!parameterComboBox.getItems().isEmpty()) {
        parameterComboBox.getSelectionModel().selectFirst();
      }
    } catch (Exception e) {
      LOG.error("Controller failed to get available dynamic variables: {}", e.getMessage(), e);
    }
  }

  /**
   * Updates the parameter combo box, ensuring the update occurs on the JavaFX Application Thread.
   */
  private void updateParameterComboBox() {
    runOnFxThread(this::updateParameterComboBoxInternal);
  }


  /**
   * Clears all list views and the event ID text field in the input tab.
   * Resets the `currentEventId` state.
   * Ensures the clearing action runs on the JavaFX Application Thread.
   */
  private void clearAllLists() {
    runOnFxThread(() -> {
      if(eventListView != null) eventListView.getItems().clear();
      if(conditionsListView != null) conditionsListView.getItems().clear();
      if(outcomesListView != null) outcomesListView.getItems().clear();
      if(eventIdField != null) eventIdField.clear();
      this.currentEventId = null;
      LOG.trace("All input tab lists cleared.");
    });
  }

  /**
   * Opens a dialog window for the user to define and add a new dynamic variable.
   * Handles dialog setup, input validation, result conversion, and delegates
   * the addition to {@link #handleAddDynamicVariable(DynamicVariable)} upon success.
   */
  private void openAddDynamicVariableDialog() {
    LOG.debug("Opening 'Add Dynamic Variable' dialog.");
    Dialog<DynamicVariable> dialog = new Dialog<>();
    dialog.setTitle(uiBundle.getString(KEY_DIALOG_ADD_VAR_TITLE));
    try {
      dialog.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource(CSS_PATH)).toExternalForm());
      dialog.getDialogPane().getStyleClass().add("dynamic-variable-dialog");
    } catch (Exception e) {
      LOG.warn("Could not load CSS for dynamic variable dialog: {}", e.getMessage());
    }

    ButtonType addButtonType = new ButtonType(uiBundle.getString(KEY_DIALOG_ADD_BUTTON), ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

    GridPane grid = createDynamicVariableDialogGrid();
    dialog.getDialogPane().setContent(grid);

    TextField nameField = (TextField) grid.lookup("#varNameField");
    ComboBox<String> typeComboBox = (ComboBox<String>) grid.lookup("#varTypeCombo");
    TextField valueField = (TextField) grid.lookup("#varValueField");
    TextField descriptionField = (TextField) grid.lookup("#varDescField");
    Node addButton = dialog.getDialogPane().lookupButton(addButtonType);

    Runnable updateButtonState = () -> {
      boolean disabled = nameField.getText().trim().isEmpty() ||
          typeComboBox.getValue() == null ||
          valueField.getText().trim().isEmpty();
      addButton.setDisable(disabled);
    };
    nameField.textProperty().addListener((obs, ov, nv) -> updateButtonState.run());
    typeComboBox.valueProperty().addListener((obs, ov, nv) -> updateButtonState.run());
    valueField.textProperty().addListener((obs, ov, nv) -> updateButtonState.run());
    updateButtonState.run();

    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == addButtonType) {
        try {
          return new DynamicVariable(
              nameField.getText().trim(),
              typeComboBox.getValue(),
              valueField.getText().trim(),
              descriptionField.getText().trim()
          );
        } catch (IllegalArgumentException ex) {
          LOG.warn("Invalid input provided in dynamic variable dialog: {}", ex.getMessage());
          showErrorAlert(KEY_ERROR_INVALID_INPUT_TITLE, ex.getMessage());
          return null;
        }
      }
      return null;
    });

    Optional<DynamicVariable> result = dialog.showAndWait();
    result.ifPresent(this::handleAddDynamicVariable);
  }

  /**
   * Creates the GridPane layout used within the 'Add Dynamic Variable' dialog.
   *
   * @return A GridPane containing labels and input fields for dynamic variable properties.
   */
  private GridPane createDynamicVariableDialogGrid() {
    GridPane grid = new GridPane();
    grid.setHgap(DEFAULT_SPACING);
    grid.setVgap(DEFAULT_SPACING);
    grid.setPadding(new Insets(SECTION_SPACING));

    TextField nameField = new TextField(); nameField.setId("varNameField");
    nameField.setPromptText(uiBundle.getString(KEY_DIALOG_VAR_NAME)); nameField.setPrefWidth(DIALOG_INPUT_WIDTH);
    ComboBox<String> typeComboBox = new ComboBox<>(FXCollections.observableArrayList("int", "double", "boolean", "string"));
    typeComboBox.setId("varTypeCombo"); typeComboBox.setValue("double"); typeComboBox.setPrefWidth(DIALOG_INPUT_WIDTH);
    TextField valueField = new TextField(); valueField.setId("varValueField");
    valueField.setPromptText(uiBundle.getString(KEY_DIALOG_VAR_VALUE)); valueField.setPrefWidth(DIALOG_INPUT_WIDTH);
    TextField descriptionField = new TextField(); descriptionField.setId("varDescField");
    descriptionField.setPromptText(uiBundle.getString(KEY_DIALOG_VAR_DESC)); descriptionField.setPrefWidth(DIALOG_INPUT_WIDTH);

    grid.add(new Label(uiBundle.getString(KEY_DIALOG_VAR_NAME) + ":"), 0, 0); grid.add(nameField, 1, 0);
    grid.add(new Label(uiBundle.getString(KEY_DIALOG_VAR_TYPE) + ":"), 0, 1); grid.add(typeComboBox, 1, 1);
    grid.add(new Label(uiBundle.getString(KEY_DIALOG_VAR_VALUE) + ":"), 0, 2); grid.add(valueField, 1, 2);
    grid.add(new Label(uiBundle.getString(KEY_DIALOG_VAR_DESC) + ":"), 0, 3); grid.add(descriptionField, 1, 3);
    return grid;
  }

  /**
   * Creates a standard Label used for section headers, styled appropriately.
   *
   * @param bundleKey The key in the resource bundle for the header text.
   * @return A styled Label for use as a section header.
   */
  private Label createHeaderLabel(String bundleKey) {
    Label label = new Label(uiBundle.getString(bundleKey));
    label.getStyleClass().add("section-header");
    return label;
  }

  /**
   * Creates a standard ListView with a predefined preferred height and style class.
   *
   * @param preferredHeight The desired preferred height of the list view.
   * @param <T> The type of items the ListView will hold.
   * @return A configured ListView instance.
   */
  private <T> ListView<T> createListView(double preferredHeight) {
    ListView<T> listView = new ListView<>();
    listView.setPrefHeight(preferredHeight);
    listView.getStyleClass().add("data-list-view");
    return listView;
  }

  /**
   * Creates a standard Button with text from the resource bundle, an action handler,
   * and a style class.
   *
   * @param bundleKey The key in the resource bundle for the button text.
   * @param handler   The event handler for the button's action.
   * @return A configured Button instance.
   */
  private Button createButton(String bundleKey, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
    Button button = new Button(uiBundle.getString(bundleKey));
    button.setOnAction(handler);
    button.getStyleClass().add("action-button");
    return button;
  }

  /**
   * Creates an HBox to hold buttons, centering them horizontally.
   *
   * @param buttons The Buttons to add to the HBox.
   * @return An HBox containing the centered buttons.
   */
  private HBox createCenteredButtonBox(Button... buttons) {
    HBox buttonBox = new HBox(DEFAULT_SPACING);
    buttonBox.setAlignment(Pos.CENTER);
    buttonBox.getChildren().addAll(buttons);
    return buttonBox;
  }

  /**
   * Displays an error Alert dialog with a title and content message.
   * Ensures the Alert is shown on the JavaFX Application Thread.
   *
   * @param titleKey    The resource bundle key for the Alert title.
   * @param contentText The main message content of the Alert.
   */
  private void showErrorAlert(String titleKey, String contentText) {
    runOnFxThread(() -> {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle(uiBundle.getString(titleKey));
      alert.setHeaderText(null);
      alert.setContentText(contentText);
      try {
        alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource(CSS_PATH)).toExternalForm());
      } catch (Exception e) {
        LOG.warn("Could not apply CSS to error alert: {}", e.getMessage());
      }
      alert.showAndWait();
    });
  }

  /**
   * Handles notification that an object was added. Currently logs the event.
   *
   * @param objectId The UUID of the added object.
   */
  @Override
  public void onObjectAdded(UUID objectId) {
    LOG.trace("InputTab received: onObjectAdded {}", objectId);
  }

  /**
   * Handles notification that an object was removed. Currently logs the event.
   * UI clearing is handled by {@link #onSelectionChanged(UUID)} if the removed object was selected.
   *
   * @param objectId The UUID of the removed object.
   */
  @Override
  public void onObjectRemoved(UUID objectId) {
    LOG.trace("InputTab received: onObjectRemoved {}", objectId);
  }

  /**
   * Handles notification that an object's data was updated.
   * If the updated object is the currently selected one, refreshes the UI.
   *
   * @param objectId The UUID of the updated object.
   */
  @Override
  public void onObjectUpdated(UUID objectId) {
    LOG.trace("InputTab received: onObjectUpdated {}", objectId);
    if (Objects.equals(this.currentObjectId, objectId)) {
      LOG.debug("Refreshing InputTab because selected object {} was updated.", objectId);
      refreshAllListsForObject();
    }
  }

  /**
   * Handles notification that the selected object has changed.
   * Updates the internal `currentObjectId` state and refreshes the entire UI
   * to display data for the newly selected object.
   * Ensures UI updates run on the JavaFX Application Thread.
   *
   * @param selectedObjectId The UUID of the newly selected object, or null if selection is cleared.
   */
  @Override
  public void onSelectionChanged(UUID selectedObjectId) {
    runOnFxThread(() -> {
      LOG.debug("InputTab received: onSelectionChanged {}", selectedObjectId);
      if (!Objects.equals(this.currentObjectId, selectedObjectId)) {
        this.currentObjectId = selectedObjectId;
        refreshAllListsForObject();
      }
    });
  }

  /**
   * Handles notification that the global list of dynamic variables has changed.
   * Refreshes the parameter combo box to reflect the updated list.
   * Ensures UI updates run on the JavaFX Application Thread.
   */
  @Override
  public void onDynamicVariablesChanged() {
    runOnFxThread(() -> {
      LOG.debug("InputTab received: onDynamicVariablesChanged");
      updateParameterComboBoxInternal();
    });
  }

  /**
   * Handles notification of an error occurring elsewhere in the application.
   * Displays the error message to the user in an Alert dialog.
   *
   * @param errorMessage The description of the error that occurred.
   */
  @Override
  public void onErrorOccurred(String errorMessage) {
    LOG.warn("InputTab received: onErrorOccurred: {}", errorMessage);
    showErrorAlert(KEY_ERROR_API_FAILURE, errorMessage);
  }
}