package oogasalad.editor.view;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


import oogasalad.editor.model.data.object.event.ExecutorData;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

@ExtendWith(ApplicationExtension.class)
class ConditionsSectionBuilderTest {


  @Mock private Supplier<List<String>> mockConditionTypeSupplier;
  @Mock private Runnable mockAddGroupHandler;
  @Mock private IntConsumer mockRemoveGroupHandler;
  @Mock private AddConditionHandler mockAddConditionHandler;
  @Mock private RemoveConditionHandler mockRemoveConditionHandler;
  @Mock private EditConditionParamHandler mockEditConditionParamHandler;

  private ResourceBundle testBundle;
  private ConditionsSectionBuilder builder;
  private Pane root;


  @BeforeAll
  static void setupHeadlessMode() {

    if (System.getProperty("os.name", "").toLowerCase().startsWith("linux")) {
      System.setProperty("headless.geometry", "1600x1200-32");
    }
    System.setProperty("testfx.robot", "glass");
    System.setProperty("testfx.headless", "true");
    System.setProperty("prism.order", "sw");
    System.setProperty("prism.text", "t2k");
    System.setProperty("java.awt.headless", "true");
  }

  @Start
  void start(Stage stage) {

    root = new Pane();
    Scene scene = new Scene(root, 800, 600);
    stage.setScene(scene);
    stage.show();
  }


  @BeforeEach
  void setUp() {

    MockitoAnnotations.openMocks(this);


    testBundle = new ListResourceBundle() {
      @Override
      protected Object[][] getContents() {
        return new Object[][]{
            {"conditionsHeader", "Conditions Test"},
            {"addGroupButton", "Add Group Test"},
            {"removeGroupButton", "Remove Group Test"},
            {"addConditionButton", "Add Cond Test"},
            {"removeConditionButton", "Remove Cond Test"},
            {"parametersHeader", "Parameters Test"}
        };
      }
    };


    when(mockConditionTypeSupplier.get()).thenReturn(List.of("TypeA", "TypeB", "TypeC"));


    builder = new ConditionsSectionBuilder(
        testBundle,
        mockConditionTypeSupplier,
        mockAddGroupHandler,
        mockRemoveGroupHandler,
        mockAddConditionHandler,
        mockRemoveConditionHandler,
        mockEditConditionParamHandler
    );


    Platform.runLater(() -> {
      Node conditionsSection = builder.build();
      assertNotNull(conditionsSection, "Builder should return a non-null Node");
      root.getChildren().add(conditionsSection);
    });
    WaitForAsyncUtils.waitForFxEvents();
  }



  @Test
  void testBuildCreatesUIElements(FxRobot robot) {

    assertNotNull(robot.lookup("#addGroupButton").queryButton(), "Add Group button should exist.");
    assertNotNull(robot.lookup("#removeGroupButton").queryButton(), "Remove Group button should exist.");
    assertNotNull(robot.lookup("#conditionTypeComboBox").queryComboBox(), "Condition type ComboBox should exist.");
    assertNotNull(robot.lookup("#addConditionButton").queryButton(), "Add Condition button should exist.");
    assertNotNull(robot.lookup("#removeConditionButton").queryButton(), "Remove Condition button should exist.");
    assertNotNull(robot.lookup("#conditionsListView").queryListView(), "Conditions ListView should exist.");
    assertNotNull(robot.lookup("#conditionParametersPane").query(), "Condition Parameters Pane should exist.");


    ComboBox<String> comboBox = robot.lookup("#conditionTypeComboBox").queryComboBox();
    assertEquals(List.of("TypeA", "TypeB", "TypeC"), new ArrayList<>(comboBox.getItems()), "ComboBox should be populated from supplier.");
    assertEquals("Select Condition Type", comboBox.getPromptText());
  }

  @Test
  void testAddGroupButtonAction(FxRobot robot) {

    robot.clickOn("#addGroupButton");
    WaitForAsyncUtils.waitForFxEvents();


    verify(mockAddGroupHandler, times(1)).run();
  }

  @Test
  void testRemoveGroupButtonAction_NoSelection(FxRobot robot) {

    robot.clickOn("#removeGroupButton");
    WaitForAsyncUtils.waitForFxEvents();


    verify(mockRemoveGroupHandler, never()).accept(anyInt());
  }

  @Test
  void testRemoveGroupButtonAction_WithSelection(FxRobot robot) {



    ExecutorData data1 = new ExecutorData("CondG0", new HashMap<>(), new HashMap<>());
    ExecutorData data2 = new ExecutorData("CondG1", new HashMap<>(), new HashMap<>());
    List<List<ExecutorData>> initialData = List.of(List.of(data1), List.of(data2));
    Platform.runLater(() -> builder.updateConditionsListView(initialData));
    WaitForAsyncUtils.waitForFxEvents();


    ListView<ConditionDisplayItem> listView = robot.lookup("#conditionsListView").queryListView();
    Platform.runLater(() -> listView.getSelectionModel().select(1));
    WaitForAsyncUtils.waitForFxEvents();
    ConditionDisplayItem selected = listView.getSelectionModel().getSelectedItem();
    assertNotNull(selected, "Item should be selected");
    assertEquals(1, selected.getGroupIndex(), "Group 1 item should be selected");


    robot.clickOn("#removeGroupButton");
    WaitForAsyncUtils.waitForFxEvents();


    verify(mockRemoveGroupHandler, times(1)).accept(1);
  }


  @Test
  void testAddConditionButtonAction_NoTypeSelected(FxRobot robot) {

    robot.clickOn("#addConditionButton");
    WaitForAsyncUtils.waitForFxEvents();


    verify(mockAddConditionHandler, never()).handle(anyInt(), anyString());
  }

  @Test
  void testAddConditionButtonAction_TypeSelected_NoListSelection(FxRobot robot) {

    robot.clickOn("#conditionTypeComboBox");
    robot.clickOn("TypeB");
    WaitForAsyncUtils.waitForFxEvents();


    robot.clickOn("#addConditionButton");
    WaitForAsyncUtils.waitForFxEvents();


    verify(mockAddConditionHandler, times(1)).handle(0, "TypeB");


    ComboBox<String> comboBox = robot.lookup("#conditionTypeComboBox").queryComboBox();
    assertNull(comboBox.getSelectionModel().getSelectedItem(), "ComboBox selection should be cleared after adding.");
    assertEquals("Select Condition Type", comboBox.getPromptText());
  }

  @Test
  void testAddConditionButtonAction_TypeSelected_WithListSelection(FxRobot robot) {

    ExecutorData dataG0C0 = new ExecutorData("CondG0", new HashMap<>(), new HashMap<>());
    ExecutorData dataG1C0 = new ExecutorData("CondG1", new HashMap<>(), new HashMap<>());
    List<List<ExecutorData>> initialData = List.of(List.of(dataG0C0), List.of(dataG1C0));
    Platform.runLater(() -> builder.updateConditionsListView(initialData));
    WaitForAsyncUtils.waitForFxEvents();

    ListView<ConditionDisplayItem> listView = robot.lookup("#conditionsListView").queryListView();
    Platform.runLater(() -> listView.getSelectionModel().select(1));
    WaitForAsyncUtils.waitForFxEvents();
    ConditionDisplayItem selected = listView.getSelectionModel().getSelectedItem();
    assertNotNull(selected, "Item should be selected");
    assertEquals(1, selected.getGroupIndex(), "Item from group 1 should be selected");


    robot.clickOn("#conditionTypeComboBox");
    robot.clickOn("TypeA");
    WaitForAsyncUtils.waitForFxEvents();


    robot.clickOn("#addConditionButton");
    WaitForAsyncUtils.waitForFxEvents();


    verify(mockAddConditionHandler, times(1)).handle(1, "TypeA");
  }


  @Test
  void testRemoveConditionButtonAction_NoSelection(FxRobot robot) {

    robot.clickOn("#removeConditionButton");
    WaitForAsyncUtils.waitForFxEvents();


    verify(mockRemoveConditionHandler, never()).handle(anyInt(), anyInt());
  }

  @Test
  void testRemoveConditionButtonAction_WithSelection(FxRobot robot) {

    ExecutorData dataG0C0 = new ExecutorData("G0C0", new HashMap<>(), new HashMap<>());
    ExecutorData dataG1C0 = new ExecutorData("G1C0", new HashMap<>(), new HashMap<>());
    ExecutorData dataG1C1 = new ExecutorData("G1C1", new HashMap<>(), new HashMap<>());
    List<List<ExecutorData>> initialData = List.of(
        List.of(dataG0C0),
        List.of(dataG1C0, dataG1C1)
    );
    Platform.runLater(() -> builder.updateConditionsListView(initialData));
    WaitForAsyncUtils.waitForFxEvents();


    ListView<ConditionDisplayItem> listView = robot.lookup("#conditionsListView").queryListView();
    Platform.runLater(() -> listView.getSelectionModel().select(2));
    WaitForAsyncUtils.waitForFxEvents();

    ConditionDisplayItem selected = listView.getSelectionModel().getSelectedItem();
    assertNotNull(selected, "Item should be selected");
    assertEquals(1, selected.getGroupIndex(), "Selected item should be group 1");
    assertEquals(1, selected.getConditionIndex(), "Selected item should be condition 1");


    robot.clickOn("#removeConditionButton");
    WaitForAsyncUtils.waitForFxEvents();


    verify(mockRemoveConditionHandler, times(1)).handle(1, 1);
  }

  @Test
  void testUpdateConditionsListView_PopulatesList(FxRobot robot) {

    ExecutorData data1 = new ExecutorData("CondG0C0", new HashMap<>(), new HashMap<>());
    ExecutorData data2 = new ExecutorData("CondG1C0", new HashMap<>(), new HashMap<>());
    List<List<ExecutorData>> testData = List.of(
        List.of(data1),
        List.of(data2)
    );


    Platform.runLater(() -> builder.updateConditionsListView(testData));
    WaitForAsyncUtils.waitForFxEvents();


    ListView<ConditionDisplayItem> listView = robot.lookup("#conditionsListView").queryListView();
    assertEquals(2, listView.getItems().size(), "ListView should contain 2 items.");
    assertEquals("Group 0 [0]: CondG0C0", listView.getItems().get(0).toString());
    assertEquals("Group 1 [0]: CondG1C0", listView.getItems().get(1).toString());


    Pane paramsPane = (Pane) robot.lookup("#conditionParametersPane").query();
    assertTrue(paramsPane.getChildren().isEmpty(), "Parameters pane should be empty after list update without selection.");
  }



  @Test
  void testParameterEditing_StringParam(FxRobot robot) {

    Map<String, String> strParams = new HashMap<>();
    strParams.put("name", "InitialName");
    Map<String, Double> dblParams = new HashMap<>();
    dblParams.put("value", 10.5);
    ExecutorData dataWithParams = new ExecutorData("ParamCond", strParams, dblParams);
    List<List<ExecutorData>> initialData = List.of(List.of(dataWithParams));


    Platform.runLater(() -> builder.updateConditionsListView(initialData));
    WaitForAsyncUtils.waitForFxEvents();
    ListView<ConditionDisplayItem> listView = robot.lookup("#conditionsListView").queryListView();
    Platform.runLater(() -> listView.getSelectionModel().select(0));
    WaitForAsyncUtils.waitForFxEvents();


    TextField nameField = robot.lookup(".text-field").nth(0).query();
    assertEquals("InitialName", nameField.getText());


    robot.clickOn(nameField);
    robot.eraseText(nameField.getText().length());
    robot.write("NewName");
    robot.push(KeyCode.ENTER);
    WaitForAsyncUtils.waitForFxEvents();


    ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);
    verify(mockEditConditionParamHandler, times(1)).handle(
        eq(0),
        eq(0),
        eq("name"),
        valueCaptor.capture()
    );
    assertEquals("NewName", valueCaptor.getValue(), "Handler should be called with the new string value.");
  }

  @Test
  void testParameterEditing_DoubleParam_Valid(FxRobot robot) {

    Map<String, String> strParams = new HashMap<>();
    strParams.put("name", "InitialName");
    Map<String, Double> dblParams = new HashMap<>();
    dblParams.put("value", 10.5);
    ExecutorData dataWithParams = new ExecutorData("ParamCond", strParams, dblParams);
    List<List<ExecutorData>> initialData = List.of(List.of(dataWithParams));


    Platform.runLater(() -> builder.updateConditionsListView(initialData));
    WaitForAsyncUtils.waitForFxEvents();
    ListView<ConditionDisplayItem> listView = robot.lookup("#conditionsListView").queryListView();
    Platform.runLater(() -> listView.getSelectionModel().select(0));
    WaitForAsyncUtils.waitForFxEvents();


    TextField valueField = robot.lookup(".text-field").nth(1).query();
    assertEquals("10.5", valueField.getText());


    robot.clickOn(valueField);
    robot.eraseText(valueField.getText().length());
    robot.write("25.75");
    robot.push(KeyCode.ENTER);
    WaitForAsyncUtils.waitForFxEvents();


    ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);
    verify(mockEditConditionParamHandler, times(1)).handle(
        eq(0),
        eq(0),
        eq("value"),
        valueCaptor.capture()
    );
    assertTrue(valueCaptor.getValue() instanceof Double, "Captured value should be Double");
    assertEquals(25.75, (Double) valueCaptor.getValue(), 0.001, "Handler should be called with the new double value.");
  }

  @Test
  void testParameterEditing_DoubleParam_Invalid(FxRobot robot) {

    Map<String, Double> dblParams = new HashMap<>();
    dblParams.put("value", 10.5);
    ExecutorData dataWithParams = new ExecutorData("ParamCond", new HashMap<>(), dblParams);
    List<List<ExecutorData>> initialData = List.of(List.of(dataWithParams));


    Platform.runLater(() -> builder.updateConditionsListView(initialData));
    WaitForAsyncUtils.waitForFxEvents();
    ListView<ConditionDisplayItem> listView = robot.lookup("#conditionsListView").queryListView();
    Platform.runLater(() -> listView.getSelectionModel().select(0));
    WaitForAsyncUtils.waitForFxEvents();


    TextField valueField = robot.lookup(".text-field").query();
    assertEquals("10.5", valueField.getText());


    robot.clickOn(valueField);
    robot.eraseText(valueField.getText().length());
    robot.write("invalid-double");
    robot.push(KeyCode.ENTER);
    WaitForAsyncUtils.waitForFxEvents();


    verify(mockEditConditionParamHandler, times(0)).handle(
        anyInt(), anyInt(), eq("value"), any(Object.class)
    );


    assertEquals("10.5", valueField.getText(), "Field should reset to original value on invalid double input via Enter.");
  }

  @Test
  void testConstructor_NullArgs() {

    MockitoAnnotations.openMocks(this);

    when(mockConditionTypeSupplier.get()).thenReturn(List.of("A"));



    assertThrows(NullPointerException.class, () -> new ConditionsSectionBuilder(null, mockConditionTypeSupplier, mockAddGroupHandler, mockRemoveGroupHandler, mockAddConditionHandler, mockRemoveConditionHandler, mockEditConditionParamHandler));
    assertThrows(NullPointerException.class, () -> new ConditionsSectionBuilder(testBundle, null, mockAddGroupHandler, mockRemoveGroupHandler, mockAddConditionHandler, mockRemoveConditionHandler, mockEditConditionParamHandler));
    assertThrows(NullPointerException.class, () -> new ConditionsSectionBuilder(testBundle, mockConditionTypeSupplier, null, mockRemoveGroupHandler, mockAddConditionHandler, mockRemoveConditionHandler, mockEditConditionParamHandler));
    assertThrows(NullPointerException.class, () -> new ConditionsSectionBuilder(testBundle, mockConditionTypeSupplier, mockAddGroupHandler, null, mockAddConditionHandler, mockRemoveConditionHandler, mockEditConditionParamHandler));
    assertThrows(NullPointerException.class, () -> new ConditionsSectionBuilder(testBundle, mockConditionTypeSupplier, mockAddGroupHandler, mockRemoveGroupHandler, null, mockRemoveConditionHandler, mockEditConditionParamHandler));
    assertThrows(NullPointerException.class, () -> new ConditionsSectionBuilder(testBundle, mockConditionTypeSupplier, mockAddGroupHandler, mockRemoveGroupHandler, mockAddConditionHandler, null, mockEditConditionParamHandler));
    assertThrows(NullPointerException.class, () -> new ConditionsSectionBuilder(testBundle, mockConditionTypeSupplier, mockAddGroupHandler, mockRemoveGroupHandler, mockAddConditionHandler, mockRemoveConditionHandler, null));
  }
}