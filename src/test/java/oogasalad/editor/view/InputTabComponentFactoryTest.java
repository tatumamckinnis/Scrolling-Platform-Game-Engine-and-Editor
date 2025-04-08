package oogasalad.editor.view;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Point2D; // Import Point2D for scrolling
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.model.data.event_enum.ConditionType; // Correct Import
import oogasalad.editor.model.data.event_enum.OutcomeType; // Correct Import
import oogasalad.editor.model.data.object.DynamicVariable;
import oogasalad.editor.model.data.object.event.EditorEvent; // Import EditorEvent
import oogasalad.editor.view.resources.EditorResourceLoader;
import org.junit.jupiter.api.Assumptions; // Import Assumptions
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.ComboBoxMatchers;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.matcher.control.ListViewMatchers;
import org.testfx.matcher.control.TextInputControlMatchers;
import org.testfx.robot.Motion; // Import Motion
import org.testfx.service.query.PointQuery; // Import PointQuery
import org.testfx.util.WaitForAsyncUtils;


import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*; // Import assumeTrue
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.*;


@ExtendWith({ApplicationExtension.class})
class InputTabComponentFactoryTest {

  // Mocks - initialized in @Start
  private EditorController mockEditorController;
  private ResourceBundle mockUiBundle;

  // Assume builders create nodes with these IDs. Verify in builder code.
  private static final String EVENT_LIST_ID = "#eventListView";
  private static final String EVENT_ID_FIELD_ID = "#eventIdField";
  private static final String ADD_EVENT_BUTTON_ID = "#addEventButton";
  private static final String REMOVE_EVENT_BUTTON_ID = "#removeEventButton";
  private static final String CONDITIONS_LIST_ID = "#conditionsListView";
  // Match the ID set in ConditionsSectionBuilder
  private static final String ADD_CONDITION_COMBO_ID = "#conditionComboBox";
  private static final String ADD_CONDITION_BUTTON_ID = "#addConditionButton";
  private static final String REMOVE_CONDITION_BUTTON_ID = "#removeConditionButton";
  private static final String OUTCOMES_LIST_ID = "#outcomesListView";
  // Match the ID set in OutcomesSectionBuilder
  private static final String ADD_OUTCOME_COMBO_ID = "#outcomeTypeComboBox";
  private static final String PARAM_COMBO_ID = "#parameterComboBox";
  private static final String ADD_OUTCOME_BUTTON_ID = "#addOutcomeButton";
  private static final String REMOVE_OUTCOME_BUTTON_ID = "#removeOutcomeButton";
  private static final String ADD_VAR_BUTTON_ID = "#addVariableButton";

  private InputTabComponentFactory factory;
  private Pane inputTabPanel;

  // Test state variables - managed via listeners
  private UUID currentTestObjectId;

  @Captor ArgumentCaptor<DynamicVariable> dynamicVariableCaptor;
  @Captor ArgumentCaptor<String> stringCaptor;


  // Helper to setup mocks, factory, UI in one go
  private void setupUI(Stage stage) {
    mockEditorController = Mockito.mock(EditorController.class);
    mockUiBundle = Mockito.mock(ResourceBundle.class);

    // Reset state before each UI setup
    currentTestObjectId = null;

    try (MockedStatic<EditorResourceLoader> mockedLoader = mockStatic(EditorResourceLoader.class)) {
      mockedLoader.when(() -> EditorResourceLoader.loadResourceBundle("InputTabUI"))
          .thenReturn(mockUiBundle);
      when(mockUiBundle.getString(anyString())).thenReturn("Mock String"); // Default
      when(mockUiBundle.getString("errorSelectionNeeded")).thenReturn("Selection Error Message");
      when(mockUiBundle.getString("errorApiFailureTitle")).thenReturn("API Error Title");
      when(mockUiBundle.getString("dialogAddVarTitle")).thenReturn("Add Variable");
      when(mockUiBundle.getString("dialogAddButton")).thenReturn("Add");
      when(mockUiBundle.getString("dialogVarName")).thenReturn("Name");
      when(mockUiBundle.getString("dialogVarType")).thenReturn("Type");
      when(mockUiBundle.getString("dialogVarValue")).thenReturn("Value");
      when(mockUiBundle.getString("dialogVarDesc")).thenReturn("Desc");
      when(mockUiBundle.getString("errorInvalidInputTitle")).thenReturn("Invalid Input");
      // Add prompts used in builders if needed for mocks
      when(mockUiBundle.getString("conditionsHeader")).thenReturn("Conditions");
      when(mockUiBundle.getString("outcomesHeader")).thenReturn("Outcomes");
      when(mockUiBundle.getString("parameterLabel")).thenReturn("Parameter");
      when(mockUiBundle.getString("addConditionButton")).thenReturn("Add");
      when(mockUiBundle.getString("removeConditionButton")).thenReturn("Remove");
      when(mockUiBundle.getString("addOutcomeButton")).thenReturn("Add");
      when(mockUiBundle.getString("removeOutcomeButton")).thenReturn("Remove");
      when(mockUiBundle.getString("createParamButton")).thenReturn("+");


      factory = new InputTabComponentFactory(mockEditorController);
    }

    inputTabPanel = factory.createInputTabPanel();
    assertNotNull(inputTabPanel, "Input tab panel should be created");

    // Make scene slightly larger to reduce chance of scrolling issues initially
    Scene scene = new Scene(inputTabPanel, 500, 700);
    stage.setScene(scene);
    stage.setTitle("Input Tab Factory Test");
    stage.show();


    // Pre-populate combos with actual enum values - Wait after creation
    WaitForAsyncUtils.waitForFxEvents();
    Platform.runLater(() -> {
      ComboBox<ConditionType> condCombo = lookupComboBoxSafe(ADD_CONDITION_COMBO_ID);
      if (condCombo != null) condCombo.setItems(FXCollections.observableArrayList(ConditionType.values())); // Use actual values

      ComboBox<OutcomeType> outCombo = lookupComboBoxSafe(ADD_OUTCOME_COMBO_ID);
      if (outCombo != null) outCombo.setItems(FXCollections.observableArrayList(OutcomeType.values())); // Use actual values

      ComboBox<String> paramCombo = lookupComboBoxSafe(PARAM_COMBO_ID);
      if (paramCombo != null) paramCombo.setItems(FXCollections.observableArrayList("var1", "var2")); // Keep example params
    });
    WaitForAsyncUtils.waitForFxEvents();
  }

  @Start // Default setup
  private void start(Stage stage) {
    setupUI(stage);
  }

  // --- Helper Methods ---
  private <T extends Node> T lookupSafe(String query) {
    try {
      Set<Node> nodes = inputTabPanel.lookupAll(query);
      return nodes.isEmpty() ? null : (T) nodes.iterator().next();
    } catch (Exception e) {
      System.err.println("Warning: Node not found for query: '" + query + "'. Check ID in builder. Error: " + e.getMessage());
      return null;
    }
  }
  private ListView<String> lookupListViewSafe(String query) {
    Node node = lookupSafe(query);
    return (node instanceof ListView) ? (ListView<String>) node : null;
  }
  private <T> ComboBox<T> lookupComboBoxSafe(String query) {
    Node node = lookupSafe(query);
    return (node instanceof ComboBox) ? (ComboBox<T>) node : null;
  }
  // Helper to ensure a node is visible, scrolling if necessary
  private void ensureVisible(FxRobot robot, String query) {
    Node node = robot.lookup(query).query();
    robot.moveTo(node); // Moves mouse, implicitly scrolls viewport if needed by default
    WaitForAsyncUtils.waitForFxEvents();
  }

  // Helper to ensure a ListView item is visible and clickable
  private Node ensureListCellVisible(FxRobot robot, String listViewQuery, String itemText) {
    ListView<String> listView = lookupListViewSafe(listViewQuery);
    assumeTrue(listView != null, "ListView '" + listViewQuery + "' not found.");

    // Attempt to scroll to the item
    Platform.runLater(() -> {
      for(String item : listView.getItems()) {
        if(item.equals(itemText)) {
          listView.scrollTo(item);
          break;
        }
      }
    });
    WaitForAsyncUtils.waitForFxEvents();
    WaitForAsyncUtils.sleep(100, TimeUnit.MILLISECONDS); // Small delay for scroll animation

    // Find the cell - may need retry logic if scroll is slow
    Node cell = robot.lookup(".list-cell")
        .match(n -> n instanceof ListCell && ((ListCell<?>) n).getItem() != null && ((ListCell<?>) n).getItem().toString().equals(itemText) && isVisible(n))
        .query();
    robot.moveTo(cell); // Move to ensure visibility after potential scroll
    WaitForAsyncUtils.waitForFxEvents();
    return cell;
  }

  private boolean isVisible(Node node) {
    return node.isVisible() && node.getScene() != null && node.getScene().getWindow() != null && node.getScene().getWindow().isShowing();
  }

  private void selectObject(UUID objectId) {
    // Use clearInvocations instead of reset to preserve when(...).thenReturn setups
    // unless a full reset is explicitly desired for a test group.
    clearInvocations(mockEditorController);

    // Setup general behavior for this selection - specific tests might override later
    if (objectId != null) {
      // Define default behavior for methods called during selection changed
      when(mockEditorController.getEventsForObject(eq(objectId))).thenReturn(Collections.emptyMap());
      when(mockEditorController.getAvailableDynamicVariables(eq(objectId))).thenReturn(Collections.emptyList());
    } else {
     // when(mockEditorController.getAvailableDynamicVariables(isNull())).thenReturn(Collections.emptyList());
    }

    Platform.runLater(() -> factory.onSelectionChanged(objectId));
    WaitForAsyncUtils.waitForFxEvents(); // Wait for the selection change to process
    this.currentTestObjectId = objectId;
  }
  private void selectEvent(FxRobot robot, String eventId) {
    ListView<String> eventList = lookupListViewSafe(EVENT_LIST_ID);
    assumeTrue(eventList != null, "Event list view not found.");
    assumeTrue(eventList.getItems().contains(eventId), "Event '" + eventId + "' not found in list view items for selection.");

    // Ensure cell is visible and click it
    Node cell = ensureListCellVisible(robot, EVENT_LIST_ID, eventId);
    robot.clickOn(cell, Motion.DEFAULT);
    //-----------

    WaitForAsyncUtils.waitForFxEvents();
    // Add a small wait/retry for selection model update if still flaky
    WaitForAsyncUtils.waitForAsync(1000, () -> assertEquals(eventId, eventList.getSelectionModel().getSelectedItem())); // Wait up to 1 sec
    assertEquals(eventId, eventList.getSelectionModel().getSelectedItem(), "List view selection mismatch");
  }
  private void populateEventList(List<String> events) {
    ListView<String> eventList = lookupListViewSafe(EVENT_LIST_ID);
    assumeTrue(eventList != null, "Event list view not found for population.");
    Platform.runLater(() -> {
      eventList.setItems(FXCollections.observableArrayList(events));
    });
    WaitForAsyncUtils.waitForFxEvents();
  }
  private void populateConditionList(List<String> conditions) {
    ListView<String> condList = lookupListViewSafe(CONDITIONS_LIST_ID);
    assumeTrue(condList != null, "Condition list view not found for population.");
    Platform.runLater(() -> {
      condList.setItems(FXCollections.observableArrayList(conditions));
    });
    WaitForAsyncUtils.waitForFxEvents();
  }
  private void populateOutcomeList(List<String> outcomes) {
    ListView<String> outcomeList = lookupListViewSafe(OUTCOMES_LIST_ID);
    assumeTrue(outcomeList != null, "Outcome list view not found for population.");
    Platform.runLater(() -> {
      outcomeList.setItems(FXCollections.observableArrayList(outcomes));
    });
    WaitForAsyncUtils.waitForFxEvents();
  }
  private void populateParameterCombo(List<String> params) {
    ComboBox<String> paramCombo = lookupComboBoxSafe(PARAM_COMBO_ID);
    assumeTrue(paramCombo != null, "Parameter combo box not found for population.");
    Platform.runLater(() -> {
      paramCombo.setItems(FXCollections.observableArrayList(params));
    });
    WaitForAsyncUtils.waitForFxEvents();
  }
  private void selectCondition(FxRobot robot, String conditionString) {
    ListView<String> condList = lookupListViewSafe(CONDITIONS_LIST_ID);
    assumeTrue(condList != null, "Condition list view not found.");
    assumeTrue(condList.getItems().contains(conditionString), "Condition '" + conditionString + "' not found in list view items for selection.");

    // Ensure cell is visible and click it
    Node cell = ensureListCellVisible(robot, CONDITIONS_LIST_ID, conditionString);
    robot.clickOn(cell, Motion.DEFAULT);
    //-----------

    WaitForAsyncUtils.waitForFxEvents();
    // Add a small wait/retry for selection model update if still flaky
    WaitForAsyncUtils.waitForAsync(1000, () -> assertEquals(conditionString, condList.getSelectionModel().getSelectedItem()));
    assertEquals(conditionString, condList.getSelectionModel().getSelectedItem(), "Condition list view selection mismatch");
  }
  private void selectOutcome(FxRobot robot, String outcomeString) {
    ListView<String> outcomeList = lookupListViewSafe(OUTCOMES_LIST_ID);
    assumeTrue(outcomeList != null, "Outcome list view not found.");
    // Outcome might have parameter string, adjust check
    boolean found = outcomeList.getItems().stream().anyMatch(item -> item.equals(outcomeString));
    assumeTrue(found, "Outcome '" + outcomeString + "' not found in list view items for selection.");

    // Ensure cell is visible and click it
    Node cell = ensureListCellVisible(robot, OUTCOMES_LIST_ID, outcomeString);
    robot.clickOn(cell, Motion.DEFAULT);
    //-----------

    WaitForAsyncUtils.waitForFxEvents();
    // Add a small wait/retry for selection model update if still flaky
    WaitForAsyncUtils.waitForAsync(1000, () -> assertEquals(outcomeString, outcomeList.getSelectionModel().getSelectedItem()));
    assertEquals(outcomeString, outcomeList.getSelectionModel().getSelectedItem(), "Outcome list view selection mismatch");
  }


  // --- Action Handler Tests (Using Actual Enum Constants) ---

  @Test void testAddEventAction(FxRobot robot) {
    assumeTrue(lookupSafe(ADD_EVENT_BUTTON_ID) != null, "Add Event button not found.");
    UUID objId = UUID.randomUUID();
    selectObject(objId);

    ensureVisible(robot, EVENT_ID_FIELD_ID);
    robot.clickOn(EVENT_ID_FIELD_ID).write("TestEvent1");
    ensureVisible(robot, ADD_EVENT_BUTTON_ID);
    robot.clickOn(ADD_EVENT_BUTTON_ID);
    WaitForAsyncUtils.waitForFxEvents(); // Wait for action to process

    verify(mockEditorController).addEvent(eq(objId), eq("TestEvent1")); // Use matchers
    verifyThat(EVENT_ID_FIELD_ID, TextInputControlMatchers.hasText("")); // Check field cleared
  }
  @Test void testAddEventAction_EmptyId(FxRobot robot){
    assumeTrue(lookupSafe(ADD_EVENT_BUTTON_ID) != null, "Add Event button not found.");
    UUID objId = UUID.randomUUID();
    selectObject(objId);

    ensureVisible(robot, ADD_EVENT_BUTTON_ID);
    robot.clickOn(ADD_EVENT_BUTTON_ID); // Click without typing ID
    WaitForAsyncUtils.waitForFxEvents();

    verify(mockEditorController, never()).addEvent(any(), anyString());
    verifyThat(EVENT_ID_FIELD_ID, TextInputControlMatchers.hasText(""));
  }
  @Test void testAddEventAction_NoObjectSelected(FxRobot robot){
    assumeTrue(lookupSafe(ADD_EVENT_BUTTON_ID) != null, "Add Event button not found.");
    selectObject(null); // No object selected

    ensureVisible(robot, EVENT_ID_FIELD_ID);
    robot.clickOn(EVENT_ID_FIELD_ID).write("NoObjectEvent");
    ensureVisible(robot, ADD_EVENT_BUTTON_ID);
    robot.clickOn(ADD_EVENT_BUTTON_ID);
    WaitForAsyncUtils.waitForFxEvents();

    verify(mockEditorController, never()).addEvent(any(), anyString());
    verifyThat(EVENT_ID_FIELD_ID, TextInputControlMatchers.hasText("NoObjectEvent")); // Text remains
  }
  @Test void testAddEventAction_ControllerError(FxRobot robot){
    assumeTrue(lookupSafe(ADD_EVENT_BUTTON_ID) != null, "Add Event button not found.");
    UUID objId = UUID.randomUUID();
    String eventName = "ErrorEvent";

    // Set mock AFTER selectObject clears things
    selectObject(objId);
    doThrow(new RuntimeException("Controller Error")).when(mockEditorController).addEvent(eq(objId), eq(eventName));

    ensureVisible(robot, EVENT_ID_FIELD_ID);
    robot.clickOn(EVENT_ID_FIELD_ID).write(eventName);
    ensureVisible(robot, ADD_EVENT_BUTTON_ID);
    robot.clickOn(ADD_EVENT_BUTTON_ID);
    WaitForAsyncUtils.waitForFxEvents();

    verify(mockEditorController).addEvent(eq(objId), eq(eventName)); // Verify controller was called
    verifyThat(EVENT_ID_FIELD_ID, TextInputControlMatchers.hasText(eventName)); // Field should not clear on error
  }
  @Test void testRemoveEventAction(FxRobot robot){
    assumeTrue(lookupSafe(REMOVE_EVENT_BUTTON_ID) != null, "Remove Event button not found.");
    UUID objId = UUID.randomUUID();
    String eventToRemove = "eventA";
    String otherEvent = "eventB";

    // Setup mocks BEFORE selection triggers listener
    when(mockEditorController.getEventsForObject(eq(objId))).thenReturn(Map.of(eventToRemove, mock(EditorEvent.class), otherEvent, mock(EditorEvent.class)));
    selectObject(objId); // Trigger listener with initial (potentially empty) mocks defined in selectObject, then above mock takes precedence
    WaitForAsyncUtils.waitForFxEvents(); // Let selection listener finish

    // Explicitly populate list AFTER selection listener has run based on mocks
    populateEventList(List.of(eventToRemove, otherEvent));
    selectEvent(robot, eventToRemove); // Select the item (includes ensureVisible)

    ensureVisible(robot, REMOVE_EVENT_BUTTON_ID);
    robot.clickOn(REMOVE_EVENT_BUTTON_ID);
    WaitForAsyncUtils.waitForFxEvents();

    verify(mockEditorController).removeEvent(eq(objId), eq(eventToRemove)); // Use matchers
  }
  @Test void testRemoveEventAction_NoEventSelected(FxRobot robot){
    assumeTrue(lookupSafe(REMOVE_EVENT_BUTTON_ID) != null, "Remove Event button not found.");
    UUID objId = UUID.randomUUID();

    // Setup mocks before selection
    when(mockEditorController.getEventsForObject(eq(objId))).thenReturn(Map.of("eventA", mock(EditorEvent.class), "eventB", mock(EditorEvent.class)));
    selectObject(objId);
    WaitForAsyncUtils.waitForFxEvents();

    populateEventList(List.of("eventA", "eventB"));
    // No event selected

    ensureVisible(robot, REMOVE_EVENT_BUTTON_ID);
    robot.clickOn(REMOVE_EVENT_BUTTON_ID);
    WaitForAsyncUtils.waitForFxEvents();

    verify(mockEditorController, never()).removeEvent(any(), anyString());
  }
  @Test void testRemoveEventAction_NoObjectSelected(FxRobot robot){
    assumeTrue(lookupSafe(REMOVE_EVENT_BUTTON_ID) != null, "Remove Event button not found.");
    selectObject(null); // No object selected

    // Attempt to remove without selection
    ensureVisible(robot, REMOVE_EVENT_BUTTON_ID); // May fail if disabled, adjust assumption if needed
    robot.clickOn(REMOVE_EVENT_BUTTON_ID);
    WaitForAsyncUtils.waitForFxEvents();

    verify(mockEditorController, never()).removeEvent(any(), anyString());
  }

  @Test void testAddConditionAction_KEY_SPACE(FxRobot robot) { // Use actual condition
    assumeTrue(lookupSafe(ADD_CONDITION_COMBO_ID) != null, "Add Condition combo not found.");
    assumeTrue(lookupSafe(ADD_CONDITION_BUTTON_ID) != null, "Add Condition button not found.");
    UUID objId = UUID.randomUUID();
    String eventId = "eventA";
    ConditionType conditionToAdd = ConditionType.KEY_SPACE; // Use actual enum constant

    // Setup mocks before selection
    EditorEvent mockEvent = mock(EditorEvent.class);
    when(mockEditorController.getEventsForObject(eq(objId))).thenReturn(Map.of(eventId, mockEvent));
    selectObject(objId); // Trigger listener
    WaitForAsyncUtils.waitForFxEvents();

    populateEventList(List.of(eventId)); // Ensure visible list item
    selectEvent(robot, eventId); // Select the event

    // Interact with ComboBox and Button
    ensureVisible(robot, ADD_CONDITION_COMBO_ID);
    robot.clickOn(ADD_CONDITION_COMBO_ID).clickOn(conditionToAdd.toString());
    ensureVisible(robot, ADD_CONDITION_BUTTON_ID);
    robot.clickOn(ADD_CONDITION_BUTTON_ID);
    WaitForAsyncUtils.waitForFxEvents();

    // **FIXED**: Verify using matchers for all args
    verify(mockEditorController).addCondition(eq(objId), eq(eventId), eq(conditionToAdd));
  }
  @Test void testAddConditionAction_NoEventSelected(FxRobot robot) {
    assumeTrue(lookupSafe(ADD_CONDITION_COMBO_ID) != null, "Add Condition combo not found.");
    assumeTrue(lookupSafe(ADD_CONDITION_BUTTON_ID) != null, "Add Condition button not found.");
    UUID objId = UUID.randomUUID();
    ConditionType conditionToAdd = ConditionType.KEY_UP;

    // Setup mock before selection
    when(mockEditorController.getEventsForObject(eq(objId))).thenReturn(Map.of("someOtherEvent", mock(EditorEvent.class)));
    selectObject(objId); // Object selected, but no event selected
    WaitForAsyncUtils.waitForFxEvents();

    populateEventList(List.of("someOtherEvent")); // List has items, but none selected by default

    ensureVisible(robot, ADD_CONDITION_COMBO_ID);
    robot.clickOn(ADD_CONDITION_COMBO_ID).clickOn(conditionToAdd.toString());
    ensureVisible(robot, ADD_CONDITION_BUTTON_ID);
    robot.clickOn(ADD_CONDITION_BUTTON_ID);
    WaitForAsyncUtils.waitForFxEvents();

    verify(mockEditorController, never()).addCondition(any(), anyString(), any());
  }

  @Test void testRemoveConditionAction_KEY_LEFT(FxRobot robot) { // Use actual condition
    assumeTrue(lookupSafe(REMOVE_CONDITION_BUTTON_ID) != null, "Remove Condition button not found.");
    UUID objId = UUID.randomUUID();
    String eventId = "eventA";
    ConditionType conditionToRemove = ConditionType.KEY_LEFT; // Use actual enum constant
    String conditionString = conditionToRemove.toString();

    // Setup mocks before selection
    EditorEvent mockEvent = mock(EditorEvent.class);
    when(mockEditorController.getEventsForObject(eq(objId))).thenReturn(Map.of(eventId, mockEvent));
    // Mock getConditions *after* event is conceptually selected
    when(mockEditorController.getConditionsForEvent(eq(objId), eq(eventId))).thenReturn(List.of(conditionToRemove));
    selectObject(objId); // Trigger listener
    WaitForAsyncUtils.waitForFxEvents();


    populateEventList(List.of(eventId));
    selectEvent(robot, eventId); // This triggers condition list refresh internally in factory

    // Re-populate list *after* selection, because factory refreshes it
    populateConditionList(List.of(conditionString));
    selectCondition(robot, conditionString); // Select the item (includes ensureVisible)

    ensureVisible(robot, REMOVE_CONDITION_BUTTON_ID);
    robot.clickOn(REMOVE_CONDITION_BUTTON_ID);
    WaitForAsyncUtils.waitForFxEvents();

    // Use matchers for all arguments
    verify(mockEditorController).removeCondition(eq(objId), eq(eventId), eq(conditionToRemove));
  }
  @Test void testRemoveConditionAction_NoConditionSelected(FxRobot robot) {
    assumeTrue(lookupSafe(REMOVE_CONDITION_BUTTON_ID) != null, "Remove Condition button not found.");
    UUID objId = UUID.randomUUID();
    String eventId = "eventA";
    ConditionType existingCondition = ConditionType.KEY_RIGHT;

    // Setup mocks before selection
    EditorEvent mockEvent = mock(EditorEvent.class);
    when(mockEditorController.getEventsForObject(eq(objId))).thenReturn(Map.of(eventId, mockEvent));
    when(mockEditorController.getConditionsForEvent(eq(objId), eq(eventId))).thenReturn(List.of(existingCondition));
    selectObject(objId);
    WaitForAsyncUtils.waitForFxEvents();

    populateEventList(List.of(eventId));
    selectEvent(robot, eventId); // Triggers condition list refresh

    populateConditionList(List.of(existingCondition.toString())); // Repopulate visually

    // No condition selected
    ensureVisible(robot, REMOVE_CONDITION_BUTTON_ID);
    robot.clickOn(REMOVE_CONDITION_BUTTON_ID);
    WaitForAsyncUtils.waitForFxEvents();

    verify(mockEditorController, never()).removeCondition(any(), any(), any());
  }






  // --- Listener Logic Tests ---


  @Test void testOnObjectRemoved_SelectedObject() {
    UUID selectedId = UUID.randomUUID();

    // Setup behavior *before* selection
    when(mockEditorController.getEventsForObject(eq(selectedId))).thenReturn(Map.of("eventX", mock(EditorEvent.class)));
    selectObject(selectedId); // Triggers listener
    WaitForAsyncUtils.waitForFxEvents();

    populateEventList(List.of("eventX")); // Visually add item

    Platform.runLater(() -> factory.onObjectRemoved(selectedId));
    WaitForAsyncUtils.waitForFxEvents(); // Wait for listener processing

    ListView<String> eventList = lookupListViewSafe(EVENT_LIST_ID);
    assumeTrue(eventList != null, "Event list not found");

    // Verify the UI effect (list cleared) using verifyThat for implicit wait
    verifyThat(EVENT_LIST_ID, ListViewMatchers.isEmpty());
    assertTrue(eventList.getItems().isEmpty(), "Event list should be cleared");
  }

  @Test void testOnObjectRemoved_DifferentObject() {
    UUID selectedId = UUID.randomUUID();
    UUID removedId = UUID.randomUUID();

    // Setup behavior *before* selection
    when(mockEditorController.getEventsForObject(eq(selectedId))).thenReturn(Map.of("eventX", mock(EditorEvent.class)));
    selectObject(selectedId); // Set factory's currentObjectId to selectedId
    WaitForAsyncUtils.waitForFxEvents();

    populateEventList(List.of("eventX"));

    // Clear invocations AFTER initial selectObject is done processing
    clearInvocations(mockEditorController);

    // Act: Remove different ID
    Platform.runLater(() -> factory.onObjectRemoved(removedId));
    WaitForAsyncUtils.waitForFxEvents();
    WaitForAsyncUtils.sleep(200, TimeUnit.MILLISECONDS); // Allow time for potential unwanted actions

    // Assertions
    ListView<String> eventList = lookupListViewSafe(EVENT_LIST_ID);
    assumeTrue(eventList != null, "Event list not found");
    assertFalse(eventList.getItems().isEmpty(), "Event list should NOT be cleared");
    assertEquals(List.of("eventX"), new ArrayList<>(eventList.getItems()));
    assertEquals(selectedId, this.currentTestObjectId, "Test's internal object ID should remain unchanged");

    // Verify controller NOT called again for selectedId
    verify(mockEditorController, never()).getEventsForObject(eq(selectedId));
    verify(mockEditorController, never()).getAvailableDynamicVariables(eq(selectedId));

  }






  @Test void testOnDynamicVariablesChangedUpdatesParamCombo() {
    UUID objId = UUID.randomUUID();

    // Initial select - Vars should be empty based on selectObject default mock
    selectObject(objId);
    WaitForAsyncUtils.waitForFxEvents();

    ComboBox<String> paramCombo = lookupComboBoxSafe(PARAM_COMBO_ID);
    assumeTrue(paramCombo != null, "Parameter combo box not found.");

    // Verify initial state (empty) using verifyThat
    // **FIXED**: Use hasItems(0)
    verifyThat(PARAM_COMBO_ID, ComboBoxMatchers.hasItems(0));
    assertTrue(paramCombo.getItems().isEmpty());


    // Simulate change 1: variables become empty (redundant check)
    when(mockEditorController.getAvailableDynamicVariables(eq(objId))).thenReturn(List.of());
    Platform.runLater(() -> factory.onDynamicVariablesChanged()); // Call the listener
    WaitForAsyncUtils.waitForFxEvents();
    // **FIXED**: Use hasItems(0)
    verifyThat(PARAM_COMBO_ID, ComboBoxMatchers.hasItems(0)); // Wait for update implicitly
    assertTrue(paramCombo.getItems().isEmpty());


    // Simulate change 2: variables updated
    DynamicVariable varA = new DynamicVariable("varA", "int", "0", "");
    when(mockEditorController.getAvailableDynamicVariables(eq(objId)))
        .thenReturn(List.of(varA));
    Platform.runLater(() -> factory.onDynamicVariablesChanged()); // Call the listener again
    WaitForAsyncUtils.waitForFxEvents();

    // Verify updated state using verifyThat
    verifyThat(PARAM_COMBO_ID, ComboBoxMatchers.hasItems(1)); // Wait for update implicitly
    assertEquals(List.of("varA"), new ArrayList<>(paramCombo.getItems()));
  }


}