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
import oogasalad.editor.model.data.object.DynamicVariable;
import oogasalad.editor.model.data.object.event.ExecutorData;
import oogasalad.editor.view.eventui.AddOutcomeHandler;
import oogasalad.editor.view.eventui.EditOutcomeParamHandler;
import oogasalad.editor.view.eventui.OutcomeDisplayItem;
import oogasalad.editor.view.eventui.OutcomesSectionBuilder;
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


@ExtendWith(ApplicationExtension.class)
class OutcomesSectionBuilderTest {

  @Mock
  private ResourceBundle mockUiBundle;
  @Mock
  private Supplier<List<String>> mockOutcomeTypeSupplier;
  @Mock
  private Supplier<List<DynamicVariable>> mockDynamicVariableSupplier;
  @Mock
  private AddOutcomeHandler mockAddOutcomeHandler;
  @Mock
  private IntConsumer mockRemoveOutcomeHandler;
  @Mock
  private Runnable mockCreateParameterHandler;
  @Mock
  private EditOutcomeParamHandler mockEditOutcomeParamHandler;

  private OutcomesSectionBuilder outcomesSectionBuilder;
  private VBox rootNode;

  private static Properties testProperties;

  @BeforeAll
  static void loadTestProperties() {
    testProperties = new Properties();
    // Keep all properties definitions as they might be used by the builder internally
    testProperties.setProperty("id.sectionVbox", "outcomesSectionVbox");
    testProperties.setProperty("style.inputSubSection", "input-sub-section");
    testProperties.setProperty("key.outcomesHeader", "Outcomes");
    testProperties.setProperty("id.outcomeSelectionRow", "outcomeSelectionRow");
    testProperties.setProperty("id.outcomeTypeComboBox", "outcomeTypeComboBox");
    testProperties.setProperty("key.promptSelectOutcome", "Select Outcome Type");
    testProperties.setProperty("key.addOutcomeButton", "Add Outcome");
    testProperties.setProperty("id.addOutcomeButton", "addOutcomeButton");
    testProperties.setProperty("key.removeOutcomeButton", "Remove Outcome");
    testProperties.setProperty("id.removeOutcomeButton", "removeOutcomeButton");
    testProperties.setProperty("id.dynamicVariableRow", "dynamicVariableRow");
    testProperties.setProperty("key.parameterLabel", "Parameter");
    testProperties.setProperty("id.dynamicVariableComboBox", "dynamicVariableComboBox");
    testProperties.setProperty("key.promptSelectParameter", "Select Variable Parameter");
    testProperties.setProperty("key.createParamButton", "Create Param");
    testProperties.setProperty("text.createParamButton", "+");
    testProperties.setProperty("id.addVariableButton", "addVariableButton");
    testProperties.setProperty("id.outcomesListView", "outcomesListView");
    testProperties.setProperty("style.dataListView", "data-list-view");
    testProperties.setProperty("id.parametersContainer", "outcomeParametersContainer");
    testProperties.setProperty("key.executorParametersHeader", "Parameters");
    testProperties.setProperty("id.parametersPane", "outcomeParametersPane");
    testProperties.setProperty("id.parametersScrollPane", "outcomeParametersScrollPane");
    testProperties.setProperty("style.sectionHeader", "section-header");
    testProperties.setProperty("style.actionButton", "action-button");
    testProperties.setProperty("style.removeButton", "remove-button");
    testProperties.setProperty("style.smallButton", "small-button");
    testProperties.setProperty("key.warnNoOutcomeType", "No outcome type selected.");
    testProperties.setProperty("key.warnNoOutcomeSelected", "No outcome selected for removal.");
    testProperties.setProperty("id.parametersGrid", "parametersGrid");
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
    testProperties.setProperty("TEST_OUTCOME.param.count", "2");
    testProperties.setProperty("TEST_OUTCOME.param.1.name", "StringParam");
    testProperties.setProperty("TEST_OUTCOME.param.1.type", "String");
    testProperties.setProperty("TEST_OUTCOME.param.1.description", "A string parameter.");
    testProperties.setProperty("TEST_OUTCOME.param.1.defaultValue", "default");
    testProperties.setProperty("TEST_OUTCOME.param.2.name", "DoubleParam");
    testProperties.setProperty("TEST_OUTCOME.param.2.type", "Double");
    testProperties.setProperty("TEST_OUTCOME.param.2.description", "A double parameter.");
    testProperties.setProperty("TEST_OUTCOME.param.2.defaultValue", "1.0");
    testProperties.setProperty("OTHER_OUTCOME.param.count", "0");
  }

  @Start
  public void start(Stage stage) {
    MockitoAnnotations.openMocks(this);

    when(mockUiBundle.getString(anyString())).thenAnswer(invocation -> {
      String key = invocation.getArgument(0);
      return testProperties.getProperty("key." + key, key);
    });
    when(mockOutcomeTypeSupplier.get()).thenReturn(Arrays.asList("TEST_OUTCOME", "OTHER_OUTCOME"));
    when(mockDynamicVariableSupplier.get()).thenReturn(Arrays.asList(
        new DynamicVariable("var1", "double", "10.0", ""),
        new DynamicVariable("var2", "string", "hello", "")
    ));

    outcomesSectionBuilder = new OutcomesSectionBuilder(
        mockUiBundle, mockOutcomeTypeSupplier, mockDynamicVariableSupplier,
        mockAddOutcomeHandler, mockRemoveOutcomeHandler, mockCreateParameterHandler,
        mockEditOutcomeParamHandler
    );

    try {
      java.lang.reflect.Field propsField = OutcomesSectionBuilder.class.getDeclaredField("localProps");
      propsField.setAccessible(true);
      propsField.set(outcomesSectionBuilder, testProperties);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      fail("Failed to inject test properties", e);
    }

    rootNode = (VBox) outcomesSectionBuilder.build();
    Scene scene = new Scene(rootNode, 400, 600);
    stage.setScene(scene);
    stage.show();
  }


  @Test
  void build_createsMainVBoxWithCorrectIdAndStyle(FxRobot robot) {
    verifyThat("#outcomesSectionVbox", NodeMatchers.isVisible());
    assertTrue(rootNode.getStyleClass().contains("input-sub-section"));
  }

  @Test
  void build_addsHeaderLabel(FxRobot robot) {
    verifyThat("#outcomesSectionVbox .label", LabeledMatchers.hasText("Outcomes"));
    Label header = robot.lookup("#outcomesSectionVbox .label").match(node -> node instanceof Label && ((Label) node).getText().equals("Outcomes")).queryAs(Label.class);
    assertTrue(header.getStyleClass().contains("section-header"));
  }

  @Test
  void build_populatesOutcomeTypeComboBox(FxRobot robot) {
    verifyThat("#outcomeTypeComboBox", ComboBoxMatchers.hasItems(2));
    ComboBox<String> combo = robot.lookup("#outcomeTypeComboBox").queryComboBox();
    assertEquals(Arrays.asList("TEST_OUTCOME", "OTHER_OUTCOME"), combo.getItems());
  }


  @Test
  void updateOutcomesListView_populatesListViewCorrectly(FxRobot robot) {
    ExecutorData data1 = new ExecutorData("TEST_OUTCOME", new HashMap<>(), new HashMap<>());
    ExecutorData data2 = new ExecutorData("OTHER_OUTCOME", new HashMap<>(), new HashMap<>());
    List<ExecutorData> outcomes = Arrays.asList(data1, data2);

    robot.interact(() -> outcomesSectionBuilder.updateOutcomesListView(outcomes));
    WaitForAsyncUtils.waitForFxEvents();

    verifyThat("#outcomesListView", ListViewMatchers.hasItems(2));
    verifyThat("#outcomesListView", (ListView<OutcomeDisplayItem> lv) -> lv.getItems().get(0).toString().equals("[0]: TEST_OUTCOME"));
    verifyThat("#outcomesListView", (ListView<OutcomeDisplayItem> lv) -> lv.getItems().get(1).toString().equals("[1]: OTHER_OUTCOME"));
  }

  @Test
  void updateOutcomesListView_handlesNullInput(FxRobot robot) {
    ExecutorData data1 = new ExecutorData("TEST_OUTCOME", new HashMap<>(), new HashMap<>());
    robot.interact(() -> outcomesSectionBuilder.updateOutcomesListView(List.of(data1)));
    WaitForAsyncUtils.waitForFxEvents();
    verifyThat("#outcomesListView", ListViewMatchers.hasItems(1));

    robot.interact(() -> outcomesSectionBuilder.updateOutcomesListView(null));
    WaitForAsyncUtils.waitForFxEvents();
    verifyThat("#outcomesListView", ListViewMatchers.hasItems(0));
  }

  @Test
  void updateDynamicVariableComboBox_populatesComboBox(FxRobot robot) {
    robot.interact(() -> outcomesSectionBuilder.updateDynamicVariableComboBox());
    WaitForAsyncUtils.waitForFxEvents();
    verifyThat("#dynamicVariableComboBox", ComboBoxMatchers.hasItems(2));
    ComboBox<String> combo = robot.lookup("#dynamicVariableComboBox").queryComboBox();
    assertEquals(Arrays.asList("var1", "var2"), combo.getItems());
  }

  @Test
  void addOutcomeButton_triggersHandler(FxRobot robot) {
    robot.clickOn("#outcomeTypeComboBox").clickOn("TEST_OUTCOME");
    WaitForAsyncUtils.waitForFxEvents();
    robot.clickOn("#addOutcomeButton");
    verify(mockAddOutcomeHandler).handle("TEST_OUTCOME");
  }

  @Test
  void addOutcomeButton_doesNothingIfTypeNotSelected(FxRobot robot) {
    robot.interact(() -> robot.lookup("#outcomeTypeComboBox").queryComboBox().getSelectionModel().clearSelection());
    robot.clickOn("#addOutcomeButton");
    verify(mockAddOutcomeHandler, never()).handle(anyString());
  }


  @Test
  void removeOutcomeButton_triggersHandlerWhenSelected(FxRobot robot) {
    ExecutorData data1 = new ExecutorData("TEST_OUTCOME", new HashMap<>(), new HashMap<>());
    robot.interact(() -> outcomesSectionBuilder.updateOutcomesListView(List.of(data1)));
    WaitForAsyncUtils.waitForFxEvents();

    robot.clickOn("[0]: TEST_OUTCOME");
    WaitForAsyncUtils.waitForFxEvents();

    robot.clickOn("#removeOutcomeButton");
    verify(mockRemoveOutcomeHandler).accept(0);
  }

  @Test
  void removeOutcomeButton_doesNothingIfNotSelected(FxRobot robot) {
    ExecutorData data1 = new ExecutorData("TEST_OUTCOME", new HashMap<>(), new HashMap<>());
    robot.interact(() -> outcomesSectionBuilder.updateOutcomesListView(List.of(data1)));
    WaitForAsyncUtils.waitForFxEvents();

    robot.interact(() -> robot.lookup("#outcomesListView").queryListView().getSelectionModel().clearSelection());
    robot.clickOn("#removeOutcomeButton");
    verify(mockRemoveOutcomeHandler, never()).accept(anyInt());
  }

  @Test
  void createParameterButton_triggersHandler(FxRobot robot) {
    robot.clickOn("#addVariableButton");
    verify(mockCreateParameterHandler).run();
  }

}