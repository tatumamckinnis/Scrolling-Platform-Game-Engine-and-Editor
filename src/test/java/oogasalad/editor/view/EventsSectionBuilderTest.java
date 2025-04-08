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
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

/**
 * Tests for EventsSectionBuilder using TestFX and Mockito.
 */
@ExtendWith(ApplicationExtension.class)
class EventsSectionBuilderTest {

  private static final String UI_BUNDLE_BASE_NAME = "oogasalad.editor.view.resources.InputTabUI";
  private static final String ADD_BUTTON_KEY = "addEventButton";
  private static final String REMOVE_BUTTON_KEY = "removeEventButton";

  @Mock private Consumer<String> mockAddHandler;
  @Mock private Runnable mockRemoveHandler;
  @Mock private Consumer<String> mockSelectionHandler;

  @Captor private ArgumentCaptor<String> stringCaptor;

  private ResourceBundle uiBundle;
  private EventsSectionBuilder builder;
  private ListView<String> eventListView;
  private TextField eventIdField;

  private AutoCloseable mockitoCloseable;
  private Node rootNode;


  @Start
  private void start(Stage stage) {
    mockitoCloseable = MockitoAnnotations.openMocks(this);

    uiBundle = ResourceBundle.getBundle(UI_BUNDLE_BASE_NAME);

    builder = new EventsSectionBuilder(uiBundle, mockAddHandler, mockRemoveHandler, mockSelectionHandler);
    rootNode = builder.build();
    StackPane rootPane = new StackPane(rootNode);
    Scene scene = new Scene(rootPane, 400, 300);
    stage.setScene(scene);
    stage.show();

    eventListView = builder.getEventListView();
    eventIdField = builder.getEventIdField();


    assertNotNull(eventListView, "Failed to get eventListView via getter");
    assertNotNull(eventIdField, "Failed to get eventIdField via getter");
  }

  @AfterEach
  void tearDown() throws Exception {
    if (mockitoCloseable != null) {
      mockitoCloseable.close();
    }
  }

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

  private Button lookupButtonWithText(String text) {
    return lookupNode(button -> button instanceof Button && text.equals(((Button) button).getText()), Button.class);
  }

  private <T extends javafx.scene.Node> T lookupNode(java.util.function.Predicate<T> filter, Class<T> nodeClass) {
    if (rootNode == null) { throw new IllegalStateException("UI not built"); }
    return (T) rootNode.lookupAll("*").stream()
        .filter(node -> nodeClass.isInstance(node) && filter.test((T) node))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Node not found with filter: " + filter.toString() + " and type " + nodeClass.getSimpleName()));
  }


  @Test
  void testAddComponentStructure() {
    assertNotNull(eventListView);
    assertNotNull(eventIdField);
    assertNotNull(lookupButtonWithText(uiBundle.getString(ADD_BUTTON_KEY)));
    assertNotNull(lookupButtonWithText(uiBundle.getString(REMOVE_BUTTON_KEY)));
  }

  @Test
  void testAddButtonAddsEvent(FxRobot robot) {
    String testEventId = "TestEvent1";
    robot.clickOn(eventIdField).write(testEventId);
    waitForFxEvents();
    robot.clickOn(lookupButtonWithText(uiBundle.getString(ADD_BUTTON_KEY)));
    waitForFxEvents();

    verify(mockAddHandler).accept(stringCaptor.capture());
    assertEquals(testEventId, stringCaptor.getValue());
  }

  @Test
  void testAddButtonDoesNotAddEmptyEvent(FxRobot robot) {
    robot.clickOn(eventIdField).write("   ");
    waitForFxEvents();
    robot.clickOn(lookupButtonWithText(uiBundle.getString(ADD_BUTTON_KEY)));
    waitForFxEvents();

    verify(mockAddHandler, never()).accept(anyString());

    robot.clickOn(eventIdField).eraseText(3);
    waitForFxEvents();
    robot.clickOn(lookupButtonWithText(uiBundle.getString(ADD_BUTTON_KEY)));
    waitForFxEvents();

    verify(mockAddHandler, never()).accept(anyString());
  }

  @Test
  void testRemoveButtonRemovesSelectedEvent(FxRobot robot) {
    String itemToRemove = "EventToRemove";
    Platform.runLater(() -> eventListView.getItems().add(itemToRemove));
    waitForFxEvents();

    robot.clickOn(itemToRemove);
    waitForFxEvents();

    robot.clickOn(lookupButtonWithText(uiBundle.getString(REMOVE_BUTTON_KEY)));
    waitForFxEvents();

    verify(mockRemoveHandler).run();
  }


  @Test
  void testListSelectionNotifiesHandler(FxRobot robot) {
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
    assertEquals(event1, stringCaptor.getValue());

    robot.clickOn(event2);
    waitForFxEvents();

    verify(mockSelectionHandler, times(2)).accept(stringCaptor.capture());
    assertEquals(event2, stringCaptor.getValue());
  }
}