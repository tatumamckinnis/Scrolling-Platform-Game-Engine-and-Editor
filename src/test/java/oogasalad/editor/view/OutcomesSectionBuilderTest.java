package oogasalad.editor.view;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import oogasalad.editor.model.data.event_enum.OutcomeType;
import oogasalad.editor.model.data.object.DynamicVariable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

@ExtendWith(ApplicationExtension.class)
class OutcomesSectionBuilderTest {

  private static final String UI_BUNDLE_NAME = "oogasalad/editor/view/resources/InputTabUI";
  private static final String ADD_BUTTON_KEY = "addOutcomeButton";
  private static final String REMOVE_BUTTON_KEY = "removeOutcomeButton";
  private static final String CREATE_PARAM_BUTTON_KEY = "createParamButton";

  @Mock private BiConsumer<OutcomeType, String> mockAddOutcomeHandler;
  @Mock private Consumer<OutcomeType> mockRemoveOutcomeHandler;
  @Mock private Runnable mockCreateParameterHandler;

  @Captor private ArgumentCaptor<OutcomeType> outcomeTypeCaptor;
  @Captor private ArgumentCaptor<String> stringArgCaptor;

  private ResourceBundle uiBundle;
  private OutcomesSectionBuilder builder;
  private ListView<String> outcomeListView;
  private ComboBox<OutcomeType> outcomeTypeComboBox;
  private ComboBox<String> parameterComboBox;
  private Button createParamButton;

  private AutoCloseable mockitoCloseable;
  private Node rootNode;

  @Start
  private void start(Stage stage) throws IOException {
    mockitoCloseable = MockitoAnnotations.openMocks(this);

    uiBundle = ResourceBundle.getBundle("oogasalad/editor/view/resources/InputTabUI");

    builder = new OutcomesSectionBuilder(uiBundle, mockAddOutcomeHandler, mockRemoveOutcomeHandler, mockCreateParameterHandler);
    rootNode = builder.build();
    StackPane rootPane = new StackPane(rootNode);
    Scene scene = new Scene(rootPane, 400, 400);
    stage.setScene(scene);
    stage.show();

    outcomeListView = builder.getOutcomesListView();
    parameterComboBox = builder.getParameterComboBox();
    outcomeTypeComboBox = lookup("#outcomeTypeComboBox");
    createParamButton = lookupButtonWithText("+");

    assertNotNull(outcomeListView, "Failed to get outcomeListView via getter");
    assertNotNull(parameterComboBox, "Failed to get parameterComboBox via getter");
    assertNotNull(outcomeTypeComboBox, "Failed to lookup #outcomeTypeComboBox (Ensure ID is set in source code)");
    assertNotNull(createParamButton, "Failed to lookup createParamButton by text");
  }

  @org.junit.jupiter.api.AfterEach
  void tearDown() throws Exception {
    if (mockitoCloseable != null) {
      mockitoCloseable.close();
    }
  }

  private <T extends javafx.scene.Node> T lookup(String query) {
    if (rootNode == null) {
      throw new IllegalStateException("Builder did not return a Node or start method failed.");
    }
    T node = (T) rootNode.lookup(query);
    if (node == null) {
      node = (T) rootNode.lookupAll(query).stream().findFirst().orElse(null);
      if (node == null){
        throw new RuntimeException("Node not found with query: " + query);
      }
    }
    return node;
  }

  private Button lookupButtonWithText(String text) {
    return lookupNode(button -> button instanceof Button && ((Button) button).getText().equals(text));
  }
  private <T extends javafx.scene.Node> T lookupNode(java.util.function.Predicate<T> filter) {
    if (rootNode == null) {
      throw new IllegalStateException("Builder did not return a Node or start method failed.");
    }
    return (T) rootNode.lookupAll("*").stream()
        .filter(node -> {
          try {
            if (node != null && filter.test((T) node)) {
              return true;
            }
            return false;
          } catch (ClassCastException e) {
            return false;
          }
        })
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Node not found with filter: " + filter.toString()));
  }


  @Test
  void testAddComponentStructure() {
    assertNotNull(outcomeListView, "Outcome ListView should exist");
    assertNotNull(outcomeTypeComboBox, "Outcome Type ComboBox should exist");
    assertNotNull(parameterComboBox, "Parameter ComboBox should exist");
    assertNotNull(createParamButton, "Create Parameter Button should exist");
    assertNotNull(lookupButtonWithText(uiBundle.getString(ADD_BUTTON_KEY)), "Add Outcome Button should exist");
    assertNotNull(lookupButtonWithText(uiBundle.getString(REMOVE_BUTTON_KEY)), "Remove Outcome Button should exist");
  }

  @Test
  void testAddButtonAddsOutcomeWithoutParameter(FxRobot robot) {
    OutcomeType testType = OutcomeType.JUMP;

    Platform.runLater(()-> outcomeTypeComboBox.getItems().add(testType));
    waitForFxEvents();

    robot.clickOn(outcomeTypeComboBox).clickOn(testType.toString());
    waitForFxEvents();
    Platform.runLater(()-> parameterComboBox.getSelectionModel().clearSelection());
    waitForFxEvents();

    robot.clickOn(lookupButtonWithText(uiBundle.getString(ADD_BUTTON_KEY)));
    waitForFxEvents();

    verify(mockAddOutcomeHandler).accept(outcomeTypeCaptor.capture(), stringArgCaptor.capture());
    assertEquals(testType, outcomeTypeCaptor.getValue());
    assertNull(stringArgCaptor.getValue());
  }

  @Test
  void testAddButtonAddsOutcomeWithParameter(FxRobot robot) {
    OutcomeType testType = OutcomeType.INCREMENT;
    String testParamName = "score";

    Platform.runLater(()-> {
      outcomeTypeComboBox.getItems().add(testType);
      parameterComboBox.getItems().add(testParamName);
    });
    waitForFxEvents();

    robot.clickOn(outcomeTypeComboBox).clickOn(testType.toString());
    robot.clickOn(parameterComboBox).clickOn(testParamName);
    waitForFxEvents();

    robot.clickOn(lookupButtonWithText(uiBundle.getString(ADD_BUTTON_KEY)));
    waitForFxEvents();

    verify(mockAddOutcomeHandler).accept(outcomeTypeCaptor.capture(), stringArgCaptor.capture());
    assertEquals(testType, outcomeTypeCaptor.getValue());
    assertEquals(testParamName, stringArgCaptor.getValue());
  }

  @Test
  void testAddButtonDoesNotAddWithoutOutcomeType(FxRobot robot) {
    String testParamName = "health";
    Platform.runLater(()-> parameterComboBox.getItems().add(testParamName));
    waitForFxEvents();
    robot.clickOn(parameterComboBox).clickOn(testParamName);
    waitForFxEvents();

    robot.clickOn(lookupButtonWithText(uiBundle.getString(ADD_BUTTON_KEY)));
    waitForFxEvents();

    verify(mockAddOutcomeHandler, never()).accept(any(), any());
  }


  @Test
  void testRemoveButtonRemovesSelectedOutcome(FxRobot robot) {
    OutcomeType typeToRemove = OutcomeType.PAUSE;
    String itemText = typeToRemove.toString();

    Platform.runLater(() -> outcomeListView.getItems().add(itemText));
    waitForFxEvents();

    robot.clickOn(outcomeListView);
    robot.clickOn(itemText);
    waitForFxEvents(); // Ensure selection is processed

    robot.clickOn(lookupButtonWithText(uiBundle.getString(REMOVE_BUTTON_KEY)));
    waitForFxEvents();

    verify(mockRemoveOutcomeHandler).accept(outcomeTypeCaptor.capture());
    assertEquals(typeToRemove, outcomeTypeCaptor.getValue());
  }

  @Test
  void testRemoveButtonRemovesSelectedOutcomeWithParam(FxRobot robot) {
    OutcomeType typeToRemove = OutcomeType.ADD_OBJECT;
    String paramName = "Coin";
    String itemText = String.format("%s (%s)", typeToRemove.toString(), paramName);

    Platform.runLater(() -> outcomeListView.getItems().add(itemText));
    waitForFxEvents();

    robot.clickOn(outcomeListView);
    robot.clickOn(itemText);
    waitForFxEvents();

    robot.clickOn(lookupButtonWithText(uiBundle.getString(REMOVE_BUTTON_KEY)));
    waitForFxEvents();

    verify(mockRemoveOutcomeHandler).accept(outcomeTypeCaptor.capture());
    assertEquals(typeToRemove, outcomeTypeCaptor.getValue());
  }


  @Test
  void testCreateParameterButtonTriggersHandler(FxRobot robot) {
    robot.clickOn(createParamButton);
    waitForFxEvents();

    verify(mockCreateParameterHandler).run();
  }

  @Test
  void testUpdateParameterComboBox(FxRobot robot) {
    DynamicVariable var1 = new DynamicVariable("var1", "int", "0", "");
    DynamicVariable var2 = new DynamicVariable("var2", "double", "1.0", "");
    List<DynamicVariable> vars = List.of(var1, var2);

    Platform.runLater(() -> builder.updateParameterComboBox(vars));
    waitForFxEvents();

    assertEquals(2, parameterComboBox.getItems().size());
    assertTrue(parameterComboBox.getItems().contains("var1"));
    assertTrue(parameterComboBox.getItems().contains("var2"));

    Platform.runLater(() -> builder.updateParameterComboBox(null));
    waitForFxEvents();
    assertTrue(parameterComboBox.getItems().isEmpty());
  }
}