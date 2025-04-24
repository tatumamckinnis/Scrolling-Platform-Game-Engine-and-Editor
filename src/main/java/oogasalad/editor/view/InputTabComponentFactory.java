package oogasalad.editor.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.model.data.event_enum.ConditionType;
import oogasalad.editor.model.data.event_enum.OutcomeType;
import oogasalad.editor.model.data.object.DynamicVariable;
import oogasalad.editor.model.data.object.event.EditorEvent;
import oogasalad.editor.model.data.object.event.ExecutorData;
import oogasalad.editor.view.resources.EditorResourceLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Factory responsible for creating the content panel for the "Input" tab.
 * It coordinates various section builders (Events, Conditions, Outcomes) and handles
 * communication between the UI sections and the {@link EditorController}.
 * Implements {@link EditorViewListener} to react to relevant data changes.
 * @author Tatum McKinnis
 */
public class InputTabComponentFactory implements EditorViewListener {

  private static final Logger LOG = LogManager.getLogger(InputTabComponentFactory.class);
  private static final String IDENTIFIERS_PROPERTIES_PATH = "/oogasalad/config/editor/resources/input_tab_component_factory_identifiers.properties";


  private final EditorController editorController;
  private final ResourceBundle uiBundle;
  private final Properties identifierProps;

  private EventsSectionBuilder eventsSectionBuilder;
  private ConditionsSectionBuilder conditionsSectionBuilder;
  private OutcomesSectionBuilder outcomesSectionBuilder;

  private UUID currentObjectId;
  private String currentEventId;

  /**
   * Constructs the factory, initializing dependencies and loading resources.
   * Loads identifiers and the primary UI resource bundle.
   *
   * @param editorController The main controller for editor actions.
   * @throws NullPointerException if editorController is null.
   * @throws RuntimeException     if essential resources (identifiers, UI bundle) cannot be loaded.
   */
  public InputTabComponentFactory(EditorController editorController) {
    this.editorController = Objects.requireNonNull(editorController, "EditorController cannot be null.");
    this.identifierProps = loadIdentifierProperties();

    String uiBundleBaseName = getId("ui.bundle.name");
    try {
      this.uiBundle = EditorResourceLoader.loadResourceBundle(uiBundleBaseName);
    } catch (Exception e) {
      String errorMsgFormat = getId("key.error.bundle.load.fatal");
      String errorMsg = String.format(errorMsgFormat, uiBundleBaseName);
      LOG.fatal(errorMsg, e);
      throw new RuntimeException(String.format(getId("key.error.bundle.load.runtime"), uiBundleBaseName), e);
    }

    createSectionBuilders();
    LOG.info("InputTabComponentFactory initialized.");
  }

  /**
   * Loads the identifier strings (keys, CSS classes, IDs, paths) from the properties file.
   * @return A Properties object containing the loaded identifiers.
   * @throws RuntimeException If the properties file cannot be found or read.
   */
  private Properties loadIdentifierProperties() {
    Properties props = new Properties();
    try (InputStream input = InputTabComponentFactory.class.getResourceAsStream(IDENTIFIERS_PROPERTIES_PATH)) {
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
      LOG.error("Missing identifier in InputTab properties file for key: {}", key);
      throw new RuntimeException("Missing identifier in InputTab properties file for key: " + key);
    }
    return value;
  }

  /**
   * {@inheritDoc}
   * Logs prefab changes; no direct UI update needed in this factory itself.
   */
  @Override
  public void onPrefabsChanged() {
    LOG.debug("InputTabComponentFactory notified of prefab changes.");
  }

  /**
   * Called when a sprite template is changed
   */
  @Override
  public void onSpriteTemplateChanged() {
    LOG.debug("InputTabComponentFactory notified of sprite template");
  }

  /**
   * Initializes the builders for the Events, Conditions, and Outcomes UI sections,
   * passing them necessary resources and handler method references.
   */
  private void createSectionBuilders() {
    eventsSectionBuilder = new EventsSectionBuilder(
        uiBundle,
        this::handleAddEvent,
        this::handleRemoveEvent,
        this::handleEventSelectionChange
    );

    conditionsSectionBuilder = new ConditionsSectionBuilder(
        uiBundle,
        this::getConditionTypeNames,
        this::handleAddConditionGroup,
        this::handleRemoveConditionGroup,
        this::handleAddCondition,
        this::handleRemoveCondition,
        this::handleEditConditionParam
    );

    outcomesSectionBuilder = new OutcomesSectionBuilder(
        uiBundle,
        this::getOutcomeTypeNames,
        this::getDynamicVariablesForObject,
        this::handleAddOutcome,
        this::handleRemoveOutcome,
        this::openAddDynamicVariableDialog,
        this::handleEditOutcomeParam
    );
  }

  /**
   * Creates the main content Pane for the Input Tab.
   * This involves building each section (Events, Conditions, Outcomes) using their respective
   * builders and arranging them vertically within a ScrollPane anchored to fill its parent.
   * Layout (padding, spacing) is controlled via CSS.
   *
   * @return A Pane containing the fully assembled Input Tab UI.
   */
  public Pane createInputTabPanel() {
    VBox contentVBox = new VBox();
    contentVBox.setId(getId("id.content.vbox"));

    Node eventsSection = eventsSectionBuilder.build();
    Node conditionsSection = conditionsSectionBuilder.build();
    Node outcomesSection = outcomesSectionBuilder.build();

    contentVBox.getChildren().addAll(eventsSection, conditionsSection, outcomesSection);

    ScrollPane scrollPane = new ScrollPane(contentVBox);
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(false);
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    scrollPane.setId(getId("id.scroll.pane"));

    AnchorPane rootPane = new AnchorPane();
    rootPane.getChildren().add(scrollPane);
    AnchorPane.setTopAnchor(scrollPane, 0.0);
    AnchorPane.setBottomAnchor(scrollPane, 0.0);
    AnchorPane.setLeftAnchor(scrollPane, 0.0);
    AnchorPane.setRightAnchor(scrollPane, 0.0);

    clearAllUIToDefault();
    LOG.debug("Input tab panel created using AnchorPane -> ScrollPane -> VBox.");

    return rootPane;
  }

  /**
   * Handles the change event when a different event is selected in the events list view.
   * Updates the internal state and triggers a refresh of the conditions and outcomes sections.
   *
   * @param selectedEventId The ID of the newly selected event, or null if deselected.
   */
  private void handleEventSelectionChange(String selectedEventId) {
    runOnFxThread(() -> {
      this.currentEventId = selectedEventId;
      LOG.debug("Internal state: Event selection changed to: {}", currentEventId);
      refreshConditionsAndOutcomesForEvent();
    });
  }

  /**
   * Handles the request to add a new event. Validates input and delegates to the controller.
   * Refreshes the events list upon successful addition. Shows an error alert on failure.
   *
   * @param eventId The ID of the event to add, taken from the input field.
   */
  private void handleAddEvent(String eventId) {
    LOG.debug("Add Event action triggered for ID: {}", eventId);
    if (!isSelected(true, false)) {
      return;
    }
    try {
      editorController.addEvent(currentObjectId, eventId);
      LOG.info("Delegated add event '{}' for object {}", eventId, currentObjectId);
      refreshEventsList();
    } catch (Exception e) {
      LOG.error("Error delegating add event: {}", e.getMessage(), e);
      showFormattedErrorAlert(getId("key.error.api.failure"), getId("key.error.action.failed"),
          getId("action.add.event"), e.getMessage());
    }
  }


  /**
   * Handles the request to remove the currently selected event.
   * Validates selection and delegates to the controller. Refreshes the list on success.
   * Shows an error alert on failure or if selection is invalid.
   */
  private void handleRemoveEvent() {
    LOG.debug("Remove Event action triggered.");
    String selectedEvent = eventsSectionBuilder.getEventListView().getSelectionModel().getSelectedItem();
    if (!isSelected(true, false) || selectedEvent == null) {
      showErrorAlert(getId("key.error.selection.needed"), uiBundle.getString(getId("key.error.event.remove.selection")));
      return;
    }
    try {
      editorController.removeEvent(currentObjectId, selectedEvent);
      LOG.info("Delegated remove event '{}' for object {}", selectedEvent, currentObjectId);
      refreshEventsList();
    } catch (Exception e) {
      LOG.error("Error delegating remove event: {}", e.getMessage(), e);
      showFormattedErrorAlert(getId("key.error.api.failure"), getId("key.error.action.failed"),
          getId("action.remove.event"), e.getMessage());
    }
  }

  /**
   * Handles the request to add a new condition group to the currently selected event.
   * Validates selection and delegates to the controller. Refreshes UI on success.
   */
  private void handleAddConditionGroup() {
    LOG.debug("Add Condition Group action triggered.");
    if (!isSelected(true, true)) {
      return;
    }
    try {
      editorController.addConditionGroup(currentObjectId, currentEventId);
      refreshConditionsAndOutcomesForEvent();
    } catch (Exception e) {
      LOG.error("Error delegating add condition group: {}", e.getMessage(), e);
      showFormattedErrorAlert(getId("key.error.api.failure"), getId("key.error.action.failed"),
          getId("action.add.condition.group"), e.getMessage());
    }
  }


  /**
   * Handles the request to remove a condition group from the currently selected event.
   * Validates selection and delegates to the controller. Refreshes UI on success.
   *
   * @param groupIndex The index of the condition group to remove.
   */
  private void handleRemoveConditionGroup(int groupIndex) {
    LOG.debug("Remove Condition Group action triggered for group index: {}", groupIndex);
    if (!isSelected(true, true)) {
      return;
    }
    try {
      editorController.removeConditionGroup(currentObjectId, currentEventId, groupIndex);
      refreshConditionsAndOutcomesForEvent();
    } catch (Exception e) {
      LOG.error("Error delegating remove condition group: {}", e.getMessage(), e);
      showFormattedErrorAlert(getId("key.error.api.failure"), getId("key.error.action.failed"),
          getId("action.remove.condition.group"), e.getMessage());
    }
  }

  /**
   * Handles the request to add a condition of a specific type to a specific group within the current event.
   * Validates selection and delegates to the controller. Refreshes UI on success.
   *
   * @param groupIndex    The index of the group to add the condition to.
   * @param conditionType The string identifier of the condition type to add.
   */
  private void handleAddCondition(int groupIndex, String conditionType) {
    LOG.debug("Add Condition action triggered for type: {} in group {}", conditionType, groupIndex);
    if (!isSelected(true, true)) {
      return;
    }
    try {
      editorController.addEventCondition(currentObjectId, currentEventId, groupIndex, conditionType);
      refreshConditionsAndOutcomesForEvent();
    } catch (Exception e) {
      LOG.error("Error delegating add condition: {}", e.getMessage(), e);
      showFormattedErrorAlert(getId("key.error.api.failure"), getId("key.error.action.failed"),
          getId("action.add.condition"), e.getMessage());
    }
  }

  /**
   * Handles the request to remove a specific condition from a group within the current event.
   * Validates selection and delegates to the controller. Refreshes UI on success.
   *
   * @param groupIndex     The index of the group containing the condition.
   * @param conditionIndex The index of the condition within the group to remove.
   */
  private void handleRemoveCondition(int groupIndex, int conditionIndex) {
    LOG.debug("Remove Condition action triggered for group {}, index {}", groupIndex, conditionIndex);
    if (!isSelected(true, true)) {
      return;
    }
    try {
      editorController.removeEventCondition(currentObjectId, currentEventId, groupIndex, conditionIndex);
      refreshConditionsAndOutcomesForEvent();
    } catch (Exception e) {
      LOG.error("Error delegating remove condition: {}", e.getMessage(), e);
      showFormattedErrorAlert(getId("key.error.api.failure"), getId("key.error.action.failed"),
          getId("action.remove.condition"), e.getMessage());
    }
  }

  /**
   * Handles the request to edit a parameter of a specific condition.
   * Delegates the parameter update to the appropriate controller method based on value type.
   *
   * @param groupIndex     The index of the group containing the condition.
   * @param conditionIndex The index of the condition within the group.
   * @param paramName      The name of the parameter to edit.
   * @param value          The new value for the parameter (expected String or Double).
   */
  private void handleEditConditionParam(int groupIndex, int conditionIndex, String paramName, Object value) {
    if (!isSelected(true, true)) {
      return;
    }
    handleEditParam(paramName, value,
        (strVal) -> editorController.setEventConditionStringParameter(currentObjectId,
            currentEventId, groupIndex, conditionIndex, paramName, strVal),
        (dblVal) -> editorController.setEventConditionDoubleParameter(currentObjectId,
            currentEventId, groupIndex, conditionIndex, paramName, dblVal),
        getId("action.edit.condition.param")
    );
  }

  /**
   * Handles the request to add an outcome of a specific type to the current event.
   * Validates selection and delegates to the controller. Refreshes UI on success.
   *
   * @param outcomeType The string identifier of the outcome type to add.
   */
  private void handleAddOutcome(String outcomeType) {
    LOG.debug("Add Outcome action triggered for type: {}", outcomeType);
    if (!isSelected(true, true)) {
      return;
    }
    try {
      editorController.addEventOutcome(currentObjectId, currentEventId, outcomeType);
      refreshConditionsAndOutcomesForEvent();
    } catch (Exception e) {
      LOG.error("Error delegating add outcome: {}", e.getMessage(), e);
      showFormattedErrorAlert(getId("key.error.api.failure"), getId("key.error.action.failed"),
          getId("action.add.outcome"), e.getMessage());
    }
  }

  /**
   * Handles the request to remove a specific outcome from the current event.
   * Validates selection and delegates to the controller. Refreshes UI on success.
   *
   * @param outcomeIndex The index of the outcome to remove.
   */
  private void handleRemoveOutcome(int outcomeIndex) {
    LOG.debug("Remove Outcome action triggered for index {}", outcomeIndex);
    if (!isSelected(true, true)) {
      return;
    }
    try {
      editorController.removeEventOutcome(currentObjectId, currentEventId, outcomeIndex);
      refreshConditionsAndOutcomesForEvent();
    } catch (Exception e) {
      LOG.error("Error delegating remove outcome: {}", e.getMessage(), e);
      showFormattedErrorAlert(getId("key.error.api.failure"), getId("key.error.action.failed"),
          getId("action.remove.outcome"), e.getMessage());
    }
  }

  /**
   * Handles the request to edit a parameter of a specific outcome.
   * Delegates the parameter update to the appropriate controller method based on value type.
   *
   * @param outcomeIndex The index of the outcome.
   * @param paramName    The name of the parameter to edit.
   * @param value        The new value for the parameter (expected String or Double).
   */
  private void handleEditOutcomeParam(int outcomeIndex, String paramName, Object value) {
    if (!isSelected(true, true)) {
      return;
    }
    handleEditParam(paramName, value,
        (strVal) -> editorController.setEventOutcomeStringParameter(currentObjectId,
            currentEventId, outcomeIndex, paramName, strVal),
        (dblVal) -> editorController.setEventOutcomeDoubleParameter(currentObjectId,
            currentEventId, outcomeIndex, paramName, dblVal),
        getId("action.edit.outcome.param")
    );
  }

  /**
   * Generic helper method to handle editing either a condition or outcome parameter.
   * Determines the type of the value and calls the appropriate setter lambda.
   * Includes error handling and UI refresh logic.
   *
   * @param paramName    The name of the parameter being edited.
   * @param value        The new value (expected String or Double).
   * @param stringSetter Lambda function to call if the value is a String.
   * @param doubleSetter Lambda function to call if the value is a Double.
   * @param contextKey   The identifier key for the action description (e.g., "edit condition parameter").
   */
  private void handleEditParam(String paramName, Object value,
      Consumer<String> stringSetter, Consumer<Double> doubleSetter, String contextKey) {
    String contextDescription = uiBundle.getString(contextKey);
    LOG.trace("Edit Param: Context='{}', Param='{}', Value='{}'", contextDescription, paramName, value);
    try {
      if (value instanceof String strValue) {
        stringSetter.accept(strValue);
      } else if (value instanceof Double doubleValue) {
        doubleSetter.accept(doubleValue);
      } else {
        String typeName = (value == null) ? "null" : value.getClass().getName();
        LOG.warn(String.format(uiBundle.getString(getId("key.error.unsupported.param.type")), contextDescription, typeName));
      }
    } catch (Exception e) {
      LOG.error("Error delegating edit parameter ({}): {}", contextDescription, e.getMessage(), e);
      showFormattedErrorAlert(getId("key.error.api.failure"), getId("key.error.action.failed"),
          contextKey, e.getMessage());
      refreshConditionsAndOutcomesForEvent();
    }
  }

  /**
   * Opens the dialog for adding a new dynamic variable.
   * Validates that an object is selected before opening.
   * Handles the result from the dialog if a variable is successfully created.
   */
  private void openAddDynamicVariableDialog() {
    LOG.debug("Opening 'Add Dynamic Variable' dialog.");
    if (!isSelected(true, false)) {
      return;
    }
    DynamicVariableDialog dialog = new DynamicVariableDialog(uiBundle);
    Optional<DynamicVariable> result = dialog.showAndWait();
    result.ifPresent(this::handleAddDynamicVariable);
  }

  /**
   * Handles the addition of a new dynamic variable received from the dialog.
   * Delegates the addition to the controller and handles potential errors.
   *
   * @param dynamicVar The DynamicVariable object created by the dialog.
   */
  private void handleAddDynamicVariable(DynamicVariable dynamicVar) {
    try {
      if (dynamicVar == null) {
        LOG.warn(uiBundle.getString(getId("key.warn.add.null.variable")));
        return;
      }
      editorController.addDynamicVariable(dynamicVar);
      LOG.info("Delegated add dynamic variable: {}", dynamicVar.getName());
    } catch (Exception e) {
      LOG.error("Error delegating add dynamic variable: {}", e.getMessage(), e);
      showFormattedErrorAlert(getId("key.error.api.failure"), getId("key.error.action.failed"),
          getId("action.add.variable"), e.getMessage());
    }
  }

  /**
   * Retrieves the names of available condition types (enums).
   * Used to populate the condition type selection ComboBox.
   *
   * @return A sorted list of condition type names.
   */
  private List<String> getConditionTypeNames() {
    return Stream.of(ConditionType.values()).map(Enum::name).sorted().collect(Collectors.toList());
  }

  /**
   * Retrieves the names of available outcome types (enums).
   * Used to populate the outcome type selection ComboBox.
   *
   * @return A sorted list of outcome type names.
   */
  private List<String> getOutcomeTypeNames() {
    return Stream.of(OutcomeType.values()).map(Enum::name).sorted().collect(Collectors.toList());
  }

  /**
   * Retrieves the list of dynamic variables available for the currently selected object context.
   * Handles potential errors during retrieval. Used by the Outcomes section builder.
   *
   * @return A list of available DynamicVariables, or an empty list if none or on error.
   */
  private List<DynamicVariable> getDynamicVariablesForObject() {
    if (currentObjectId == null) {
      return Collections.emptyList();
    }
    try {
      return editorController.getAvailableDynamicVariables(currentObjectId);
    } catch (Exception e) {
      LOG.error("Failed to get dynamic variables for object {}: {}", currentObjectId, e.getMessage(), e);
      return Collections.emptyList();
    }
  }

  /**
   * Helper method to ensure UI updates are performed on the JavaFX Application Thread.
   *
   * @param action The Runnable containing UI update code.
   */
  private void runOnFxThread(Runnable action) {
    if (Platform.isFxApplicationThread()) {
      action.run();
    } else {
      Platform.runLater(action);
    }
  }

  /**
   * Refreshes the entire UI related to the currently selected object,
   * specifically the events list and the dynamic variable list used by outcomes.
   */
  private void refreshUIForObject() {
    runOnFxThread(() -> {
      LOG.debug("Refreshing UI for object: {}", currentObjectId);
      refreshEventsList();
      refreshDynamicVariables();
    });
  }

  /**
   * Refreshes the list of events displayed in the Events section for the currently selected object.
   * Fetches data from the controller and updates the ListView. Preserves selection if possible.
   * Clears conditions/outcomes if the selected event no longer exists or if no event is selected.
   */
  void refreshEventsList() {
    runOnFxThread(() -> {
      String previouslySelectedEvent = currentEventId;
      Map<String, EditorEvent> events = fetchEventsForCurrentObject();

      if (events == null) {
        eventsSectionBuilder.getEventListView().getItems().clear();
        clearConditionsAndOutcomesUI();
        return;
      }

      List<String> sortedEventIds = events.keySet().stream().sorted().collect(Collectors.toList());
      eventsSectionBuilder.getEventListView().getItems().setAll(sortedEventIds);

      if (previouslySelectedEvent != null && sortedEventIds.contains(previouslySelectedEvent)) {
        eventsSectionBuilder.getEventListView().getSelectionModel().select(previouslySelectedEvent);
      } else {
        clearConditionsAndOutcomesUI();
      }
      LOG.debug("Refreshed events list for object {}: {} events.", currentObjectId, sortedEventIds.size());
    });
  }

  /**
   * Fetches the map of events associated with the currently selected object ID from the controller.
   * Handles potential errors during fetching.
   *
   * @return A Map of event IDs to EditorEvent objects, or null if an error occurs, or empty map if no object selected.
   */
  private Map<String, EditorEvent> fetchEventsForCurrentObject() {
    if (currentObjectId == null) {
      return Collections.emptyMap();
    }
    try {
      return editorController.getEventsForObject(currentObjectId);
    } catch (Exception e) {
      LOG.error("Controller failed to get events for object {}: {}", currentObjectId, e.getMessage(), e);
      showFormattedErrorAlert(getId("key.error.api.failure"), getId("key.error.action.failed"),
          getId("action.fetch.events"), e.getMessage());
      return null;
    }
  }

  /**
   * Refreshes the Conditions and Outcomes sections based on the currently selected event.
   * Fetches the relevant data from the controller and updates the respective section builders.
   */
  void refreshConditionsAndOutcomesForEvent() {
    runOnFxThread(() -> {
      LOG.debug("Refreshing conditions and outcomes for event: {}", currentEventId);
      List<List<ExecutorData>> conditions = fetchConditionsForCurrentEvent();
      List<ExecutorData> outcomes = fetchOutcomesForCurrentEvent();

      conditionsSectionBuilder.updateConditionsListView(conditions);
      outcomesSectionBuilder.updateOutcomesListView(outcomes);
    });
  }

  /**
   * Fetches the list of condition groups (each group is a list of conditions) for the current event.
   * Handles cases where no object or event is selected, and errors during fetching.
   *
   * @return A List of Lists of ExecutorData representing condition groups, or null if no event/object selected or error.
   */
  private List<List<ExecutorData>> fetchConditionsForCurrentEvent() {
    if (currentObjectId == null || currentEventId == null) {
      return null;
    }
    try {
      return editorController.getEventConditions(currentObjectId, currentEventId);
    } catch (Exception e) {
      LOG.error("Controller failed to get conditions for event {}: {}", currentEventId, e.getMessage(), e);
      showFormattedErrorAlert(getId("key.error.api.failure"), getId("key.error.action.failed"),
          getId("action.fetch.conditions"), e.getMessage());
      return null;
    }
  }

  /**
   * Fetches the list of outcomes for the current event.
   * Handles cases where no object or event is selected, and errors during fetching.
   *
   * @return A List of ExecutorData representing outcomes, or null if no event/object selected or error.
   */
  private List<ExecutorData> fetchOutcomesForCurrentEvent() {
    if (currentObjectId == null || currentEventId == null) {
      return null;
    }
    try {
      return editorController.getEventOutcomes(currentObjectId, currentEventId);
    } catch (Exception e) {
      LOG.error("Controller failed to get outcomes for event {}: {}", currentEventId, e.getMessage(), e);
      showFormattedErrorAlert(getId("key.error.api.failure"), getId("key.error.action.failed"),
          getId("action.fetch.outcomes"), e.getMessage());
      return null;
    }
  }

  /**
   * Refreshes the list of dynamic variables available in the Outcomes section's combo box.
   * Ensures this runs on the FX thread.
   */
  private void refreshDynamicVariables() {
    runOnFxThread(() -> {
      LOG.debug("Refreshing dynamic variables UI for object context: {}", currentObjectId);
      outcomesSectionBuilder.updateDynamicVariableComboBox();
    });
  }

  /**
   * Clears the UI displays for conditions and outcomes. Typically called when the selected event changes
   * or is deselected. Ensures this runs on the FX thread.
   */
  private void clearConditionsAndOutcomesUI() {
    runOnFxThread(() -> {
      this.currentEventId = null;
      conditionsSectionBuilder.updateConditionsListView(null);
      outcomesSectionBuilder.updateOutcomesListView(null);
      LOG.trace("Cleared conditions and outcomes UI sections.");
    });
  }

  /**
   * Resets the entire Input Tab UI to its default (empty/deselected) state.
   * Clears event list, event input field, conditions, and outcomes.
   * Ensures this runs on the FX thread.
   */
  private void clearAllUIToDefault() {
    runOnFxThread(() -> {
      eventsSectionBuilder.getEventListView().getItems().clear();
      eventsSectionBuilder.getEventIdField().clear();
      clearConditionsAndOutcomesUI();
      refreshDynamicVariables();
      LOG.trace("Cleared all input tab UI to default state.");
    });
  }

  /**
   * Checks if the required selections (object and/or event) are currently active.
   * If a required selection is missing, logs a warning and shows an error alert.
   *
   * @param requireObjectSelection If true, checks if {@code currentObjectId} is not null.
   * @param requireEventSelection  If true, checks if {@code currentEventId} is not null.
   * @return {@code true} if all required selections are present, {@code false} otherwise.
   */
  private boolean isSelected(boolean requireObjectSelection, boolean requireEventSelection) {
    String errorKey = null;
    if (requireObjectSelection && currentObjectId == null) {
      errorKey = getId("key.error.object.selection.required");
    } else if (requireEventSelection && currentEventId == null) {
      errorKey = getId("key.error.event.selection.required");
    }

    if (errorKey != null) {
      String errorMessage = uiBundle.getString(errorKey);
      LOG.warn("Action requires selection: {}", errorMessage);
      showErrorAlert(getId("key.error.selection.needed"), errorMessage);
      return false;
    }
    return true;
  }

  /**
   * Displays a standard error alert dialog using localized text for the title.
   *
   * @param titleKey    The key (from identifiers file) for the alert title text in the uiBundle.
   * @param contentText The main error message content to display.
   */
  private void showErrorAlert(String titleKey, String contentText) {
    runOnFxThread(() -> {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle(uiBundle.getString(titleKey));
      alert.setHeaderText(null);
      alert.setContentText(contentText);
      String cssPath = getId("css.path");
      try {
        String cssUrl = Objects.requireNonNull(getClass().getResource(cssPath)).toExternalForm();
        alert.getDialogPane().getStylesheets().add(cssUrl);
      } catch (Exception e) {
        LOG.warn(String.format(uiBundle.getString(getId("key.error.css.apply.failed")), cssPath, e.getMessage()));
      }
      alert.showAndWait();
    });
  }

  /**
   * Displays a standard error alert dialog using localized text for the title
   * and a formatted message for the content.
   *
   * @param titleKey       The key (from identifiers file) for the alert title text.
   * @param formatKey      The key (from identifiers file) for the error message format string (e.g., "Failed to %s: %s").
   * @param actionKey      The key (from identifiers file) for the description of the action that failed.
   * @param exceptionMsg   The message obtained from the caught exception.
   */
  private void showFormattedErrorAlert(String titleKey, String formatKey, String actionKey, String exceptionMsg) {
    String title = uiBundle.getString(titleKey);
    String format = uiBundle.getString(formatKey);
    String action = uiBundle.getString(actionKey);
    String contentText = String.format(format, action, exceptionMsg);
    showErrorAlert(titleKey, contentText);
  }


  /**
   * {@inheritDoc}
   * Refreshes the dynamic variable list when an object is added.
   */
  @Override
  public void onObjectAdded(UUID objectId) {
    LOG.trace("InputTab received: onObjectAdded {}", objectId);
    refreshDynamicVariables();
  }

  /**
   * {@inheritDoc}
   * Clears the entire UI if the removed object was the one selected.
   * Refreshes the dynamic variable list regardless. Ensures execution on the FX thread.
   */
  @Override
  public void onObjectRemoved(UUID objectId) {
    runOnFxThread(() -> {
      LOG.trace("InputTab received: onObjectRemoved {}", objectId);
      if (Objects.equals(this.currentObjectId, objectId)) {
        LOG.debug("Selected object {} was removed. Clearing input tab.", objectId);
        this.currentObjectId = null;
        clearAllUIToDefault();
      }
      refreshDynamicVariables();
    });
  }

  /**
   * {@inheritDoc}
   * Refreshes the events list if the updated object is the one currently selected.
   * Refreshes the dynamic variable list regardless. Ensures execution on the FX thread.
   */
  @Override
  public void onObjectUpdated(UUID objectId) {
    runOnFxThread(() -> {
      LOG.trace("InputTab received: onObjectUpdated {}", objectId);
      if (Objects.equals(this.currentObjectId, objectId)) {
        LOG.debug("Refreshing InputTab because selected object {} was updated.", objectId);
        refreshEventsList();
      }
      refreshDynamicVariables();
    });
  }

  /**
   * {@inheritDoc}
   * Updates the current object ID and refreshes the entire UI if the selection differs.
   * Clears the UI if deselected. Refreshes dynamic variables. Ensures execution on the FX thread.
   */
  @Override
  public void onSelectionChanged(UUID selectedObjectId) {
    runOnFxThread(() -> {
      LOG.debug("InputTab received: onSelectionChanged {}", selectedObjectId);
      if (!Objects.equals(this.currentObjectId, selectedObjectId)) {
        this.currentObjectId = selectedObjectId;
        this.currentEventId = null;
        if (this.currentObjectId != null) {
          refreshUIForObject();
        } else {
          clearAllUIToDefault();
        }
      }
      refreshDynamicVariables();
    });
  }

  /**
   * {@inheritDoc}
   * Refreshes the dynamic variable list display. Ensures execution on the FX thread.
   */
  @Override
  public void onDynamicVariablesChanged() {
    runOnFxThread(() -> {
      LOG.debug("InputTab received: onDynamicVariablesChanged");
      refreshDynamicVariables();
    });
  }

  /**
   * {@inheritDoc}
   * Shows an error alert dialog with the provided message.
   */
  @Override
  public void onErrorOccurred(String errorMessage) {
    LOG.warn("InputTab received: onErrorOccurred: {}", errorMessage);
    showErrorAlert(getId("key.error.api.failure"), errorMessage);
  }
}