package oogasalad.editor.view;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.geometry.Insets;
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
import oogasalad.editor.model.data.object.DynamicVariableContainer;
import oogasalad.editor.model.data.object.event.EditorEvent;
import oogasalad.editor.model.data.object.event.ExecutorData;
import oogasalad.editor.view.resources.EditorResourceLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class InputTabComponentFactory implements EditorViewListener {

  private static final Logger LOG = LogManager.getLogger(InputTabComponentFactory.class);

  private static final String UI_BUNDLE_NAME = "InputTabUI";
  private static final String CSS_PATH = "/css/editor/editor.css";
  private static final String KEY_ERROR_SELECTION_NEEDED = "errorSelectionNeeded";
  private static final String KEY_ERROR_API_FAILURE = "errorApiFailureTitle";
  private static final String KEY_ERROR_ACTION_FAILED = "errorActionFailedContent";

  private static final double DEFAULT_PADDING = 12.0;
  private static final double SECTION_SPACING = 25.0;

  private final EditorController editorController;
  private final ResourceBundle uiBundle;

  private EventsSectionBuilder eventsSectionBuilder;
  private ConditionsSectionBuilder conditionsSectionBuilder;
  private OutcomesSectionBuilder outcomesSectionBuilder;

  private UUID currentObjectId;
  private String currentEventId;

  public InputTabComponentFactory(EditorController editorController) {
    this.editorController = Objects.requireNonNull(editorController,
        "EditorController cannot be null.");
    try {
      this.uiBundle = EditorResourceLoader.loadResourceBundle(UI_BUNDLE_NAME);
    } catch (Exception e) {
      LOG.fatal("Failed to load essential UI resource bundle {}. Cannot continue.", UI_BUNDLE_NAME,
          e);
      throw new RuntimeException("Failed to load UI resource bundle: " + UI_BUNDLE_NAME, e);
    }
    createSectionBuilders();
    LOG.info("InputTabComponentFactory initialized.");
  }

  @Override
  public void onPrefabsChanged() {
    LOG.debug("InputTabComponentFactory notified of prefab changes.");
  }

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

  public Pane createInputTabPanel() {
    VBox contentVBox = new VBox(SECTION_SPACING);
    contentVBox.setPadding(new Insets(DEFAULT_PADDING));
    contentVBox.setId("input-tab-content-vbox");

    Node eventsSection = eventsSectionBuilder.build();
    Node conditionsSection = conditionsSectionBuilder.build();
    Node outcomesSection = outcomesSectionBuilder.build();

    contentVBox.getChildren().addAll(eventsSection, conditionsSection, outcomesSection);

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

    clearAllUIToDefault();
    LOG.debug("Input tab panel created using AnchorPane -> ScrollPane -> VBox.");

    return rootPane;
  }

  private void handleEventSelectionChange(String selectedEventId) {
    runOnFxThread(() -> {
      this.currentEventId = selectedEventId;
      LOG.debug("Internal state: Event selection changed to: {}", currentEventId);
      refreshConditionsAndOutcomesForEvent();
    });
  }

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
      showErrorAlert(KEY_ERROR_API_FAILURE,
          String.format(uiBundle.getString(KEY_ERROR_ACTION_FAILED), "add event", e.getMessage()));
    }
  }


  private void handleRemoveEvent() {
    LOG.debug("Remove Event action triggered.");
    String selectedEvent = eventsSectionBuilder.getEventListView().getSelectionModel()
        .getSelectedItem();
    if (!isSelected(true, false) || selectedEvent == null) {
      showErrorAlert(KEY_ERROR_SELECTION_NEEDED,
          "An object and an event must be selected for removal.");
      return;
    }
    try {
      editorController.removeEvent(currentObjectId, selectedEvent);
      LOG.info("Delegated remove event '{}' for object {}", selectedEvent, currentObjectId);
      refreshEventsList();
    } catch (Exception e) {
      LOG.error("Error delegating remove event: {}", e.getMessage(), e);
      showErrorAlert(KEY_ERROR_API_FAILURE,
          String.format(uiBundle.getString(KEY_ERROR_ACTION_FAILED), "remove event",
              e.getMessage()));
    }
  }

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
      showErrorAlert(KEY_ERROR_API_FAILURE,
          String.format(uiBundle.getString(KEY_ERROR_ACTION_FAILED), "add condition group",
              e.getMessage()));
    }
  }


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
      showErrorAlert(KEY_ERROR_API_FAILURE,
          String.format(uiBundle.getString(KEY_ERROR_ACTION_FAILED), "remove condition group",
              e.getMessage()));
    }
  }

  private void handleAddCondition(int groupIndex, String conditionType) {
    LOG.debug("Add Condition action triggered for type: {} in group {}", conditionType, groupIndex);
    if (!isSelected(true, true)) {
      return;
    }
    try {
      editorController.addEventCondition(currentObjectId, currentEventId, groupIndex,
          conditionType);
      refreshConditionsAndOutcomesForEvent();
    } catch (Exception e) {
      LOG.error("Error delegating add condition: {}", e.getMessage(), e);
      showErrorAlert(KEY_ERROR_API_FAILURE,
          String.format(uiBundle.getString(KEY_ERROR_ACTION_FAILED), "add condition",
              e.getMessage()));
    }
  }

  private void handleRemoveCondition(int groupIndex, int conditionIndex) {
    LOG.debug("Remove Condition action triggered for group {}, index {}", groupIndex,
        conditionIndex);
    if (!isSelected(true, true)) {
      return;
    }
    try {
      editorController.removeEventCondition(currentObjectId, currentEventId, groupIndex,
          conditionIndex);
      refreshConditionsAndOutcomesForEvent();
    } catch (Exception e) {
      LOG.error("Error delegating remove condition: {}", e.getMessage(), e);
      showErrorAlert(KEY_ERROR_API_FAILURE,
          String.format(uiBundle.getString(KEY_ERROR_ACTION_FAILED), "remove condition",
              e.getMessage()));
    }
  }

  private void handleEditConditionParam(int groupIndex, int conditionIndex, String paramName,
      Object value) {
    if (!isSelected(true, true)) {
      return;
    }
    handleEditParam(paramName, value,
        (strVal) -> editorController.setEventConditionStringParameter(currentObjectId,
            currentEventId, groupIndex, conditionIndex, paramName, strVal),
        (dblVal) -> editorController.setEventConditionDoubleParameter(currentObjectId,
            currentEventId, groupIndex, conditionIndex, paramName, dblVal),
        "condition"
    );
  }

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
      showErrorAlert(KEY_ERROR_API_FAILURE,
          String.format(uiBundle.getString(KEY_ERROR_ACTION_FAILED), "add outcome",
              e.getMessage()));
    }
  }

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
      showErrorAlert(KEY_ERROR_API_FAILURE,
          String.format(uiBundle.getString(KEY_ERROR_ACTION_FAILED), "remove outcome",
              e.getMessage()));
    }
  }

  private void handleEditOutcomeParam(int outcomeIndex, String paramName, Object value) {
    if (!isSelected(true, true)) {
      return;
    }
    handleEditParam(paramName, value,
        (strVal) -> editorController.setEventOutcomeStringParameter(currentObjectId,
            currentEventId, outcomeIndex, paramName, strVal),
        (dblVal) -> editorController.setEventOutcomeDoubleParameter(currentObjectId,
            currentEventId, outcomeIndex, paramName, dblVal),
        "outcome"
    );
  }

  private void handleEditParam(String paramName, Object value,
      Consumer<String> stringSetter, Consumer<Double> doubleSetter, String contextType) {
    LOG.trace("Edit {} Param: param={}, value={}", contextType, paramName, value);
    try {
      if (value instanceof String strValue) {
        stringSetter.accept(strValue);
      } else if (value instanceof Double doubleValue) {
        doubleSetter.accept(doubleValue);
      } else {
        LOG.warn("Unsupported parameter type for {}: {}", contextType,
            value == null ? "null" : value.getClass().getName());
      }
    } catch (Exception e) {
      LOG.error("Error delegating edit {} parameter: {}", contextType, e.getMessage(), e);
      showErrorAlert(KEY_ERROR_API_FAILURE,
          String.format(uiBundle.getString(KEY_ERROR_ACTION_FAILED), "edit " + contextType + " parameter",
              e.getMessage()));
      refreshConditionsAndOutcomesForEvent();
    }
  }


  private void openAddDynamicVariableDialog() {
    LOG.debug("Opening 'Add Dynamic Variable' dialog.");
    if (!isSelected(true, false)) {
      return;
    }
    DynamicVariableDialog dialog = new DynamicVariableDialog(uiBundle);
    Optional<DynamicVariable> result = dialog.showAndWait();
    result.ifPresent(this::handleAddDynamicVariable);
  }

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
      showErrorAlert(KEY_ERROR_API_FAILURE,
          String.format(uiBundle.getString(KEY_ERROR_ACTION_FAILED), "add dynamic variable",
              e.getMessage()));
    }
  }


  private List<String> getConditionTypeNames() {
    return Stream.of(ConditionType.values()).map(Enum::name).sorted().collect(Collectors.toList());
  }


  private List<String> getOutcomeTypeNames() {
    return Stream.of(OutcomeType.values()).map(Enum::name).sorted().collect(Collectors.toList());
  }


  private List<DynamicVariable> getDynamicVariablesForObject() {
    if (currentObjectId == null) {
      return Collections.emptyList();
    }
    try {
      return editorController.getAvailableDynamicVariables(currentObjectId);
    } catch (Exception e) {
      LOG.error("Failed to get dynamic variables for object {}: {}", currentObjectId,
          e.getMessage(), e);
      return Collections.emptyList();
    }
  }


  private void runOnFxThread(Runnable action) {
    if (Platform.isFxApplicationThread()) {
      action.run();
    } else {
      Platform.runLater(action);
    }
  }


  private void refreshUIForObject() {
    runOnFxThread(() -> {
      LOG.debug("Refreshing UI for object: {}", currentObjectId);
      refreshEventsList();
      refreshDynamicVariables();
    });
  }


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
      LOG.debug("Refreshed events list for object {}: {} events.", currentObjectId,
          sortedEventIds.size());
    });
  }


  private Map<String, EditorEvent> fetchEventsForCurrentObject() {
    if (currentObjectId == null) {
      return Collections.emptyMap();
    }
    try {
      return editorController.getEventsForObject(currentObjectId);
    } catch (Exception e) {
      LOG.error("Controller failed to get events for object {}: {}", currentObjectId,
          e.getMessage(), e);
      showErrorAlert(KEY_ERROR_API_FAILURE,
          String.format(uiBundle.getString(KEY_ERROR_ACTION_FAILED), "fetch events",
              e.getMessage()));
      return null;
    }
  }


  void refreshConditionsAndOutcomesForEvent() {
    runOnFxThread(() -> {
      LOG.debug("Refreshing conditions and outcomes for event: {}", currentEventId);
      List<List<ExecutorData>> conditions = fetchConditionsForCurrentEvent();
      List<ExecutorData> outcomes = fetchOutcomesForCurrentEvent();

      conditionsSectionBuilder.updateConditionsListView(conditions);
      outcomesSectionBuilder.updateOutcomesListView(outcomes);
    });
  }


  private List<List<ExecutorData>> fetchConditionsForCurrentEvent() {
    if (currentObjectId == null || currentEventId == null) {
      return null;
    }
    try {
      return editorController.getEventConditions(currentObjectId, currentEventId);
    } catch (Exception e) {
      LOG.error("Controller failed to get conditions for event {}: {}", currentEventId,
          e.getMessage(), e);
      showErrorAlert(KEY_ERROR_API_FAILURE,
          String.format(uiBundle.getString(KEY_ERROR_ACTION_FAILED), "fetch conditions",
              e.getMessage()));
      return null;
    }
  }


  private List<ExecutorData> fetchOutcomesForCurrentEvent() {
    if (currentObjectId == null || currentEventId == null) {
      return null;
    }
    try {
      return editorController.getEventOutcomes(currentObjectId, currentEventId);
    } catch (Exception e) {
      LOG.error("Controller failed to get outcomes for event {}: {}", currentEventId,
          e.getMessage(), e);
      showErrorAlert(KEY_ERROR_API_FAILURE,
          String.format(uiBundle.getString(KEY_ERROR_ACTION_FAILED), "fetch outcomes",
              e.getMessage()));
      return null;
    }
  }


  private void refreshDynamicVariables() {
    runOnFxThread(() -> {
      LOG.debug("Refreshing dynamic variables for object: {}", currentObjectId);
      outcomesSectionBuilder.updateDynamicVariableComboBox();
    });
  }


  private void clearConditionsAndOutcomesUI() {
    runOnFxThread(() -> {
      this.currentEventId = null;
      conditionsSectionBuilder.updateConditionsListView(null);
      outcomesSectionBuilder.updateOutcomesListView(null);
      LOG.trace("Cleared conditions and outcomes UI sections.");
    });
  }


  private void clearAllUIToDefault() {
    runOnFxThread(() -> {
      eventsSectionBuilder.getEventListView().getItems().clear();
      eventsSectionBuilder.getEventIdField().clear();
      clearConditionsAndOutcomesUI();
      refreshDynamicVariables();
      LOG.trace("Cleared all input tab UI to default.");
    });
  }


  private boolean isSelected(boolean requireObjectSelection, boolean requireEventSelection) {
    String errorMessage = null;
    if (requireObjectSelection && currentObjectId == null) {
      errorMessage = "An object must be selected.";
    } else if (requireEventSelection && currentEventId == null) {
      errorMessage = "An event must be selected.";
    }

    if (errorMessage != null) {
      LOG.warn("Action requires selection: {}", errorMessage);
      showErrorAlert(KEY_ERROR_SELECTION_NEEDED, errorMessage);
      return false;
    }
    return true;
  }


  private void showErrorAlert(String titleKey, String contentText) {
    runOnFxThread(() -> {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle(uiBundle.containsKey(titleKey) ? uiBundle.getString(titleKey) : titleKey);
      alert.setHeaderText(null);
      alert.setContentText(contentText);
      try {
        String cssUrl = Objects.requireNonNull(getClass().getResource(CSS_PATH)).toExternalForm();
        alert.getDialogPane().getStylesheets().add(cssUrl);
      } catch (Exception e) {
        LOG.warn("Could not apply CSS {} to error alert: {}", CSS_PATH, e.getMessage());
      }
      alert.showAndWait();
    });
  }

  @Override
  public void onObjectAdded(UUID objectId) {
    LOG.trace("InputTab received: onObjectAdded {}", objectId);
    refreshDynamicVariables();
  }


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


  @Override
  public void onDynamicVariablesChanged() {
    runOnFxThread(() -> {
      LOG.debug("InputTab received: onDynamicVariablesChanged");
      refreshDynamicVariables();
    });
  }


  @Override
  public void onErrorOccurred(String errorMessage) {
    LOG.warn("InputTab received: onErrorOccurred: {}", errorMessage);
    showErrorAlert(KEY_ERROR_API_FAILURE, errorMessage);
  }
}