package oogasalad.editor.view;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;

import java.util.*;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import oogasalad.editor.model.data.object.event.ExecutorData;
import oogasalad.editor.view.eventui.ConditionDisplayItem;
import oogasalad.editor.view.eventui.ConditionsSectionBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.base.NodeMatchers;

import org.testfx.matcher.control.ComboBoxMatchers;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.matcher.control.ListViewMatchers;
import org.testfx.util.WaitForAsyncUtils;

import oogasalad.editor.view.eventui.EditConditionParamHandler;
import oogasalad.editor.view.eventui.AddConditionHandler;
import oogasalad.editor.view.eventui.RemoveConditionHandler;


@ExtendWith(ApplicationExtension.class)
class ConditionsSectionBuilderTest {

  @Mock
  private ResourceBundle mockUiBundle;
  @Mock
  private Supplier<List<String>> mockConditionTypeSupplier;
  @Mock
  private Runnable mockAddGroupHandler;
  @Mock
  private IntConsumer mockRemoveGroupHandler;
  @Mock
  private AddConditionHandler mockAddConditionHandler;
  @Mock
  private RemoveConditionHandler mockRemoveConditionHandler;
  @Mock
  private EditConditionParamHandler mockEditConditionParamHandler; // Keep mock

  private ConditionsSectionBuilder conditionsSectionBuilder;
  private VBox rootNode;

  private static Properties testProperties;

  @BeforeAll
  static void loadTestProperties() {
    testProperties = new Properties();
    testProperties.setProperty("id.sectionPane", "conditionsSectionPane");
    testProperties.setProperty("style.inputSubSection", "input-sub-section");
    testProperties.setProperty("key.conditionsHeader", "Conditions");
    testProperties.setProperty("id.groupButtonRow", "conditionGroupButtonRow");
    testProperties.setProperty("style.buttonBox", "button-box");
    testProperties.setProperty("key.addGroupButton", "Add Group");
    testProperties.setProperty("id.addGroupButton", "addGroupButton");
    testProperties.setProperty("key.removeGroupButton", "Remove Group");
    testProperties.setProperty("id.removeGroupButton", "removeGroupButton");
    testProperties.setProperty("style.removeButton", "remove-button");
    testProperties.setProperty("id.conditionSelectionRow", "conditionSelectionRow");
    testProperties.setProperty("style.selectionBox", "selection-box");
    testProperties.setProperty("key.conditionGroupLabel", "Target Group:");
    testProperties.setProperty("id.conditionGroupComboBox", "conditionGroupComboBox");
    testProperties.setProperty("key.promptSelectGroup", "Select Group");
    testProperties.setProperty("id.conditionTypeComboBox", "conditionTypeComboBox");
    testProperties.setProperty("key.promptSelectCondition", "Select Condition Type");
    testProperties.setProperty("key.addConditionButton", "Add Condition");
    testProperties.setProperty("id.addConditionButton", "addConditionButton");
    testProperties.setProperty("key.removeConditionButton", "Remove Condition");
    testProperties.setProperty("id.removeConditionButton", "removeConditionButton");
    testProperties.setProperty("id.conditionsListView", "conditionsListView");
    testProperties.setProperty("style.dataListView", "data-list-view");
    testProperties.setProperty("id.parametersContainer", "conditionParametersContainer");
    testProperties.setProperty("key.parametersHeader", "Parameters");
    testProperties.setProperty("id.parametersPane", "conditionParametersPane");
    testProperties.setProperty("id.parametersScrollPane", "conditionParametersScrollPane");
    testProperties.setProperty("style.sectionHeader", "section-header");
    testProperties.setProperty("style.actionButton", "action-button");
    testProperties.setProperty("id.parametersGrid", "parametersGrid");
    testProperties.setProperty("style.parameterGrid", "parameter-grid");
    testProperties.setProperty("param.baseKey", ".param");
    testProperties.setProperty("param.countSuffix", ".count");
    testProperties.setProperty("param.nameSuffix", ".name");
    testProperties.setProperty("param.typeSuffix", ".type");
    testProperties.setProperty("param.descSuffix", ".description");
    testProperties.setProperty("param.defaultSuffix", ".defaultValue");
    testProperties.setProperty("param.typeIdString", "String");
    testProperties.setProperty("param.typeIdDouble", "Double");
    testProperties.setProperty("param.typeIdInteger", "Integer");
    testProperties.setProperty("param.typeIdBoolean", "Boolean");
    testProperties.setProperty("param.typeIdDropdownPrefix", "Dropdown:");
    testProperties.setProperty("key.paramTypeString", "(String)");
    testProperties.setProperty("key.paramTypeDouble", "(Double)");
    testProperties.setProperty("key.paramTypeInteger", "(Integer)");
    testProperties.setProperty("key.paramTypeBoolean", "(Boolean)");
    testProperties.setProperty("key.paramTypeDropdown", "(Choice)");
    testProperties.setProperty("key.warnInvalidNumber", "Invalid number format for %s definition.");
    testProperties.setProperty("key.warnMissingParameter", "Parameter definition missing for %s.");
    testProperties.setProperty("key.errorParameterLoad", "Error loading parameters for %s.");
    testProperties.setProperty("key.errorParameterDefinition", "Missing definition for param index %d of %s.");
    testProperties.setProperty("key.errorParameterParse", "Error parsing value '%s' for param '%s' (%s).");
    testProperties.setProperty("key.labelError", "Error");
    testProperties.setProperty("key.paramTypeError", "(Error)");
    testProperties.setProperty("key.warnInvalidNumericInput", "Invalid %s value: %s");
    testProperties.setProperty("TEST_CONDITION.param.count", "2");
    testProperties.setProperty("TEST_CONDITION.param.1.name", "BooleanParam");
    testProperties.setProperty("TEST_CONDITION.param.1.type", "Boolean");
    testProperties.setProperty("TEST_CONDITION.param.1.description", "A boolean parameter.");
    testProperties.setProperty("TEST_CONDITION.param.1.defaultValue", "false");
    testProperties.setProperty("TEST_CONDITION.param.2.name", "DropdownParam");
    testProperties.setProperty("TEST_CONDITION.param.2.type", "Dropdown:OptionA,OptionB");
    testProperties.setProperty("TEST_CONDITION.param.2.description", "A choice parameter.");
    testProperties.setProperty("TEST_CONDITION.param.2.defaultValue", "OptionA");
    testProperties.setProperty("ANOTHER_CONDITION.param.count", "0");
  }

  @Start
  public void start(Stage stage) {
    MockitoAnnotations.openMocks(this);

    when(mockUiBundle.getString(anyString())).thenAnswer(invocation -> {
      String key = invocation.getArgument(0);
      return testProperties.getProperty("key." + key, key);
    });
    when(mockConditionTypeSupplier.get()).thenReturn(Arrays.asList("TEST_CONDITION", "ANOTHER_CONDITION"));

    conditionsSectionBuilder = new ConditionsSectionBuilder(
        mockUiBundle, mockConditionTypeSupplier, mockAddGroupHandler,
        mockRemoveGroupHandler, mockAddConditionHandler, mockRemoveConditionHandler,
        mockEditConditionParamHandler
    );

    try {
      java.lang.reflect.Field propsField = ConditionsSectionBuilder.class.getDeclaredField("localProps");
      propsField.setAccessible(true);
      propsField.set(conditionsSectionBuilder, testProperties);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      fail("Failed to inject test properties", e);
    }

    rootNode = (VBox) conditionsSectionBuilder.build();
    Scene scene = new Scene(rootNode, 400, 600);
    stage.setScene(scene);
    stage.show();
  }

  @Test
  void build_createsMainVBoxWithCorrectIdAndStyle(FxRobot robot) {
    verifyThat("#conditionsSectionPane", NodeMatchers.isVisible());
    assertTrue(rootNode.getStyleClass().contains("input-sub-section"));
  }

  @Test
  void build_addsHeaderLabel(FxRobot robot) {
    verifyThat("#conditionsSectionPane .label", LabeledMatchers.hasText("Conditions"));
    Label header = robot.lookup("#conditionsSectionPane .label").match(node -> node instanceof Label && ((Label) node).getText().equals("Conditions")).queryAs(Label.class);
    assertTrue(header.getStyleClass().contains("section-header"));
  }

  @Test
  void build_populatesConditionTypeComboBox(FxRobot robot) {
    verifyThat("#conditionTypeComboBox", ComboBoxMatchers.hasItems(2));
    ComboBox<String> combo = robot.lookup("#conditionTypeComboBox").queryComboBox();
    assertEquals(Arrays.asList("TEST_CONDITION", "ANOTHER_CONDITION"), combo.getItems());
  }


  @Test
  void updateConditionsListView_populatesListViewAndGroupComboBox(FxRobot robot) {
    ExecutorData data1_0 = new ExecutorData("TEST_CONDITION", new HashMap<>(), new HashMap<>());
    ExecutorData data1_1 = new ExecutorData("ANOTHER_CONDITION", new HashMap<>(), new HashMap<>());
    ExecutorData data2_0 = new ExecutorData("TEST_CONDITION", new HashMap<>(), new HashMap<>());

    List<List<ExecutorData>> conditions = List.of(
        List.of(data1_0, data1_1),
        List.of(data2_0)
    );

    robot.interact(() -> conditionsSectionBuilder.updateConditionsListView(conditions));
    WaitForAsyncUtils.waitForFxEvents();

    verifyThat("#conditionsListView", ListViewMatchers.hasItems(3));
    verifyThat("#conditionsListView", (ListView<ConditionDisplayItem> lv) -> lv.getItems().get(0).toString().equals("Group 0 [0]: TEST_CONDITION"));
    verifyThat("#conditionsListView", (ListView<ConditionDisplayItem> lv) -> lv.getItems().get(1).toString().equals("Group 0 [1]: ANOTHER_CONDITION"));
    verifyThat("#conditionsListView", (ListView<ConditionDisplayItem> lv) -> lv.getItems().get(2).toString().equals("Group 1 [0]: TEST_CONDITION"));

    verifyThat("#conditionGroupComboBox", ComboBoxMatchers.hasItems(2));
    ComboBox<Integer> combo = robot.lookup("#conditionGroupComboBox").queryComboBox();
    assertEquals(Arrays.asList(0, 1), combo.getItems());
  }

  @Test
  void updateConditionsListView_handlesNullInput(FxRobot robot) {
    ExecutorData data1_0 = new ExecutorData("TEST_CONDITION", new HashMap<>(), new HashMap<>());
    List<List<ExecutorData>> conditions = List.of(List.of(data1_0));

    robot.interact(() -> conditionsSectionBuilder.updateConditionsListView(conditions));
    WaitForAsyncUtils.waitForFxEvents();
    verifyThat("#conditionsListView", ListViewMatchers.hasItems(1));
    verifyThat("#conditionGroupComboBox", ComboBoxMatchers.hasItems(1));


    robot.interact(() -> conditionsSectionBuilder.updateConditionsListView(null));
    WaitForAsyncUtils.waitForFxEvents();
    verifyThat("#conditionsListView", ListViewMatchers.hasItems(0));
    verifyThat("#conditionGroupComboBox", ComboBoxMatchers.hasItems(0));
  }

  @Test
  void addGroupButton_triggersHandler(FxRobot robot) {
    robot.clickOn("#addGroupButton");
    verify(mockAddGroupHandler).run();
  }


  @Test
  void removeGroupButton_triggersHandlerWhenSelected(FxRobot robot) {
    ExecutorData data = new ExecutorData("TEST_CONDITION", new HashMap<>(), new HashMap<>());
    robot.interact(() -> conditionsSectionBuilder.updateConditionsListView(List.of(List.of(data))));
    WaitForAsyncUtils.waitForFxEvents();

    robot.clickOn("Group 0 [0]: TEST_CONDITION");
    WaitForAsyncUtils.waitForFxEvents();

    robot.clickOn("#removeGroupButton");
    verify(mockRemoveGroupHandler).accept(0);
  }

  @Test
  void removeGroupButton_doesNothingIfNotSelected(FxRobot robot) {
    ExecutorData data = new ExecutorData("TEST_CONDITION", new HashMap<>(), new HashMap<>());
    robot.interact(() -> conditionsSectionBuilder.updateConditionsListView(List.of(List.of(data))));
    WaitForAsyncUtils.waitForFxEvents();

    robot.interact(() -> robot.lookup("#conditionsListView").queryListView().getSelectionModel().clearSelection());
    robot.clickOn("#removeGroupButton");

    verify(mockRemoveGroupHandler, never()).accept(anyInt());
  }


  @Test
  void addConditionButton_triggersHandler(FxRobot robot) {
    robot.interact(() -> conditionsSectionBuilder.updateConditionsListView(List.of(List.of())));
    WaitForAsyncUtils.waitForFxEvents();

    robot.clickOn("#conditionGroupComboBox").clickOn("0");
    WaitForAsyncUtils.waitForFxEvents();
    robot.clickOn("#conditionTypeComboBox").clickOn("TEST_CONDITION");
    WaitForAsyncUtils.waitForFxEvents();

    robot.clickOn("#addConditionButton");
    verify(mockAddConditionHandler).handle(0, "TEST_CONDITION");
  }


  @Test
  void removeConditionButton_triggersHandlerWhenSelected(FxRobot robot) {
    ExecutorData data = new ExecutorData("TEST_CONDITION", new HashMap<>(), new HashMap<>());
    ExecutorData data2 = new ExecutorData("ANOTHER_CONDITION", new HashMap<>(), new HashMap<>());
    robot.interact(() -> conditionsSectionBuilder.updateConditionsListView(List.of(List.of(data, data2))));
    WaitForAsyncUtils.waitForFxEvents();

    robot.clickOn("Group 0 [1]: ANOTHER_CONDITION");
    WaitForAsyncUtils.waitForFxEvents();

    robot.clickOn("#removeConditionButton");
    verify(mockRemoveConditionHandler).handle(0, 1);
  }

  @Test
  void removeConditionButton_doesNothingIfNotSelected(FxRobot robot) {
    ExecutorData data = new ExecutorData("TEST_CONDITION", new HashMap<>(), new HashMap<>());
    robot.interact(() -> conditionsSectionBuilder.updateConditionsListView(List.of(List.of(data))));
    WaitForAsyncUtils.waitForFxEvents();

    robot.interact(() -> robot.lookup("#conditionsListView").queryListView().getSelectionModel().clearSelection());
    robot.clickOn("#removeConditionButton");

    verify(mockRemoveConditionHandler, never()).handle(anyInt(), anyInt());
  }

}