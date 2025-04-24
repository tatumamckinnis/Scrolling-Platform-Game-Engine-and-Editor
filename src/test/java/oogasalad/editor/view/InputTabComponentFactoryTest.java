package oogasalad.editor.view;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.controller.EditorDataAPI;
import oogasalad.editor.controller.object_data.InputDataManager;
import oogasalad.editor.model.data.object.DynamicVariable;
import oogasalad.editor.model.data.object.DynamicVariableContainer;
import oogasalad.editor.model.data.object.event.EditorEvent;
import oogasalad.editor.model.data.object.event.ExecutorData;
import oogasalad.editor.view.resources.EditorResourceLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


/**
 * Test class for {@link InputTabComponentFactory}. Uses TestFX for UI interactions and Mockito for
 * mocking controller dependencies. Verifies UI setup, event handler delegation to the refactored
 * controller methods, and UI updates based on listener notifications.
 */
@ExtendWith({ApplicationExtension.class})
class InputTabComponentFactoryTest {

  @Mock
  private EditorController mockEditorController;
  @Mock
  private EditorDataAPI mockEditorDataAPI;
  @Mock
  private InputDataManager mockInputDataManager;
  @Mock
  private DynamicVariableContainer mockDynamicVariableContainer;

  private static final String EVENT_LIST_ID = "#eventListView";
  private static final String EVENT_ID_FIELD_ID = "#eventIdField";
  private static final String ADD_EVENT_BUTTON_ID = "#addEventButton";
  private static final String REMOVE_EVENT_BUTTON_ID = "#removeEventButton";
  private static final String CONDITIONS_LIST_ID = "#conditionsListView";
  private static final String ADD_CONDITION_COMBO_ID = "#conditionTypeComboBox";
  private static final String ADD_CONDITION_BUTTON_ID = "#addConditionButton";
  private static final String REMOVE_CONDITION_BUTTON_ID = "#removeConditionButton";
  private static final String ADD_GROUP_BUTTON_ID = "#addGroupButton";
  private static final String REMOVE_GROUP_BUTTON_ID = "#removeGroupButton";
  private static final String OUTCOMES_LIST_ID = "#outcomesListView";
  private static final String ADD_OUTCOME_COMBO_ID = "#outcomeTypeComboBox";
  private static final String PARAM_COMBO_ID = "#dynamicVariableComboBox";
  private static final String ADD_OUTCOME_BUTTON_ID = "#addOutcomeButton";
  private static final String REMOVE_OUTCOME_BUTTON_ID = "#removeOutcomeButton";
  private static final String ADD_VAR_BUTTON_ID = "#addVariableButton";
  private static final String CONDITION_PARAMS_PANE_ID = "#conditionParametersPane";
  private static final String OUTCOME_PARAMS_PANE_ID = "#outcomeParametersPane";

  private InputTabComponentFactory factory;
  private Pane inputTabPanel;

  private UUID currentTestObjectId;
  private String currentTestEventId;

  @Captor
  ArgumentCaptor<DynamicVariable> dynamicVariableCaptor;
  @Captor
  ArgumentCaptor<String> stringArgCaptor;
  @Captor
  ArgumentCaptor<Integer> intArgCaptor;
  @Captor
  ArgumentCaptor<Double> doubleArgCaptor;


  private AutoCloseable mocks;

  /**
   * Initializes JavaFX platform, mocks, resource bundle loading, and factory instance. Creates the
   * UI within the JavaFX thread.
   *
   * @param stage Primary stage provided by TestFX.
   */
  @Start
  private void start(Stage stage) {
    mocks = MockitoAnnotations.openMocks(this);

    when(mockEditorController.getEditorDataAPI()).thenReturn(mockEditorDataAPI);
    when(mockEditorDataAPI.getInputDataAPI()).thenReturn(mockInputDataManager);
    when(mockEditorDataAPI.getDynamicVariableContainer()).thenReturn(mockDynamicVariableContainer);

    try (MockedStatic<EditorResourceLoader> mockedLoader = mockStatic(EditorResourceLoader.class)) {
      ResourceBundle bundle = ResourceBundle.getBundle(
          "oogasalad.editor.view.resources.InputTabUI");
      mockedLoader.when(() -> EditorResourceLoader.loadResourceBundle(anyString()))
          .thenReturn(bundle);
      factory = new InputTabComponentFactory(mockEditorController);
    }

    inputTabPanel = factory.createInputTabPanel();
    assertNotNull(inputTabPanel);

    Scene scene = new Scene(inputTabPanel, 600, 800);
    stage.setScene(scene);
    stage.setTitle("Input Tab Factory Test");
    stage.show();
  }

  /**
   * Closes Mockito mocks after each test.
   *
   * @throws Exception If closing fails.
   */
  @AfterEach
  void tearDown() throws Exception {
    if (mocks != null) {
      mocks.close();
    }
    Platform.runLater(() -> {
    });
    WaitForAsyncUtils.waitForFxEvents();
  }


  /**
   * Looks up a node safely within the inputTabPanel. Uses lookupAll for robustness.
   *
   * @param query CSS selector.
   * @param <T>   Node type.
   * @return Found node or null.
   */
  private <T extends Node> T lookupSafe(String query) {
    try {
      Set<Node> nodes = inputTabPanel.lookupAll(query);
      return nodes.isEmpty() ? null : (T) nodes.iterator().next();
    } catch (Exception e) {
      System.err.println("Lookup warning for '" + query + "': " + e.getMessage());
      return null;
    }
  }

  /**
   * Simulates selecting an object by calling the factory's listener method. Ensures it runs on the
   * FX thread and waits for events. IMPORTANT: Caller must mock getEvents and getAllVariables
   * *before* calling this, as onSelectionChanged triggers refreshes that use these mocks.
   *
   * @param objectId The UUID of the object to select, or null to deselect.
   */
  private void selectObject(UUID objectId) {
    this.currentTestObjectId = objectId;

    Platform.runLater(() -> factory.onSelectionChanged(objectId));
    WaitForAsyncUtils.waitForFxEvents();
  }


  /**
   * Simulates selecting an event from the event list view using the FxRobot. Waits for the event to
   * appear in the list before clicking. Mocks subsequent data fetches for conditions/outcomes.
   * IMPORTANT: Caller must ensure the event list is populated (via selectObject and mocks) and that
   * currentTestObjectId is set correctly *before* calling this.
   *
   * @param robot   FxRobot for UI interaction.
   * @param eventId The ID of the event to select.
   * @throws TimeoutException if the event does not appear in time.
   */
  private void selectEvent(FxRobot robot, String eventId) throws TimeoutException {
    ListView<String> eventList = lookupSafe(EVENT_LIST_ID);
    assertNotNull(eventList, "Event list view not found for selectEvent");

    WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> eventList.getItems().contains(eventId));
    assertTrue(eventList.getItems().contains(eventId),
        "Event ID not found in list view: " + eventId);

    this.currentTestEventId = eventId;

    when(mockInputDataManager.getEventConditions(eq(currentTestObjectId), eq(eventId))).thenReturn(
        Collections.emptyList());
    when(mockInputDataManager.getEventOutcomes(eq(currentTestObjectId), eq(eventId))).thenReturn(
        Collections.emptyList());

    robot.clickOn(eventId);
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals(eventId, eventList.getSelectionModel().getSelectedItem(),
        "Event selection failed in UI");
  }


  /**
   * Verifies that the main UI sections are created and accessible via lookup.
   */
  @Test
  void testUILayoutCreation() {
    assertNotNull(lookupSafe(EVENT_LIST_ID), "Event list view should be present");
    assertNotNull(lookupSafe(CONDITIONS_LIST_ID), "Conditions list view should be present");
    assertNotNull(lookupSafe(OUTCOMES_LIST_ID), "Outcomes list view should be present");
    assertNotNull(lookupSafe(ADD_EVENT_BUTTON_ID), "Add event button should be present");
    assertNotNull(lookupSafe(ADD_CONDITION_BUTTON_ID), "Add condition button should be present");
    assertNotNull(lookupSafe(ADD_OUTCOME_BUTTON_ID), "Add outcome button should be present");
  }

  /**
   * Tests adding an event through the UI interaction, verifying the controller call and UI refresh.
   * Uses doAnswer to update the mock for getEvents after addEvent is called.
   *
   * @param robot FxRobot for UI interaction.
   */
  @Test
  void testAddEvent(FxRobot robot) throws TimeoutException {
    UUID objId = UUID.randomUUID();
    String newEventId = "PLAYER_START";
    Map<String, EditorEvent> initialEvents = Collections.emptyMap();
    Map<String, EditorEvent> finalEvents = Map.of(newEventId, mock(EditorEvent.class));

    when(mockInputDataManager.getEvents(eq(objId))).thenReturn(initialEvents);
    when(mockDynamicVariableContainer.getAllVariables()).thenReturn(Collections.emptyList());
    selectObject(objId);

    doAnswer(invocation -> {

      when(mockInputDataManager.getEvents(eq(objId))).thenReturn(finalEvents);

      Platform.runLater(() -> factory.refreshEventsList());
      return null;
    }).when(mockInputDataManager).addEvent(eq(objId), eq(newEventId));

    robot.clickOn(EVENT_ID_FIELD_ID).write(newEventId);
    robot.clickOn(ADD_EVENT_BUTTON_ID);
    WaitForAsyncUtils.waitForFxEvents();

    verify(mockInputDataManager).addEvent(eq(objId), eq(newEventId));

    ListView<String> eventList = lookupSafe(EVENT_LIST_ID);
    assertNotNull(eventList);

    WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> eventList.getItems().contains(newEventId));
    assertTrue(eventList.getItems().contains(newEventId));
  }

  /**
   * Tests removing an event through the UI interaction, verifying the controller call and UI
   * refresh. Uses doAnswer to update the mock for getEvents after removeEvent is called.
   *
   * @param robot FxRobot for UI interaction.
   */
  @Test
  void testRemoveEvent(FxRobot robot) throws TimeoutException {
    UUID objId = UUID.randomUUID();
    String eventToRemove = "EVENT_1";
    String eventToKeep = "EVENT_2";
    Map<String, EditorEvent> initialEvents = new HashMap<>();
    initialEvents.put(eventToRemove, mock(EditorEvent.class));
    initialEvents.put(eventToKeep, mock(EditorEvent.class));
    Map<String, EditorEvent> finalEvents = Map.of(eventToKeep, mock(EditorEvent.class));

    when(mockInputDataManager.getEvents(eq(objId))).thenReturn(initialEvents);
    when(mockDynamicVariableContainer.getAllVariables()).thenReturn(Collections.emptyList());
    selectObject(objId);

    selectEvent(robot, eventToRemove);

    doAnswer(invocation -> {
      when(mockInputDataManager.getEvents(eq(objId))).thenReturn(finalEvents);

      Platform.runLater(() -> factory.refreshEventsList());
      return null;
    }).when(mockInputDataManager).removeEvent(eq(objId), eq(eventToRemove));

    robot.clickOn(REMOVE_EVENT_BUTTON_ID);
    WaitForAsyncUtils.waitForFxEvents();

    verify(mockInputDataManager).removeEvent(eq(objId), eq(eventToRemove));

    ListView<String> eventList = lookupSafe(EVENT_LIST_ID);
    assertNotNull(eventList);

    WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
        () -> !eventList.getItems().contains(eventToRemove));
    assertFalse(eventList.getItems().contains(eventToRemove));
    assertTrue(eventList.getItems().contains(eventToKeep));
  }


  /**
   * Tests adding a condition to the selected event via UI interaction. Uses doAnswer to update mock
   * for getEventConditions.
   *
   * @param robot FxRobot for UI interaction.
   */
  @Test
  void testAddCondition(FxRobot robot) throws TimeoutException {
    UUID objId = UUID.randomUUID();
    String eventId = "ON_COLLIDE";
    String conditionType = "COLLISION";
    ExecutorData addedConditionData = new ExecutorData(conditionType, Map.of("targetTag", "Enemy"),
        Collections.emptyMap());
    List<List<ExecutorData>> finalConditions = List.of(List.of(addedConditionData));

    when(mockInputDataManager.getEvents(eq(objId))).thenReturn(
        Map.of(eventId, mock(EditorEvent.class)));
    when(mockInputDataManager.getEventConditions(eq(objId), eq(eventId))).thenReturn(
        Collections.emptyList());
    when(mockInputDataManager.getEventOutcomes(eq(objId), eq(eventId))).thenReturn(
        Collections.emptyList());
    when(mockDynamicVariableContainer.getAllVariables()).thenReturn(Collections.emptyList());
    selectObject(objId);
    selectEvent(robot, eventId);

    doAnswer(invocation -> {
      when(mockInputDataManager.getEventConditions(eq(objId), eq(eventId))).thenReturn(
          finalConditions);

      Platform.runLater(() -> factory.refreshConditionsAndOutcomesForEvent());
      return null;
    }).when(mockInputDataManager)
        .addEventCondition(eq(objId), eq(eventId), anyInt(), eq(conditionType));

    Platform.runLater(() -> {
      ComboBox<String> combo = lookupSafe(ADD_CONDITION_COMBO_ID);
      if (combo != null) {
        combo.setItems(FXCollections.observableArrayList(conditionType, "OTHER"));
      }
    });
    WaitForAsyncUtils.waitForFxEvents();

    robot.clickOn(ADD_CONDITION_COMBO_ID).clickOn(conditionType);
    robot.clickOn(ADD_CONDITION_BUTTON_ID);
    WaitForAsyncUtils.waitForFxEvents();

    verify(mockInputDataManager).addEventCondition(eq(objId), eq(eventId), eq(0),
        eq(conditionType));

    ListView<ConditionDisplayItem> conditionList = lookupSafe(CONDITIONS_LIST_ID);
    assertNotNull(conditionList);

    WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> !conditionList.getItems().isEmpty());
    assertEquals(1, conditionList.getItems().size());
    assertTrue(conditionList.getItems().get(0).toString().contains(conditionType));
  }


  /**
   * Tests adding an outcome to the selected event via UI interaction. Uses doAnswer to update mock
   * for getEventOutcomes.
   *
   * @param robot FxRobot for UI interaction.
   */
  @Test
  void testAddOutcome(FxRobot robot) throws TimeoutException {
    UUID objId = UUID.randomUUID();
    String eventId = "ON_JUMP";
    String outcomeType = "PLAY_SOUND";
    ExecutorData addedOutcomeData = new ExecutorData(outcomeType, Map.of("soundId", "jump.wav"),
        Collections.emptyMap());
    List<ExecutorData> finalOutcomes = List.of(addedOutcomeData);

    when(mockInputDataManager.getEvents(eq(objId))).thenReturn(
        Map.of(eventId, mock(EditorEvent.class)));
    when(mockInputDataManager.getEventConditions(eq(objId), eq(eventId))).thenReturn(
        Collections.emptyList());
    when(mockInputDataManager.getEventOutcomes(eq(objId), eq(eventId))).thenReturn(
        Collections.emptyList());
    when(mockDynamicVariableContainer.getAllVariables()).thenReturn(Collections.emptyList());
    selectObject(objId);
    selectEvent(robot, eventId);

    doAnswer(invocation -> {
      when(mockInputDataManager.getEventOutcomes(eq(objId), eq(eventId))).thenReturn(finalOutcomes);

      Platform.runLater(() -> factory.refreshConditionsAndOutcomesForEvent());
      return null;
    }).when(mockInputDataManager).addEventOutcome(eq(objId), eq(eventId), eq(outcomeType));

    Platform.runLater(() -> {
      ComboBox<String> combo = lookupSafe(ADD_OUTCOME_COMBO_ID);
      if (combo != null) {
        combo.setItems(FXCollections.observableArrayList(outcomeType, "OTHER"));
      }
    });
    WaitForAsyncUtils.waitForFxEvents();

    robot.clickOn(ADD_OUTCOME_COMBO_ID).clickOn(outcomeType);
    robot.clickOn(ADD_OUTCOME_BUTTON_ID);
    WaitForAsyncUtils.waitForFxEvents();

    verify(mockInputDataManager).addEventOutcome(eq(objId), eq(eventId), eq(outcomeType));

    ListView<OutcomeDisplayItem> outcomeList = lookupSafe(OUTCOMES_LIST_ID);
    assertNotNull(outcomeList);

    WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> !outcomeList.getItems().isEmpty());
    assertEquals(1, outcomeList.getItems().size());
    assertTrue(outcomeList.getItems().get(0).toString().contains(outcomeType));
  }

  /**
   * Tests that selecting an object correctly updates the event list and dynamic variables combo
   * box. Uses explicit waits for UI updates. Addresses TooFewActualInvocations.
   */
  @Test
  void testObjectSelectionUpdatesUI() throws TimeoutException {
    UUID objId1 = UUID.randomUUID();
    UUID objId2 = UUID.randomUUID();
    Map<String, EditorEvent> events1 = Map.of("event1", mock(EditorEvent.class));
    Map<String, EditorEvent> events2 = Map.of("event2a", mock(EditorEvent.class), "event2b",
        mock(EditorEvent.class));
    List<DynamicVariable> vars1 = List.of(new DynamicVariable("var1", "int", "0", ""));
    List<DynamicVariable> vars2 = List.of(new DynamicVariable("var2", "int", "0", ""),
        new DynamicVariable("var3", "int", "0", ""));

    ListView<String> eventList = lookupSafe(EVENT_LIST_ID);
    ComboBox<String> varCombo = lookupSafe(PARAM_COMBO_ID);
    assertNotNull(eventList);
    assertNotNull(varCombo);

    when(mockInputDataManager.getEvents(eq(objId1))).thenReturn(events1);
    when(mockDynamicVariableContainer.getAllVariables()).thenReturn(vars1);
    selectObject(objId1);

    WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> eventList.getItems().size() == 1);
    assertEquals(1, eventList.getItems().size());
    assertEquals("event1", eventList.getItems().get(0));
    WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> varCombo.getItems().size() == 1);
    assertEquals(1, varCombo.getItems().size());
    assertEquals("var1", varCombo.getItems().get(0));
    verify(mockInputDataManager).getEvents(eq(objId1));
    verify(mockDynamicVariableContainer).getAllVariables();

    when(mockInputDataManager.getEvents(eq(objId2))).thenReturn(events2);
    when(mockDynamicVariableContainer.getAllVariables()).thenReturn(vars2);
    selectObject(objId2);

    WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> eventList.getItems().size() == 2);
    assertEquals(2, eventList.getItems().size());
    assertTrue(eventList.getItems().containsAll(List.of("event2a", "event2b")));
    WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> varCombo.getItems().size() == 2);
    assertEquals(2, varCombo.getItems().size());
    assertTrue(varCombo.getItems().containsAll(List.of("var2", "var3")));
    verify(mockInputDataManager).getEvents(eq(objId2));
    verify(mockDynamicVariableContainer, times(2)).getAllVariables();

    when(mockDynamicVariableContainer.getAllVariables()).thenReturn(Collections.emptyList());
    selectObject(null);

    WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> eventList.getItems().isEmpty());
    assertTrue(eventList.getItems().isEmpty());

    verify(mockDynamicVariableContainer, times(2)).getAllVariables();

    WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> varCombo.getItems().isEmpty());
    assertTrue(varCombo.getItems().isEmpty());
  }

}
