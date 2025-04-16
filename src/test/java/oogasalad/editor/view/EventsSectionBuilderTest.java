package oogasalad.editor.view;

import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

/**
 * Tests for EventsSectionBuilder using TestFX and Mockito. Verifies UI component creation
 * and interaction logic for adding, removing, and selecting events by their String IDs.
 */
@ExtendWith(ApplicationExtension.class)
class EventsSectionBuilderTest {

  private static final String UI_BUNDLE_BASE_NAME = "oogasalad.editor.view.resources.InputTabUI";
  private static final String ADD_BUTTON_KEY = "addEventButton";
  private static final String REMOVE_BUTTON_KEY = "removeEventButton";
  private static final String EVENT_ID_FIELD_ID = "#eventIdField";
  private static final String EVENT_LIST_VIEW_ID = "#eventListView";

  @Mock private Consumer<String> mockAddHandler;
  @Mock private Runnable mockRemoveHandler;
  @Mock private Consumer<String> mockSelectionHandler;

  @Captor private ArgumentCaptor<String> stringCaptor;

  private ResourceBundle uiBundle;
  private EventsSectionBuilder builder;
  private Node rootNode;

  private AutoCloseable mockitoCloseable;


  /**
   * Initializes the JavaFX stage, mocks, resource bundle, and builds the UI.
   * This setup runs before each test method.
   * @param stage The JavaFX stage provided by TestFX.
   * @throws Exception If mock initialization fails.
   */
  @Start
  private void start(Stage stage) throws Exception {
    mockitoCloseable = MockitoAnnotations.openMocks(this);
    uiBundle = ResourceBundle.getBundle(UI_BUNDLE_BASE_NAME);

    builder = new EventsSectionBuilder(uiBundle, mockAddHandler, mockRemoveHandler, mockSelectionHandler);
    rootNode = builder.build();

    StackPane rootPane = new StackPane(rootNode);
    Scene scene = new Scene(rootPane, 400, 300);
    stage.setScene(scene);
    stage.show();

    assertNotNull(builder.getEventListView(), "Failed to get eventListView via getter");
    assertNotNull(builder.getEventIdField(), "Failed to get eventIdField via getter");
  }

  /**
   * Cleans up resources, specifically closing Mockito annotations context.
   * This runs after each test method.
   * @throws Exception If cleanup fails.
   */
  @AfterEach
  void tearDown() throws Exception {
    if (mockitoCloseable != null) {
      mockitoCloseable.close();
    }
  }

  /**
   * Helper method to look up a node within the built UI using a CSS query.
   * Includes waiting for FX events to ensure UI updates are processed.
   * @param query The CSS selector query.
   * @param <T> The expected type of the Node.
   * @return The found Node cast to type T.
   * @throws RuntimeException if the node is not found.
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
   * Tests that the essential UI components (TextField, ListView, Buttons) are created and present.
   */
  @Test
  void testComponentStructure() {
    assertNotNull(lookup(EVENT_LIST_VIEW_ID));
    assertNotNull(lookup(EVENT_ID_FIELD_ID));
    assertNotNull(lookup("#addEventButton"));
    assertNotNull(lookup("#removeEventButton"));
  }

  /**
   * Tests that typing an event ID and clicking the "Add Event" button triggers the add handler
   * with the correct event ID string.
   * @param robot TestFX robot for UI interaction.
   */
  @Test
  void testAddButtonAddsEvent(FxRobot robot) {
    String testEventId = "TestEvent1";
    TextField eventIdField = lookup(EVENT_ID_FIELD_ID);
    Button addButton = lookup("#addEventButton");

    robot.clickOn(eventIdField).write(testEventId);
    waitForFxEvents();
    robot.clickOn(addButton);
    waitForFxEvents();

    verify(mockAddHandler).accept(stringCaptor.capture());
    assertEquals(testEventId, stringCaptor.getValue());
    assertEquals("", eventIdField.getText());
  }

  /**
   * Tests that clicking the "Add Event" button does not trigger the add handler if the
   * event ID field is empty or contains only whitespace.
   * @param robot TestFX robot for UI interaction.
   */
  @Test
  void testAddButtonDoesNotAddEmptyEvent(FxRobot robot) {
    TextField eventIdField = lookup(EVENT_ID_FIELD_ID);
    Button addButton = lookup("#addEventButton");

    robot.clickOn(eventIdField).write("   ");
    waitForFxEvents();
    robot.clickOn(addButton);
    waitForFxEvents();
    verify(mockAddHandler, never()).accept(anyString());

    robot.clickOn(eventIdField).eraseText(3);
    waitForFxEvents();
    robot.clickOn(addButton);
    waitForFxEvents();
    verify(mockAddHandler, never()).accept(anyString());
  }

  /**
   * Tests that selecting an item in the event list and clicking the "Remove Event" button
   * triggers the remove handler.
   * @param robot TestFX robot for UI interaction.
   */
  @Test
  void testRemoveButtonRemovesSelectedEvent(FxRobot robot) {
    ListView<String> eventListView = lookup(EVENT_LIST_VIEW_ID);
    Button removeButton = lookup("#removeEventButton");
    String itemToRemove = "EventToRemove";

    Platform.runLater(() -> eventListView.getItems().add(itemToRemove));
    waitForFxEvents();

    robot.clickOn(itemToRemove);
    waitForFxEvents();
    assertEquals(itemToRemove, eventListView.getSelectionModel().getSelectedItem());

    robot.clickOn(removeButton);
    waitForFxEvents();

    verify(mockRemoveHandler).run();
  }


  /**
   * Tests that selecting different items in the event list triggers the selection handler
   * with the correct event ID string each time.
   * @param robot TestFX robot for UI interaction.
   */
  @Test
  void testListSelectionNotifiesHandler(FxRobot robot) {
    ListView<String> eventListView = lookup(EVENT_LIST_VIEW_ID);
    String event1 = "EventOne";
    String event2 = "EventTwo";

    Platform.runLater(() -> {
      eventListView.getItems().add(event1);
      eventListView.getItems().add(event2);
    });
    waitForFxEvents();

    robot.clickOn(event1);
    waitForFxEvents();
    verify(mockSelectionHandler).accept(stringCaptor.capture());
    assertEquals(event1, stringCaptor.getValue(), "First selection incorrect");

    robot.clickOn(event2);
    waitForFxEvents();
    verify(mockSelectionHandler, times(2)).accept(stringCaptor.capture());
    assertEquals(event2, stringCaptor.getValue(), "Second selection incorrect");
  }
}