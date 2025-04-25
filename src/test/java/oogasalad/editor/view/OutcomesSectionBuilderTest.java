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


import oogasalad.editor.model.data.object.DynamicVariable;
import oogasalad.editor.model.data.object.event.ExecutorData;

import oogasalad.editor.view.eventui.AddOutcomeHandler;
import oogasalad.editor.view.eventui.EditOutcomeParamHandler;
import oogasalad.editor.view.eventui.OutcomeDisplayItem;
import oogasalad.editor.view.eventui.OutcomesSectionBuilder;
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
class OutcomesSectionBuilderTest {


  @Mock private Supplier<List<String>> mockOutcomeTypeSupplier;
  @Mock private Supplier<List<DynamicVariable>> mockDynamicVariableSupplier;
  @Mock private AddOutcomeHandler mockAddOutcomeHandler;
  @Mock private IntConsumer mockRemoveOutcomeHandler;
  @Mock private Runnable mockCreateParameterHandler;
  @Mock private EditOutcomeParamHandler mockEditOutcomeParamHandler;

  private ResourceBundle testBundle;
  private OutcomesSectionBuilder builder;
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
            {"outcomesHeader", "Outcomes Test"},
            {"parameterLabel", "Param Test"},
            {"createParamButton", "+ Test"},
            {"addOutcomeButton", "Add Outcome Test"},
            {"removeOutcomeButton", "Remove Outcome Test"},
            {"executorParametersHeader", "Exec Params Test"}
        };
      }
    };


    when(mockOutcomeTypeSupplier.get()).thenReturn(List.of("OutcomeA", "OutcomeB"));


    when(mockDynamicVariableSupplier.get()).thenReturn(List.of(
        new DynamicVariable("Var1", "String", "abc", "Desc1"),
        new DynamicVariable("Var2", "Double", "123.0", "Desc2")
    ));


    builder = new OutcomesSectionBuilder(
        testBundle,
        mockOutcomeTypeSupplier,
        mockDynamicVariableSupplier,
        mockAddOutcomeHandler,
        mockRemoveOutcomeHandler,
        mockCreateParameterHandler,
        mockEditOutcomeParamHandler
    );


    Platform.runLater(() -> {
      Node outcomesSection = builder.build();
      assertNotNull(outcomesSection, "Builder should return a non-null Node");
      root.getChildren().add(outcomesSection);
    });
    WaitForAsyncUtils.waitForFxEvents();
  }



  @Test
  void testBuildCreatesUIElements(FxRobot robot) {

    assertNotNull(robot.lookup("#outcomeTypeComboBox").queryComboBox(), "Outcome type ComboBox should exist.");
    assertNotNull(robot.lookup("#addOutcomeButton").queryButton(), "Add Outcome button should exist.");
    assertNotNull(robot.lookup("#removeOutcomeButton").queryButton(), "Remove Outcome button should exist.");
    assertNotNull(robot.lookup("#dynamicVariableComboBox").queryComboBox(), "Dynamic Variable ComboBox should exist.");
    assertNotNull(robot.lookup("#addVariableButton").queryButton(), "Add Variable button (+) should exist.");
    assertNotNull(robot.lookup("#outcomesListView").queryListView(), "Outcomes ListView should exist.");
    assertNotNull(robot.lookup("#outcomeParametersPane").query(), "Outcome Parameters Pane should exist.");


    ComboBox<String> outcomeCombo = robot.lookup("#outcomeTypeComboBox").queryComboBox();
    assertEquals(List.of("OutcomeA", "OutcomeB"), new ArrayList<>(outcomeCombo.getItems()), "Outcome ComboBox should be populated.");
    assertEquals("Select Outcome Type", outcomeCombo.getPromptText());


    ComboBox<String> dynVarCombo = robot.lookup("#dynamicVariableComboBox").queryComboBox();
    assertTrue(dynVarCombo.getItems().isEmpty(), "Dynamic Variable ComboBox should initially be empty.");
    assertEquals("Select Variable Parameter", dynVarCombo.getPromptText());


    Button addVarButton = robot.lookup("#addVariableButton").queryButton();
    assertEquals("+", addVarButton.getText(), "Add Variable button text should be '+'.");
  }

  @Test
  void testUpdateDynamicVariableComboBox(FxRobot robot) {

    Platform.runLater(() -> builder.updateDynamicVariableComboBox());
    WaitForAsyncUtils.waitForFxEvents();


    verify(mockDynamicVariableSupplier, atLeastOnce()).get();

    ComboBox<String> dynVarCombo = robot.lookup("#dynamicVariableComboBox").queryComboBox();
    assertEquals(List.of("Var1", "Var2"), new ArrayList<>(dynVarCombo.getItems()), "Dynamic Variable ComboBox should be updated.");
    assertEquals("Select Variable Parameter", dynVarCombo.getPromptText());
    assertNull(dynVarCombo.getSelectionModel().getSelectedItem(),"Selection should be cleared");
  }

  @Test
  void testUpdateDynamicVariableComboBox_EmptyOrNull(FxRobot robot) {

    when(mockDynamicVariableSupplier.get()).thenReturn(Collections.emptyList());
    Platform.runLater(() -> builder.updateDynamicVariableComboBox());
    WaitForAsyncUtils.waitForFxEvents();
    ComboBox<String> dynVarCombo = robot.lookup("#dynamicVariableComboBox").queryComboBox();
    assertTrue(dynVarCombo.getItems().isEmpty(), "Dynamic Variable ComboBox should be empty for empty list.");
    assertEquals("Select Variable Parameter", dynVarCombo.getPromptText());


    when(mockDynamicVariableSupplier.get()).thenReturn(null);
    Platform.runLater(() -> builder.updateDynamicVariableComboBox());
    WaitForAsyncUtils.waitForFxEvents();
    dynVarCombo = robot.lookup("#dynamicVariableComboBox").queryComboBox();
    assertTrue(dynVarCombo.getItems().isEmpty(), "Dynamic Variable ComboBox should be empty for null list.");
    assertEquals("Select Variable Parameter", dynVarCombo.getPromptText());
  }

  @Test
  void testAddOutcomeButtonAction_NoTypeSelected(FxRobot robot) {

    robot.clickOn("#addOutcomeButton");
    WaitForAsyncUtils.waitForFxEvents();


    verify(mockAddOutcomeHandler, never()).handle(anyString());
  }

  @Test
  void testAddOutcomeButtonAction_TypeSelected(FxRobot robot) {

    robot.clickOn("#outcomeTypeComboBox");
    robot.clickOn("OutcomeA");
    WaitForAsyncUtils.waitForFxEvents();


    robot.clickOn("#addOutcomeButton");
    WaitForAsyncUtils.waitForFxEvents();


    verify(mockAddOutcomeHandler, times(1)).handle("OutcomeA");
    ComboBox<String> comboBox = robot.lookup("#outcomeTypeComboBox").queryComboBox();
    assertNull(comboBox.getSelectionModel().getSelectedItem(), "ComboBox selection should be cleared after adding.");
    assertEquals("Select Outcome Type", comboBox.getPromptText());
  }

  @Test
  void testRemoveOutcomeButtonAction_NoSelection(FxRobot robot) {

    robot.clickOn("#removeOutcomeButton");
    WaitForAsyncUtils.waitForFxEvents();


    verify(mockRemoveOutcomeHandler, never()).accept(anyInt());
  }

  @Test
  void testRemoveOutcomeButtonAction_WithSelection(FxRobot robot) {



    ExecutorData data1 = new ExecutorData("Exec1", new HashMap<>(), new HashMap<>());
    ExecutorData data2 = new ExecutorData("Exec2", new HashMap<>(), new HashMap<>());
    List<ExecutorData> initialData = List.of(data1, data2);
    Platform.runLater(() -> builder.updateOutcomesListView(initialData));
    WaitForAsyncUtils.waitForFxEvents();


    ListView<OutcomeDisplayItem> listView = robot.lookup("#outcomesListView").queryListView();
    Platform.runLater(() -> listView.getSelectionModel().select(1));
    WaitForAsyncUtils.waitForFxEvents();
    OutcomeDisplayItem selected = listView.getSelectionModel().getSelectedItem();
    assertNotNull(selected, "Item should be selected");
    assertEquals(1, selected.getIndex(), "Index 1 should be selected");


    robot.clickOn("#removeOutcomeButton");
    WaitForAsyncUtils.waitForFxEvents();


    verify(mockRemoveOutcomeHandler, times(1)).accept(1);
  }

  @Test
  void testCreateVariableButtonAction(FxRobot robot) {

    robot.clickOn("#addVariableButton");
    WaitForAsyncUtils.waitForFxEvents();


    verify(mockCreateParameterHandler, times(1)).run();
  }


  @Test
  void testUpdateOutcomesListView_PopulatesList(FxRobot robot) {

    ExecutorData data1 = new ExecutorData("Out1", new HashMap<>(), new HashMap<>());
    ExecutorData data2 = new ExecutorData("Out2", Map.of("p1", "v1"), Map.of("p2", 2.0));
    List<ExecutorData> testData = List.of(data1, data2);


    Platform.runLater(() -> builder.updateOutcomesListView(testData));
    WaitForAsyncUtils.waitForFxEvents();


    ListView<OutcomeDisplayItem> listView = robot.lookup("#outcomesListView").queryListView();
    assertEquals(2, listView.getItems().size(), "ListView should contain 2 items.");
    assertEquals("[0]: Out1", listView.getItems().get(0).toString());
    assertEquals("[1]: Out2", listView.getItems().get(1).toString());


    Pane paramsPane = (Pane) robot.lookup("#outcomeParametersPane").query();
    assertTrue(paramsPane.getChildren().isEmpty(), "Parameters pane should be empty after list update without selection.");
  }

  @Test
  void testUpdateOutcomesListView_NullOrEmpty(FxRobot robot) {

    Platform.runLater(() -> builder.updateOutcomesListView(null));
    WaitForAsyncUtils.waitForFxEvents();
    ListView<OutcomeDisplayItem> listView = robot.lookup("#outcomesListView").queryListView();
    assertTrue(listView.getItems().isEmpty(), "ListView should be empty for null input.");


    Platform.runLater(() -> builder.updateOutcomesListView(new ArrayList<>()));
    WaitForAsyncUtils.waitForFxEvents();
    assertTrue(listView.getItems().isEmpty(), "ListView should be empty for empty list input.");
  }

  @Test
  void testParameterEditing_StringParam(FxRobot robot) {

    Map<String, String> strParams = new HashMap<>();
    strParams.put("name", "InitialName");
    Map<String, Double> dblParams = new HashMap<>();
    dblParams.put("value", 10.5);
    ExecutorData dataWithParams = new ExecutorData("ParamOut", strParams, dblParams);
    List<ExecutorData> initialData = List.of(dataWithParams);


    Platform.runLater(() -> builder.updateOutcomesListView(initialData));
    WaitForAsyncUtils.waitForFxEvents();


    ListView<OutcomeDisplayItem> listView = robot.lookup("#outcomesListView").queryListView();
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
    verify(mockEditOutcomeParamHandler, times(1)).handle(
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
    ExecutorData dataWithParams = new ExecutorData("ParamOut", strParams, dblParams);
    List<ExecutorData> initialData = List.of(dataWithParams);


    Platform.runLater(() -> builder.updateOutcomesListView(initialData));
    WaitForAsyncUtils.waitForFxEvents();


    ListView<OutcomeDisplayItem> listView = robot.lookup("#outcomesListView").queryListView();
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
    verify(mockEditOutcomeParamHandler, times(1)).handle(
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
    ExecutorData dataWithParams = new ExecutorData("ParamOut", new HashMap<>(), dblParams);
    List<ExecutorData> initialData = List.of(dataWithParams);


    Platform.runLater(() -> builder.updateOutcomesListView(initialData));
    WaitForAsyncUtils.waitForFxEvents();


    ListView<OutcomeDisplayItem> listView = robot.lookup("#outcomesListView").queryListView();
    Platform.runLater(() -> listView.getSelectionModel().select(0));
    WaitForAsyncUtils.waitForFxEvents();


    TextField valueField = robot.lookup(".text-field").query();
    assertEquals("10.5", valueField.getText());


    robot.clickOn(valueField);
    robot.eraseText(valueField.getText().length());
    robot.write("invalid-double");
    robot.push(KeyCode.ENTER);
    WaitForAsyncUtils.waitForFxEvents();


    verify(mockEditOutcomeParamHandler, times(0)).handle(
        anyInt(), eq("value"), any(Object.class)
    );


    assertEquals("10.5", valueField.getText(), "Field should reset to original value on invalid double input via Enter.");
  }


  @Test
  void testConstructor_NullArgs() {

    MockitoAnnotations.openMocks(this);


    when(mockOutcomeTypeSupplier.get()).thenReturn(List.of("A"));
    when(mockDynamicVariableSupplier.get()).thenReturn(List.of(new DynamicVariable("V","","", "")));






    assertThrows(NullPointerException.class, () -> new OutcomesSectionBuilder(null, mockOutcomeTypeSupplier, mockDynamicVariableSupplier, mockAddOutcomeHandler, mockRemoveOutcomeHandler, mockCreateParameterHandler, mockEditOutcomeParamHandler));
    assertThrows(NullPointerException.class, () -> new OutcomesSectionBuilder(testBundle, null, mockDynamicVariableSupplier, mockAddOutcomeHandler, mockRemoveOutcomeHandler, mockCreateParameterHandler, mockEditOutcomeParamHandler));
    assertThrows(NullPointerException.class, () -> new OutcomesSectionBuilder(testBundle, mockOutcomeTypeSupplier, null, mockAddOutcomeHandler, mockRemoveOutcomeHandler, mockCreateParameterHandler, mockEditOutcomeParamHandler));
    assertThrows(NullPointerException.class, () -> new OutcomesSectionBuilder(testBundle, mockOutcomeTypeSupplier, mockDynamicVariableSupplier, null, mockRemoveOutcomeHandler, mockCreateParameterHandler, mockEditOutcomeParamHandler));
    assertThrows(NullPointerException.class, () -> new OutcomesSectionBuilder(testBundle, mockOutcomeTypeSupplier, mockDynamicVariableSupplier, mockAddOutcomeHandler, null, mockCreateParameterHandler, mockEditOutcomeParamHandler));
    assertThrows(NullPointerException.class, () -> new OutcomesSectionBuilder(testBundle, mockOutcomeTypeSupplier, mockDynamicVariableSupplier, mockAddOutcomeHandler, mockRemoveOutcomeHandler, null, mockEditOutcomeParamHandler));
    assertThrows(NullPointerException.class, () -> new OutcomesSectionBuilder(testBundle, mockOutcomeTypeSupplier, mockDynamicVariableSupplier, mockAddOutcomeHandler, mockRemoveOutcomeHandler, mockCreateParameterHandler, null));
  }
}