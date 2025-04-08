package oogasalad.editor.view;

import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import oogasalad.editor.model.data.event_enum.ConditionType;
import org.junit.jupiter.api.AfterEach;
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

/**
 * Test class for {@link ConditionsSectionBuilder}.
 * <p>
 * This class uses TestFX and Mockito to test the functionality
 * of the UI components and event handling in the ConditionsSectionBuilder.
 */
@ExtendWith(ApplicationExtension.class)
class ConditionsSectionBuilderTest {

  private static final String UI_BUNDLE_BASE_NAME = "oogasalad.editor.view.resources.InputTabUI";
  private static final String ADD_BUTTON_KEY = "addConditionButton";
  private static final String REMOVE_BUTTON_KEY = "removeConditionButton";

  @Mock private Consumer<ConditionType> mockAddConditionHandler;
  @Mock private Consumer<ConditionType> mockRemoveConditionHandler;

  @Captor private ArgumentCaptor<ConditionType> conditionTypeCaptor;

  private ResourceBundle uiBundle;
  private ConditionsSectionBuilder builder;
  private ListView<String> conditionsListView;
  private ComboBox<ConditionType> conditionComboBox;

  private AutoCloseable mockitoCloseable;
  private Node rootNode;

  /**
   * Initializes the JavaFX stage and builds the UI before each test.
   *
   * @param stage the JavaFX stage provided by the TestFX framework
   */
  @Start
  private void start(Stage stage) {
    mockitoCloseable = MockitoAnnotations.openMocks(this);
    uiBundle = ResourceBundle.getBundle(UI_BUNDLE_BASE_NAME);
    builder = new ConditionsSectionBuilder(uiBundle, mockAddConditionHandler, mockRemoveConditionHandler);
    rootNode = builder.build();
    StackPane rootPane = new StackPane(rootNode);
    Scene scene = new Scene(rootPane, 400, 350);
    stage.setScene(scene);
    stage.show();
    conditionsListView = builder.getConditionsListView();
    conditionComboBox = lookup("#conditionComboBox");
    assertNotNull(conditionsListView);
    assertNotNull(conditionComboBox);
  }

  /**
   * Closes the open Mockito resources after each test.
   */
  @AfterEach
  void tearDown() throws Exception {
    if (mockitoCloseable != null) {
      mockitoCloseable.close();
    }
  }

  /**
   * Looks up a node by its CSS ID.
   *
   * @param query CSS query string
   * @param <T>   the type of the JavaFX Node
   * @return the matching node
   */
  private <T extends javafx.scene.Node> T lookup(String query) {
    if (rootNode == null) { throw new IllegalStateException("UI not built"); }
    T node = (T) rootNode.lookup(query);
    if (node == null) {
      waitForFxEvents();
      node = (T) rootNode.lookup(query);
    }
    if (node == null) { throw new RuntimeException("Node not found with query: " + query); }
    return node;
  }

  /**
   * Finds a button with the given text.
   *
   * @param text the button text
   * @return the matching button
   */
  private Button lookupButtonWithText(String text) {
    return lookupNode(button -> button instanceof Button && text.equals(((Button) button).getText()), Button.class);
  }

  /**
   * Finds a node that matches a given predicate and type.
   *
   * @param filter    predicate for filtering nodes
   * @param nodeClass the expected node type
   * @param <T>       type of node
   * @return the matching node
   */
  private <T extends javafx.scene.Node> T lookupNode(java.util.function.Predicate<T> filter, Class<T> nodeClass) {
    if (rootNode == null) { throw new IllegalStateException("UI not built"); }
    return (T) rootNode.lookupAll("*").stream()
        .filter(node -> nodeClass.isInstance(node) && filter.test((T) node))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Node not found with filter: " + filter.toString() + " and type " + nodeClass.getSimpleName()));
  }

  /**
   * Tests if all UI components are added correctly.
   */
  @Test
  void testAddComponentStructure() {
    assertNotNull(conditionsListView);
    assertNotNull(conditionComboBox);
    assertNotNull(lookupButtonWithText(uiBundle.getString(ADD_BUTTON_KEY)));
    assertNotNull(lookupButtonWithText(uiBundle.getString(REMOVE_BUTTON_KEY)));
  }

  /**
   * Tests that clicking the add button triggers the correct handler with the selected condition.
   *
   * @param robot FxRobot instance from TestFX
   */
  @Test
  void testAddButtonAddsCondition(FxRobot robot) {
    ConditionType testType = ConditionType.KEY_SPACE;
    Platform.runLater(() -> conditionComboBox.getItems().add(testType));
    waitForFxEvents();
    robot.clickOn(conditionComboBox).clickOn(testType.toString());
    waitForFxEvents();
    robot.clickOn(lookupButtonWithText(uiBundle.getString(ADD_BUTTON_KEY)));
    waitForFxEvents();
    verify(mockAddConditionHandler).accept(conditionTypeCaptor.capture());
    assertEquals(testType, conditionTypeCaptor.getValue());
  }

  /**
   * Tests that no action is taken if add button is clicked with no condition selected.
   *
   * @param robot FxRobot instance from TestFX
   */
  @Test
  void testAddButtonDoesNotAddWithoutConditionType(FxRobot robot) {
    Platform.runLater(() -> conditionComboBox.getSelectionModel().clearSelection());
    waitForFxEvents();
    robot.clickOn(lookupButtonWithText(uiBundle.getString(ADD_BUTTON_KEY)));
    waitForFxEvents();
    verify(mockAddConditionHandler, never()).accept(any());
  }

  /**
   * Tests that clicking the remove button removes the selected condition from the list.
   *
   * @param robot FxRobot instance from TestFX
   */
  @Test
  void testRemoveButtonRemovesSelectedCondition(FxRobot robot) {
    ConditionType typeToRemove = ConditionType.KEY_LEFT;
    String itemText = typeToRemove.toString();
    Platform.runLater(() -> conditionsListView.getItems().add(itemText));
    waitForFxEvents();
    robot.clickOn(itemText);
    waitForFxEvents();
    robot.clickOn(lookupButtonWithText(uiBundle.getString(REMOVE_BUTTON_KEY)));
    waitForFxEvents();
    verify(mockRemoveConditionHandler).accept(conditionTypeCaptor.capture());
    assertEquals(typeToRemove, conditionTypeCaptor.getValue());
  }

  /**
   * Tests that clicking remove does nothing if no item is selected.
   *
   * @param robot FxRobot instance from TestFX
   */
  @Test
  void testRemoveButtonDoesNotRemoveWithoutSelection(FxRobot robot) {
    ConditionType typePresent = ConditionType.KEY_RIGHT;
    String itemText = typePresent.toString();
    Platform.runLater(() -> conditionsListView.getItems().add(itemText));
    waitForFxEvents();
    robot.clickOn(lookupButtonWithText(uiBundle.getString(REMOVE_BUTTON_KEY)));
    waitForFxEvents();
    verify(mockRemoveConditionHandler, never()).accept(any());
  }
}
