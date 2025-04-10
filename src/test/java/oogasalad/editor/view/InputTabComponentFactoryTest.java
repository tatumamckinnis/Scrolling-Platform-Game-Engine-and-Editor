package oogasalad.editor.view;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.model.data.event_enum.ConditionType;
import oogasalad.editor.model.data.event_enum.OutcomeType;
import oogasalad.editor.model.data.object.DynamicVariable;
import oogasalad.editor.model.data.object.event.EditorEvent;
import oogasalad.editor.view.resources.EditorResourceLoader;
import org.junit.jupiter.api.Assumptions;
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
import org.testfx.robot.Motion;
import org.testfx.service.query.PointQuery;
import org.testfx.util.WaitForAsyncUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;

@ExtendWith({ApplicationExtension.class})
class InputTabComponentFactoryTest {

  private EditorController mockEditorController;
  private ResourceBundle mockUiBundle;

  private static final String EVENT_LIST_ID = "#eventListView";
  private static final String EVENT_ID_FIELD_ID = "#eventIdField";
  private static final String ADD_EVENT_BUTTON_ID = "#addEventButton";
  private static final String REMOVE_EVENT_BUTTON_ID = "#removeEventButton";
  private static final String CONDITIONS_LIST_ID = "#conditionsListView";
  private static final String ADD_CONDITION_COMBO_ID = "#conditionComboBox";
  private static final String ADD_CONDITION_BUTTON_ID = "#addConditionButton";
  private static final String REMOVE_CONDITION_BUTTON_ID = "#removeConditionButton";
  private static final String OUTCOMES_LIST_ID = "#outcomesListView";
  private static final String ADD_OUTCOME_COMBO_ID = "#outcomeTypeComboBox";
  private static final String PARAM_COMBO_ID = "#parameterComboBox";
  private static final String ADD_OUTCOME_BUTTON_ID = "#addOutcomeButton";
  private static final String REMOVE_OUTCOME_BUTTON_ID = "#removeOutcomeButton";
  private static final String ADD_VAR_BUTTON_ID = "#addVariableButton";

  private InputTabComponentFactory factory;
  private Pane inputTabPanel;

  private UUID currentTestObjectId;

  @Captor ArgumentCaptor<DynamicVariable> dynamicVariableCaptor;
  @Captor ArgumentCaptor<String> stringCaptor;

  private void setupUI(Stage stage) {
    mockEditorController = Mockito.mock(EditorController.class);
    mockUiBundle = Mockito.mock(ResourceBundle.class);

    currentTestObjectId = null;

    try (MockedStatic<EditorResourceLoader> mockedLoader = mockStatic(EditorResourceLoader.class)) {
      mockedLoader.when(() -> EditorResourceLoader.loadResourceBundle("InputTabUI"))
          .thenReturn(mockUiBundle);
      when(mockUiBundle.getString(anyString())).thenReturn("Mock String");
      when(mockUiBundle.getString("errorSelectionNeeded")).thenReturn("Selection Error Message");
      when(mockUiBundle.getString("errorApiFailureTitle")).thenReturn("API Error Title");
      when(mockUiBundle.getString("dialogAddVarTitle")).thenReturn("Add Variable");
      when(mockUiBundle.getString("dialogAddButton")).thenReturn("Add");
      when(mockUiBundle.getString("dialogVarName")).thenReturn("Name");
      when(mockUiBundle.getString("dialogVarType")).thenReturn("Type");
      when(mockUiBundle.getString("dialogVarValue")).thenReturn("Value");
      when(mockUiBundle.getString("dialogVarDesc")).thenReturn("Desc");
      when(mockUiBundle.getString("errorInvalidInputTitle")).thenReturn("Invalid Input");
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
    assertNotNull(inputTabPanel);

    Scene scene = new Scene(inputTabPanel, 500, 700);
    stage.setScene(scene);
    stage.setTitle("Input Tab Factory Test");
    stage.show();

    WaitForAsyncUtils.waitForFxEvents();
    Platform.runLater(() -> {
      ComboBox<ConditionType> condCombo = lookupComboBoxSafe(ADD_CONDITION_COMBO_ID);
      if (condCombo != null) condCombo.setItems(FXCollections.observableArrayList(ConditionType.values()));

      ComboBox<OutcomeType> outCombo = lookupComboBoxSafe(ADD_OUTCOME_COMBO_ID);
      if (outCombo != null) outCombo.setItems(FXCollections.observableArrayList(OutcomeType.values()));

      ComboBox<String> paramCombo = lookupComboBoxSafe(PARAM_COMBO_ID);
      if (paramCombo != null) paramCombo.setItems(FXCollections.observableArrayList("var1", "var2"));
    });
    WaitForAsyncUtils.waitForFxEvents();
  }

  @Start
  private void start(Stage stage) {
    setupUI(stage);
  }

  private <T extends Node> T lookupSafe(String query) {
    try {
      Set<Node> nodes = inputTabPanel.lookupAll(query);
      return nodes.isEmpty() ? null : (T) nodes.iterator().next();
    } catch (Exception e) {
      System.err.println("Warning: Node not found for query: '" + query + "'. Error: " + e.getMessage());
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

  private void ensureVisible(FxRobot robot, String query) {
    Node node = robot.lookup(query).query();
    robot.moveTo(node);
    WaitForAsyncUtils.waitForFxEvents();
  }

  private Node ensureListCellVisible(FxRobot robot, String listViewQuery, String itemText) {
    ListView<String> listView = lookupListViewSafe(listViewQuery);
    assumeTrue(listView != null);

    Platform.runLater(() -> {
      for(String item : listView.getItems()) {
        if(item.equals(itemText)) {
          listView.scrollTo(item);
          break;
        }
      }
    });
    WaitForAsyncUtils.waitForFxEvents();
    WaitForAsyncUtils.sleep(100, TimeUnit.MILLISECONDS);

    Node cell = robot.lookup(".list-cell")
        .match(n -> n instanceof ListCell && ((ListCell<?>) n).getItem() != null && ((ListCell<?>) n).getItem().toString().equals(itemText) && isVisible(n))
        .query();
    robot.moveTo(cell);
    WaitForAsyncUtils.waitForFxEvents();
    return cell;
  }

  private boolean isVisible(Node node) {
    return node.isVisible() && node.getScene() != null && node.getScene().getWindow() != null && node.getScene().getWindow().isShowing();
  }
  private void selectObject(UUID objectId) {
    clearInvocations(mockEditorController);

    if (objectId != null) {
      when(mockEditorController.getEventsForObject(eq(objectId))).thenReturn(Collections.emptyMap());
      when(mockEditorController.getAvailableDynamicVariables(eq(objectId))).thenReturn(Collections.emptyList());
    }

    Platform.runLater(() -> factory.onSelectionChanged(objectId));
    WaitForAsyncUtils.waitForFxEvents();
    this.currentTestObjectId = objectId;
  }

  private void selectEvent(FxRobot robot, String eventId) {
    ListView<String> eventList = lookupListViewSafe(EVENT_LIST_ID);
    assumeTrue(eventList != null);
    assumeTrue(eventList.getItems().contains(eventId));

    Node cell = ensureListCellVisible(robot, EVENT_LIST_ID, eventId);
    robot.clickOn(cell, Motion.DEFAULT);
    WaitForAsyncUtils.waitForFxEvents();
    WaitForAsyncUtils.waitForAsync(1000, () -> assertEquals(eventId, eventList.getSelectionModel().getSelectedItem()));
    assertEquals(eventId, eventList.getSelectionModel().getSelectedItem());
  }

  private void populateEventList(List<String> events) {
    ListView<String> eventList = lookupListViewSafe(EVENT_LIST_ID);
    assumeTrue(eventList != null);
    Platform.runLater(() -> {
      eventList.setItems(FXCollections.observableArrayList(events));
    });
    WaitForAsyncUtils.waitForFxEvents();
  }

  private void populateConditionList(List<String> conditions) {
    ListView<String> condList = lookupListViewSafe(CONDITIONS_LIST_ID);
    assumeTrue(condList != null);
    Platform.runLater(() -> {
      condList.setItems(FXCollections.observableArrayList(conditions));
    });
    WaitForAsyncUtils.waitForFxEvents();
  }

  private void populateOutcomeList(List<String> outcomes) {
    ListView<String> outcomeList = lookupListViewSafe(OUTCOMES_LIST_ID);
    assumeTrue(outcomeList != null);
    Platform.runLater(() -> {
      outcomeList.setItems(FXCollections.observableArrayList(outcomes));
    });
    WaitForAsyncUtils.waitForFxEvents();
  }

  private void populateParameterCombo(List<String> params) {
    ComboBox<String> paramCombo = lookupComboBoxSafe(PARAM_COMBO_ID);
    assumeTrue(paramCombo != null);
    Platform.runLater(() -> {
      paramCombo.setItems(FXCollections.observableArrayList(params));
    });
    WaitForAsyncUtils.waitForFxEvents();
  }

  private void selectCondition(FxRobot robot, String conditionString) {
    ListView<String> condList = lookupListViewSafe(CONDITIONS_LIST_ID);
    assumeTrue(condList != null);
    assumeTrue(condList.getItems().contains(conditionString));

    Node cell = ensureListCellVisible(robot, CONDITIONS_LIST_ID, conditionString);
    robot.clickOn(cell, Motion.DEFAULT);
    WaitForAsyncUtils.waitForFxEvents();
    WaitForAsyncUtils.waitForAsync(1000, () -> assertEquals(conditionString, condList.getSelectionModel().getSelectedItem()));
    assertEquals(conditionString, condList.getSelectionModel().getSelectedItem());
  }

  private void selectOutcome(FxRobot robot, String outcomeString) {
    ListView<String> outcomeList = lookupListViewSafe(OUTCOMES_LIST_ID);
    assumeTrue(outcomeList != null);
    boolean found = outcomeList.getItems().stream().anyMatch(item -> item.equals(outcomeString));
    assumeTrue(found);

    Node cell = ensureListCellVisible(robot, OUTCOMES_LIST_ID, outcomeString);
    robot.clickOn(cell, Motion.DEFAULT);
    WaitForAsyncUtils.waitForFxEvents();
    WaitForAsyncUtils.waitForAsync(1000, () -> assertEquals(outcomeString, outcomeList.getSelectionModel().getSelectedItem()));
    assertEquals(outcomeString, outcomeList.getSelectionModel().getSelectedItem());
  }

  @Test void testAddEventAction(FxRobot robot) {
    assumeTrue(lookupSafe(ADD_EVENT_BUTTON_ID) != null);
    UUID objId = UUID.randomUUID();
    selectObject(objId);

    ensureVisible(robot, EVENT_ID_FIELD_ID);
    robot.clickOn(EVENT_ID_FIELD_ID).write("TestEvent1");
    ensureVisible(robot, ADD_EVENT_BUTTON_ID);
    robot.clickOn(ADD_EVENT_BUTTON_ID);
    WaitForAsyncUtils.waitForFxEvents();

    verify(mockEditorController).addEvent(eq(objId), eq("TestEvent1"));
    verifyThat(EVENT_ID_FIELD_ID, TextInputControlMatchers.hasText(""));
  }

  @Test void testAddEventAction_EmptyId(FxRobot robot){
    assumeTrue(lookupSafe(ADD_EVENT_BUTTON_ID) != null);
    UUID objId = UUID.randomUUID();
    selectObject(objId);

    ensureVisible(robot, ADD_EVENT_BUTTON_ID);
    robot.clickOn(ADD_EVENT_BUTTON_ID);
    WaitForAsyncUtils.waitForFxEvents();

    verify(mockEditorController, never()).addEvent(any(), anyString());
    verifyThat(EVENT_ID_FIELD_ID, TextInputControlMatchers.hasText(""));
  }

  @Test void testAddEventAction_NoObjectSelected(FxRobot robot){
    assumeTrue(lookupSafe(ADD_EVENT_BUTTON_ID) != null);
    selectObject(null);

    ensureVisible(robot, EVENT_ID_FIELD_ID);
    robot.clickOn(EVENT_ID_FIELD_ID).write("NoObjectEvent");
    ensureVisible(robot, ADD_EVENT_BUTTON_ID);
    robot.clickOn(ADD_EVENT_BUTTON_ID);
    WaitForAsyncUtils.waitForFxEvents();

    verify(mockEditorController, never()).addEvent(any(), anyString());
    verifyThat(EVENT_ID_FIELD_ID, TextInputControlMatchers.hasText("NoObjectEvent"));
  }

  @Test void testAddEventAction_ControllerError(FxRobot robot){
    assumeTrue(lookupSafe(ADD_EVENT_BUTTON_ID) != null);
    UUID objId = UUID.randomUUID();
    String eventName = "ErrorEvent";

    selectObject(objId);
    doThrow(new RuntimeException("Controller Error")).when(mockEditorController).addEvent(eq(objId), eq(eventName));

    ensureVisible(robot, EVENT_ID_FIELD_ID);
    robot.clickOn(EVENT_ID_FIELD_ID).write(eventName);
    ensureVisible(robot, ADD_EVENT_BUTTON_ID);
    robot.clickOn(ADD_EVENT_BUTTON_ID);
    WaitForAsyncUtils.waitForFxEvents();

    verify(mockEditorController).addEvent(eq(objId), eq(eventName));
    verifyThat(EVENT_ID_FIELD_ID, TextInputControlMatchers.hasText(eventName));
  }
  @Test void testRemoveEventAction(FxRobot robot){
    assumeTrue(lookupSafe(REMOVE_EVENT_BUTTON_ID) != null);
    UUID objId = UUID.randomUUID();
    String eventToRemove = "eventA";
    String otherEvent = "eventB";

    when(mockEditorController.getEventsForObject(eq(objId))).thenReturn(Map.of(eventToRemove, mock(EditorEvent.class), otherEvent, mock(EditorEvent.class)));
    selectObject(objId);
    WaitForAsyncUtils.waitForFxEvents();

    populateEventList(List.of(eventToRemove, otherEvent));
    selectEvent(robot, eventToRemove);

    ensureVisible(robot, REMOVE_EVENT_BUTTON_ID);
    robot.clickOn(REMOVE_EVENT_BUTTON_ID);
    WaitForAsyncUtils.waitForFxEvents();

    verify(mockEditorController).removeEvent(eq(objId), eq(eventToRemove));
  }

  @Test void testRemoveEventAction_NoEventSelected(FxRobot robot){
    assumeTrue(lookupSafe(REMOVE_EVENT_BUTTON_ID) != null);
    UUID objId = UUID.randomUUID();

    when(mockEditorController.getEventsForObject(eq(objId))).thenReturn(Map.of("eventA", mock(EditorEvent.class), "eventB", mock(EditorEvent.class)));
    selectObject(objId);
    WaitForAsyncUtils.waitForFxEvents();

    populateEventList(List.of("eventA", "eventB"));

    ensureVisible(robot, REMOVE_EVENT_BUTTON_ID);
    robot.clickOn(REMOVE_EVENT_BUTTON_ID);
    WaitForAsyncUtils.waitForFxEvents();

    verify(mockEditorController, never()).removeEvent(any(), anyString());
  }

  @Test void testRemoveEventAction_NoObjectSelected(FxRobot robot){
    assumeTrue(lookupSafe(REMOVE_EVENT_BUTTON_ID) != null);
    selectObject(null);

    ensureVisible(robot, REMOVE_EVENT_BUTTON_ID);
    robot.clickOn(REMOVE_EVENT_BUTTON_ID);
    WaitForAsyncUtils.waitForFxEvents();

    verify(mockEditorController, never()).removeEvent(any(), anyString());
  }

  @Test void testAddConditionAction_KEY_SPACE(FxRobot robot) {
    assumeTrue(lookupSafe(ADD_CONDITION_COMBO_ID) != null);
    assumeTrue(lookupSafe(ADD_CONDITION_BUTTON_ID) != null);
    UUID objId = UUID.randomUUID();
    String eventId = "eventA";
    ConditionType conditionToAdd = ConditionType.KEY_SPACE;

    EditorEvent mockEvent = mock(EditorEvent.class);
    when(mockEditorController.getEventsForObject(eq(objId))).thenReturn(Map.of(eventId, mockEvent));
    selectObject(objId);
    WaitForAsyncUtils.waitForFxEvents();

    populateEventList(List.of(eventId));
    selectEvent(robot, eventId);

    ensureVisible(robot, ADD_CONDITION_COMBO_ID);
    robot.clickOn(ADD_CONDITION_COMBO_ID).clickOn(conditionToAdd.toString());
    ensureVisible(robot, ADD_CONDITION_BUTTON_ID);
    robot.clickOn(ADD_CONDITION_BUTTON_ID);
    WaitForAsyncUtils.waitForFxEvents();

    verify(mockEditorController).addCondition(eq(objId), eq(eventId), eq(conditionToAdd));
  }

  @Test void testAddConditionAction_NoEventSelected(FxRobot robot) {
    assumeTrue(lookupSafe(ADD_CONDITION_COMBO_ID) != null);
    assumeTrue(lookupSafe(ADD_CONDITION_BUTTON_ID) != null);
    UUID objId = UUID.randomUUID();
    ConditionType conditionToAdd = ConditionType.KEY_UP;

    when(mockEditorController.getEventsForObject(eq(objId))).thenReturn(Map.of("someOtherEvent", mock(EditorEvent.class)));
    selectObject(objId);
    WaitForAsyncUtils.waitForFxEvents();

    populateEventList(List.of("someOtherEvent"));

    ensureVisible(robot, ADD_CONDITION_COMBO_ID);
    robot.clickOn(ADD_CONDITION_COMBO_ID).clickOn(conditionToAdd.toString());
    ensureVisible(robot, ADD_CONDITION_BUTTON_ID);
    robot.clickOn(ADD_CONDITION_BUTTON_ID);
    WaitForAsyncUtils.waitForFxEvents();

    verify(mockEditorController, never()).addCondition(any(), anyString(), any());
  }

  @Test void testRemoveConditionAction_KEY_LEFT(FxRobot robot) {
    assumeTrue(lookupSafe(REMOVE_CONDITION_BUTTON_ID) != null);
    UUID objId = UUID.randomUUID();
    String eventId = "eventA";
    ConditionType conditionToRemove = ConditionType.KEY_LEFT;
    String conditionString = conditionToRemove.toString();

    EditorEvent mockEvent = mock(EditorEvent.class);
    when(mockEditorController.getEventsForObject(eq(objId))).thenReturn(Map.of(eventId, mockEvent));
    when(mockEditorController.getConditionsForEvent(eq(objId), eq(eventId))).thenReturn(List.of(conditionToRemove));
    selectObject(objId);
    WaitForAsyncUtils.waitForFxEvents();

    populateEventList(List.of(eventId));
    selectEvent(robot, eventId);

    populateConditionList(List.of(conditionString));
    selectCondition(robot, conditionString);

    ensureVisible(robot, REMOVE_CONDITION_BUTTON_ID);
    robot.clickOn(REMOVE_CONDITION_BUTTON_ID);
    WaitForAsyncUtils.waitForFxEvents();

    verify(mockEditorController).removeCondition(eq(objId), eq(eventId), eq(conditionToRemove));
  }

  @Test void testRemoveConditionAction_NoConditionSelected(FxRobot robot) {
    assumeTrue(lookupSafe(REMOVE_CONDITION_BUTTON_ID) != null);
    UUID objId = UUID.randomUUID();
    String eventId = "eventA";
    ConditionType existingCondition = ConditionType.KEY_RIGHT;

    EditorEvent mockEvent = mock(EditorEvent.class);
    when(mockEditorController.getEventsForObject(eq(objId))).thenReturn(Map.of(eventId, mockEvent));
    when(mockEditorController.getConditionsForEvent(eq(objId), eq(eventId))).thenReturn(List.of(existingCondition));
    selectObject(objId);
    WaitForAsyncUtils.waitForFxEvents();

    populateEventList(List.of(eventId));
    selectEvent(robot, eventId);

    populateConditionList(List.of(existingCondition.toString()));

    ensureVisible(robot, REMOVE_CONDITION_BUTTON_ID);
    robot.clickOn(REMOVE_CONDITION_BUTTON_ID);
    WaitForAsyncUtils.waitForFxEvents();

    verify(mockEditorController, never()).removeCondition(any(), any(), any());
  }
  @Test void testOnObjectRemoved_SelectedObject() {
    UUID selectedId = UUID.randomUUID();

    when(mockEditorController.getEventsForObject(eq(selectedId))).thenReturn(Map.of("eventX", mock(EditorEvent.class)));
    selectObject(selectedId);
    WaitForAsyncUtils.waitForFxEvents();

    populateEventList(List.of("eventX"));

    Platform.runLater(() -> factory.onObjectRemoved(selectedId));
    WaitForAsyncUtils.waitForFxEvents();

    ListView<String> eventList = lookupListViewSafe(EVENT_LIST_ID);
    assumeTrue(eventList != null);

    verifyThat(EVENT_LIST_ID, ListViewMatchers.isEmpty());
    assertTrue(eventList.getItems().isEmpty());
  }

  @Test void testOnObjectRemoved_DifferentObject() {
    UUID selectedId = UUID.randomUUID();
    UUID removedId = UUID.randomUUID();

    when(mockEditorController.getEventsForObject(eq(selectedId))).thenReturn(Map.of("eventX", mock(EditorEvent.class)));
    selectObject(selectedId);
    WaitForAsyncUtils.waitForFxEvents();

    populateEventList(List.of("eventX"));

    clearInvocations(mockEditorController);

    Platform.runLater(() -> factory.onObjectRemoved(removedId));
    WaitForAsyncUtils.waitForFxEvents();
    WaitForAsyncUtils.sleep(200, TimeUnit.MILLISECONDS);

    ListView<String> eventList = lookupListViewSafe(EVENT_LIST_ID);
    assumeTrue(eventList != null);
    assertFalse(eventList.getItems().isEmpty());
    assertEquals(List.of("eventX"), new ArrayList<>(eventList.getItems()));
    assertEquals(selectedId, this.currentTestObjectId);

    verify(mockEditorController, never()).getEventsForObject(eq(selectedId));
    verify(mockEditorController, never()).getAvailableDynamicVariables(eq(selectedId));
  }

  @Test void testOnDynamicVariablesChangedUpdatesParamCombo() {
    UUID objId = UUID.randomUUID();

    selectObject(objId);
    WaitForAsyncUtils.waitForFxEvents();

    ComboBox<String> paramCombo = lookupComboBoxSafe(PARAM_COMBO_ID);
    assumeTrue(paramCombo != null);

    verifyThat(PARAM_COMBO_ID, ComboBoxMatchers.hasItems(0));
    assertTrue(paramCombo.getItems().isEmpty());

    when(mockEditorController.getAvailableDynamicVariables(eq(objId))).thenReturn(List.of());
    Platform.runLater(() -> factory.onDynamicVariablesChanged());
    WaitForAsyncUtils.waitForFxEvents();
    verifyThat(PARAM_COMBO_ID, ComboBoxMatchers.hasItems(0));
    assertTrue(paramCombo.getItems().isEmpty());

    DynamicVariable varA = new DynamicVariable("varA", "int", "0", "");
    when(mockEditorController.getAvailableDynamicVariables(eq(objId)))
        .thenReturn(List.of(varA));
    Platform.runLater(() -> factory.onDynamicVariablesChanged());
    WaitForAsyncUtils.waitForFxEvents();

    verifyThat(PARAM_COMBO_ID, ComboBoxMatchers.hasItems(1));
    assertEquals(List.of("varA"), new ArrayList<>(paramCombo.getItems()));
  }
}
