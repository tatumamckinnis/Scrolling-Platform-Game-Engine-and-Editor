package oogasalad.editor.view;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.application.Platform;
import javafx.geometry.Insets;
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
 * Factory for the "Input" tab UI. Coordinates section builders and handles
 * communication with the controller and UI updates based on model changes.
 * Implements {@link EditorViewListener} to react to changes in the editor model.
 *
 * @author Tatum McKinnis
 */
public class InputTabComponentFactory implements EditorViewListener {

  private static final Logger LOG = LogManager.getLogger(InputTabComponentFactory.class);

  private static final String UI_BUNDLE_NAME = "InputTabUI";
  private static final String CSS_PATH = "/css/editorStyles.css";
  private static final String KEY_ERROR_SELECTION_NEEDED = "errorSelectionNeeded";
  private static final String KEY_ERROR_API_FAILURE = "errorApiFailureTitle";

  private static final double DEFAULT_PADDING = 12.0;
  private static final double SECTION_SPACING = 25.0;

  private final EditorController editorController;
  private final ResourceBundle uiBundle;

  private EventsSectionBuilder eventsSectionBuilder;
  private ConditionsSectionBuilder conditionsSectionBuilder;
  private OutcomesSectionBuilder outcomesSectionBuilder;

  private ListView<String> eventListView;
  private ListView<String> conditionsListView;
  private ListView<String> outcomesListView;
  private TextField eventIdField;
  private ComboBox<String> parameterComboBox;

  private UUID currentObjectId;
  private String currentEventId;

  /**
   * Constructs an InputTabComponentFactory.
   *
   * @param editorController The controller responsible for handling editor logic. Must not be null.
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
    createSectionBuilders();
    LOG.info("InputTabComponentFactory initialized.");
  }

  /**
   * Initializes the section builder instances (Events, Conditions, Outcomes)
   * required for constructing the UI components.
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
        this::handleAddCondition,
        this::handleRemoveCondition
    );
    outcomesSectionBuilder = new OutcomesSectionBuilder(
        uiBundle,
        this::handleAddOutcome,
        this::handleRemoveOutcome,
        this::openAddDynamicVariableDialog
    );
  }


  /**
   * Creates the main content pane for the Input tab.
   * This pane includes sections for managing events, conditions, and outcomes,
   * arranged vertically within a scrollable container.
   *
   * @return An AnchorPane containing the scrollable, vertically stacked UI components for the Input tab.
   */
  public Pane createInputTabPanel() {
    VBox contentVBox = new VBox(SECTION_SPACING);
    contentVBox.setPadding(new Insets(DEFAULT_PADDING));
    contentVBox.setId("input-tab-content-vbox");

    Node eventsSection = eventsSectionBuilder.build();
    Node conditionsPane = conditionsSectionBuilder.build();
    Node outcomesPane = outcomesSectionBuilder.build();

    this.eventListView = eventsSectionBuilder.getEventListView();
    this.eventIdField = eventsSectionBuilder.getEventIdField();
    this.conditionsListView = conditionsSectionBuilder.getConditionsListView();
    this.outcomesListView = outcomesSectionBuilder.getOutcomesListView();
    this.parameterComboBox = outcomesSectionBuilder.getParameterComboBox();

    contentVBox.getChildren().addAll(eventsSection, conditionsPane, outcomesPane);

    ScrollPane scrollPane = new ScrollPane(contentVBox);
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(false);
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    scrollPane.setId("input-tab-scroll-pane");

    AnchorPane rootPane = new AnchorPane();
    rootPane.getChildren().add(scrollPane);

    AnchorPane.setTopAnchor(scrollPane, 0.0);
    AnchorPane.setBottomAnchor(scrollPane, 0.0);
    AnchorPane.setLeftAnchor(scrollPane, 0.0);
    AnchorPane.setRightAnchor(scrollPane, 0.0);

    clearAllLists();
    LOG.debug("Input tab panel created using AnchorPane -> ScrollPane -> VBox.");

    return rootPane;
  }


  /**
   * Handles the selection change event in the events list view.
   * Updates the internal state (`currentEventId`) and refreshes the
   * conditions and outcomes lists based on the newly selected event.
   *
   * @param selectedEvent The ID of the newly selected event, or null if deselected.
   */
  private void handleEventSelectionChange(String selectedEvent) {
    this.currentEventId = selectedEvent;
    LOG.debug("Internal state: Event selection changed to: {}", currentEventId);
    refreshConditionsList();
    refreshOutcomesList();
  }

  /**
   * Handles the action to add a new event to the currently selected object.
   * Delegates the action to the {@link EditorController}. Shows an error if no object is selected
   * or if the controller reports an error. Clears the event ID input field on success.
   *
   * @param eventId The ID of the event to add.
   */
  private void handleAddEvent(String eventId) {
    LOG.debug("Add Event action triggered for ID: {}", eventId);
    if (currentObjectId == null) {
      showErrorAlert(KEY_ERROR_SELECTION_NEEDED, "No object selected to add event to.");
      return;
    }
    try {
      editorController.addEvent(currentObjectId, eventId);
      eventIdField.clear();
      LOG.info("Delegated add event '{}' for object {}", eventId, currentObjectId);
    } catch (Exception e) {
      LOG.error("Error delegating add event: {}", e.getMessage(), e);
      showErrorAlert(KEY_ERROR_API_FAILURE, "Failed to add event: " + e.getMessage());
    }
  }

  /**
   * Handles the action to remove the selected event from the currently selected object.
   * Delegates the action to the {@link EditorController}. Shows an error if no object or
   * event is selected, or if the controller reports an error.
   */
  private void handleRemoveEvent() {
    LOG.debug("Remove Event action triggered.");
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
      showErrorAlert(KEY_ERROR_API_FAILURE, "Failed to remove event: " + e.getMessage());
    }
  }

  /**
   * Handles the action to add a new condition to the currently selected event.
   * Delegates the action to the {@link EditorController}. Shows an error if no object or
   * event is selected, or if the controller reports an error.
   *
   * @param conditionType The type of condition to add.
   */
  private void handleAddCondition(ConditionType conditionType) {
    LOG.debug("Add Condition action triggered for type: {}", conditionType);
    if (currentObjectId == null || currentEventId == null) {
      showErrorAlert(KEY_ERROR_SELECTION_NEEDED, "Object and event must be selected.");
      return;
    }
    try {
      editorController.addCondition(currentObjectId, currentEventId, conditionType);
      LOG.info("Delegated add condition '{}' to event '{}'", conditionType, currentEventId);
    } catch (Exception e) {
      LOG.error("Error delegating add condition: {}", e.getMessage(), e);
      showErrorAlert(KEY_ERROR_API_FAILURE, "Failed to add condition: " + e.getMessage());
    }
  }

  /**
   * Handles the action to remove the selected condition from the currently selected event.
   * Delegates the action to the {@link EditorController}. Shows an error if no object, event,
   * or condition is selected, or if the controller reports an error.
   *
   * @param conditionType The type of condition to remove.
   */
  private void handleRemoveCondition(ConditionType conditionType) {
    LOG.debug("Remove Condition action triggered for type: {}", conditionType);
    if (currentObjectId == null || currentEventId == null) {
      showErrorAlert(KEY_ERROR_SELECTION_NEEDED, "Object, event, and condition must be selected.");
      return;
    }
    try {
      editorController.removeCondition(currentObjectId, currentEventId, conditionType);
      LOG.info("Delegated remove condition '{}' from event '{}'", conditionType, currentEventId);
    } catch (Exception e) {
      LOG.error("Error delegating remove condition: {}", e.getMessage(), e);
      showErrorAlert(KEY_ERROR_API_FAILURE, "Failed to remove condition: " + e.getMessage());
    }
  }

  /**
   * Handles the action to add a new outcome to the currently selected event.
   * Delegates the action to the {@link EditorController}. Shows an error if no object or
   * event is selected, or if the controller reports an error.
   *
   * @param outcomeType The type of outcome to add.
   * @param parameter   The parameter associated with the outcome (can be null or empty).
   */
  private void handleAddOutcome(OutcomeType outcomeType, String parameter) {
    LOG.debug("Add Outcome action triggered for type: {}, param: {}", outcomeType, parameter);
    if (currentObjectId == null || currentEventId == null) {
      showErrorAlert(KEY_ERROR_SELECTION_NEEDED, "Object and event must be selected.");
      return;
    }
    try {
      editorController.addOutcome(currentObjectId, currentEventId, outcomeType, parameter);
      LOG.info("Delegated add outcome '{}' (param: '{}') to event '{}'", outcomeType, parameter, currentEventId);
    } catch (Exception e) {
      LOG.error("Error delegating add outcome: {}", e.getMessage(), e);
      showErrorAlert(KEY_ERROR_API_FAILURE, "Failed to add outcome: " + e.getMessage());
    }
  }

  /**
   * Handles the action to remove the selected outcome from the currently selected event.
   * Delegates the action to the {@link EditorController}. Shows an error if no object, event,
   * or outcome is selected, or if the controller reports an error.
   *
   * @param outcomeType The type of outcome to remove.
   */
  private void handleRemoveOutcome(OutcomeType outcomeType) {
    LOG.debug("Remove Outcome action triggered for type: {}", outcomeType);
    if (currentObjectId == null || currentEventId == null) {
      showErrorAlert(KEY_ERROR_SELECTION_NEEDED, "Object, event, and outcome must be selected.");
      return;
    }
    try {
      editorController.removeOutcome(currentObjectId, currentEventId, outcomeType);
      LOG.info("Delegated remove outcome '{}' from event '{}'", outcomeType, currentEventId);
    } catch (Exception e) {
      LOG.error("Error delegating remove outcome: {}", e.getMessage(), e);
      showErrorAlert(KEY_ERROR_API_FAILURE, "Failed to remove outcome: " + e.getMessage());
    }
  }


  /**
   * Opens a dialog window to allow the user to define and add a new dynamic variable.
   * If the user confirms the dialog, the {@link #handleAddDynamicVariable(DynamicVariable)} method is called.
   */
  private void openAddDynamicVariableDialog() {
    LOG.debug("Opening 'Add Dynamic Variable' dialog.");
    DynamicVariableDialog dialog = new DynamicVariableDialog(uiBundle);
    Optional<DynamicVariable> result = dialog.showAndWait();
    result.ifPresent(this::handleAddDynamicVariable);
  }

  /**
   * Handles the result from the dynamic variable creation dialog.
   * Delegates the addition of the new dynamic variable to the {@link EditorController}.
   * Shows an error if the variable is null or if the controller reports an error.
   *
   * @param dynamicVar The {@link DynamicVariable} object created by the user, or null if cancelled.
   */
  private void handleAddDynamicVariable(DynamicVariable dynamicVar) {
    try {
      if (dynamicVar == null) {
        LOG.warn("Attempted to add a null dynamic variable.");
        return;
      }
      editorController.addDynamicVariable(dynamicVar);
      LOG.info("Delegated add dynamic variable: {}", dynamicVar.getName());
    } catch (Exception e) {
      LOG.error("Error delegating add dynamic variable: {}", e.getMessage(), e);
      showErrorAlert(KEY_ERROR_API_FAILURE, "Failed to add variable: " + e.getMessage());
    }
  }


  /**
   * Safely executes a {@link Runnable} action on the JavaFX Application Thread.
   * If called from the FX thread, it runs immediately. Otherwise, it uses {@link Platform#runLater(Runnable)}.
   *
   * @param action The action to execute on the FX thread.
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
   * based on the currently selected object (`currentObjectId`). Ensures execution on the FX thread.
   */
  private void refreshAllListsForObject() {
    runOnFxThread(() -> {
      refreshEventsListInternal();
      updateParameterComboBoxInternal();
    });
  }

  /**
   * Internal method to refresh the events list view.
   * Clears all lists first, then fetches events for the `currentObjectId` from the controller
   * and populates the `eventListView`. Must be called on the FX thread.
   */
  private void refreshEventsListInternal() {
    clearListsInternal(true, true, true);
    this.currentEventId = null;

    if (currentObjectId != null) {
      try {
        Map<String, ?> events = editorController.getEventsForObject(currentObjectId);
        if (eventListView != null && events != null && !events.isEmpty()) {
          eventListView.getItems().addAll(events.keySet()); // Add all events
          LOG.debug("Refreshed events list for object {}: {} events.", currentObjectId, events.size());
        } else {
          LOG.debug("No events found for object {}.", currentObjectId);
        }
      } catch (Exception e) {
        LOG.error("Controller failed to get events for object {}: {}", currentObjectId, e.getMessage(), e);
        showErrorAlert(KEY_ERROR_API_FAILURE, "Failed to load events: " + e.getMessage());
      }
    } else {
      LOG.debug("Events list cleared (no object selected).");
    }
  }

  /**
   * Refreshes the conditions list based on the currently selected object and event.
   * Ensures execution on the FX thread by calling {@link #refreshConditionsListInternal()}.
   */
  private void refreshConditionsList() {
    runOnFxThread(this::refreshConditionsListInternal);
  }

  /**
   * Internal method to refresh the conditions list view.
   * Clears the conditions list, then fetches conditions for the `currentObjectId` and `currentEventId`
   * from the controller and populates the `conditionsListView`. Must be called on the FX thread.
   */
  private void refreshConditionsListInternal() {
    clearListsInternal(false, true, false);

    if (currentObjectId != null && currentEventId != null) {
      try {
        List<ConditionType> conditions = editorController.getConditionsForEvent(currentObjectId, currentEventId);
        if (conditionsListView != null && conditions != null) {
          conditions.forEach(condition -> conditionsListView.getItems().add(condition.toString())); // Add all conditions
          LOG.debug("Refreshed conditions list for event '{}': {} conditions.", currentEventId, conditions.size());
        } else {
          LOG.debug("No conditions found for event '{}'.", currentEventId);
        }
      } catch (Exception e) {
        LOG.error("Controller failed to get conditions for event '{}': {}", currentEventId, e.getMessage(), e);
        showErrorAlert(KEY_ERROR_API_FAILURE, "Failed to load conditions: " + e.getMessage());
      }
    } else {
      LOG.trace("Conditions list not refreshed (no object/event selected).");
    }
  }

  /**
   * Refreshes the outcomes list based on the currently selected object and event.
   * Ensures execution on the FX thread by calling {@link #refreshOutcomesListInternal()}.
   */
  private void refreshOutcomesList() {
    runOnFxThread(this::refreshOutcomesListInternal);
  }

  /**
   * Internal method to refresh the outcomes list view.
   * Clears the outcomes list, then fetches outcomes (and their parameters) for the `currentObjectId`
   * and `currentEventId` from the controller and populates the `outcomesListView`.
   * Formats the display string to include the parameter if available. Must be called on the FX thread.
   */
  private void refreshOutcomesListInternal() {
    clearListsInternal(false, false, true);

    if (currentObjectId != null && currentEventId != null) {
      try {
        List<OutcomeType> outcomes = editorController.getOutcomesForEvent(currentObjectId, currentEventId);
        if (outcomesListView != null && outcomes != null) {
          outcomes.forEach(outcome -> {
            String parameter = null;
            try {
              parameter = editorController.getOutcomeParameter(currentObjectId, currentEventId, outcome);
            } catch (Exception paramEx) {
              LOG.warn("Could not retrieve parameter for outcome {} on event {}: {}", outcome, currentEventId, paramEx.getMessage());
            }
            String displayString = (parameter != null && !parameter.isEmpty())
                ? String.format("%s (%s)", outcome.toString(), parameter)
                : outcome.toString();
            outcomesListView.getItems().add(displayString); // Add all outcomes
          });
          LOG.debug("Refreshed outcomes list for event '{}': {} outcomes.", currentEventId, outcomes.size());
        } else {
          LOG.debug("No outcomes found for event '{}'.", currentEventId);
        }
      } catch (Exception e) {
        LOG.error("Controller failed to get outcomes for event '{}': {}", currentEventId, e.getMessage(), e);
        showErrorAlert(KEY_ERROR_API_FAILURE, "Failed to load outcomes: " + e.getMessage());
      }
    } else {
      LOG.trace("Outcomes list not refreshed (no object/event selected).");
    }
  }

  /**
   * Updates the parameter combo box with available dynamic variables for the current object context.
   * Ensures execution on the FX thread by calling {@link #updateParameterComboBoxInternal()}.
   */
  private void updateParameterComboBox() {
    runOnFxThread(this::updateParameterComboBoxInternal);
  }

  /**
   * Internal method to update the items in the parameter combo box.
   * Fetches the available {@link DynamicVariable}s from the controller for the `currentObjectId`
   * and updates the combo box provided by the {@link OutcomesSectionBuilder}. Must be called on the FX thread.
   */
  private void updateParameterComboBoxInternal() {
    if (outcomesSectionBuilder == null) return;

    try {
      List<DynamicVariable> variables = editorController.getAvailableDynamicVariables(currentObjectId);
      outcomesSectionBuilder.updateParameterComboBox(variables);
    } catch (Exception e) {
      LOG.error("Controller failed to get available dynamic variables: {}", e.getMessage(), e);
      showErrorAlert(KEY_ERROR_API_FAILURE, "Failed to load parameters: " + e.getMessage());
      if (outcomesSectionBuilder != null) {
        outcomesSectionBuilder.updateParameterComboBox(null);
      }
    }
  }


  /**
   * Clears all list views (events, conditions, outcomes) and resets related state.
   * Ensures execution on the FX thread by calling {@link #clearListsInternal(boolean, boolean, boolean)}.
   */
  private void clearAllLists() {
    runOnFxThread(() -> clearListsInternal(true, true, true));
  }

  /**
   * Internal method to clear the items from the specified list views and potentially
   * clear the event ID field and reset the `currentEventId`. Must be called on the FX thread.
   *
   * @param clearEvents     If true, clears the event list view and event ID field, and resets `currentEventId`.
   * @param clearConditions If true, clears the conditions list view.
   * @param clearOutcomes   If true, clears the outcomes list view.
   */
  private void clearListsInternal(boolean clearEvents, boolean clearConditions, boolean clearOutcomes) {
    if (clearEvents && eventListView != null) eventListView.getItems().clear();
    if (clearConditions && conditionsListView != null) conditionsListView.getItems().clear();
    if (clearOutcomes && outcomesListView != null) outcomesListView.getItems().clear();
    if (clearEvents && eventIdField != null) eventIdField.clear();
    if (clearEvents) {
      this.currentEventId = null;
    }
    LOG.trace("Cleared lists - Events: {}, Conditions: {}, Outcomes: {}", clearEvents, clearConditions, clearOutcomes);
  }


  /**
   * Displays an error alert dialog to the user.
   * Ensures the alert is shown on the JavaFX Application Thread.
   * Attempts to apply custom CSS styling to the dialog.
   *
   * @param titleKey    The resource bundle key for the alert title.
   * @param contentText The main message text to display in the alert.
   */
  private void showErrorAlert(String titleKey, String contentText) {
    runOnFxThread(() -> {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle(uiBundle.containsKey(titleKey) ? uiBundle.getString(titleKey) : titleKey);
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
   * Handles notification that an object was added to the model.
   * Currently, only logs the event.
   *
   * @param objectId The UUID of the added object.
   */
  @Override
  public void onObjectAdded(UUID objectId) {
    LOG.trace("InputTab received: onObjectAdded {}", objectId);
  }

  /**
   * Handles notification that an object was removed from the model.
   * If the removed object is the currently selected object, it clears the Input tab's display
   * by simulating a selection change to null.
   *
   * @param objectId The UUID of the removed object.
   */
  @Override
  public void onObjectRemoved(UUID objectId) {
    LOG.trace("InputTab received: onObjectRemoved {}", objectId);
    if (Objects.equals(this.currentObjectId, objectId)) {
      LOG.debug("Selected object {} was removed. Clearing input tab.", objectId);
      onSelectionChanged(null);
    }
  }

  /**
   * Handles notification that an object's data has been updated in the model.
   * If the updated object is the currently selected object, it refreshes all lists
   * in the Input tab to reflect the changes.
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
   * Handles notification that the selected object in the editor has changed.
   * Updates the internal `currentObjectId` and refreshes all lists in the Input tab
   * to display information for the newly selected object. Ensures execution on the FX thread.
   *
   * @param selectedObjectId The UUID of the newly selected object, or null if deselected.
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
   * Handles notification that the list of available dynamic variables has changed.
   * Refreshes the parameter combo box in the Outcomes section. Ensures execution on the FX thread.
   */
  @Override
  public void onDynamicVariablesChanged() {
    runOnFxThread(() -> {
      LOG.debug("InputTab received: onDynamicVariablesChanged");
      updateParameterComboBoxInternal();
    });
  }

  /**
   * Handles notification of an error occurring elsewhere in the application (e.g., model/controller).
   * Displays an error alert to the user.
   *
   * @param errorMessage The error message to display.
   */
  @Override
  public void onErrorOccurred(String errorMessage) {
    LOG.warn("InputTab received: onErrorOccurred: {}", errorMessage);
    showErrorAlert(KEY_ERROR_API_FAILURE, errorMessage);
  }
}