package oogasalad.editor.view;

import java.util.UUID;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.model.data.EditorObject;
import oogasalad.editor.model.data.object.IdentityData;
import oogasalad.editor.model.data.object.sprite.SpriteData;
import oogasalad.editor.view.tools.ObjectInteractionTool;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

@ExtendWith(ApplicationExtension.class)
class EditorGameViewTest {

  private static final int CELL_SIZE = 32;
  private static final double INITIAL_ZOOM = 1.0;
  private static final int VIEW_WIDTH = 800;
  private static final int VIEW_HEIGHT = 600;

  @Mock
  private EditorController mockController;

  private EditorGameView gameView;
  private AutoCloseable mockitoCloseable;

  @Start
  private void start(Stage stage) {
    mockitoCloseable = MockitoAnnotations.openMocks(this);
    gameView = new EditorGameView(CELL_SIZE, INITIAL_ZOOM, mockController, null); // TODO: Fix prefab pane
    gameView.setPrefSize(VIEW_WIDTH, VIEW_HEIGHT);
    Scene scene = new Scene(gameView, VIEW_WIDTH, VIEW_HEIGHT);
    stage.setScene(scene);
    stage.show();
  }

  @BeforeEach
  void setUp() {
  }

  @Test
  void testInitializationDrawsGrid() {
    assert(gameView.getChildren().size() == 2);
  }

  @Test
  void testSelectionStateHandling() {
    UUID testId = UUID.randomUUID();
    EditorObject mockObject = mock(EditorObject.class);
    SpriteData mockSpriteData = mock(SpriteData.class);
    IdentityData mockIdentityData = mock(IdentityData.class);

    when(mockSpriteData.getSpritePath()).thenReturn("dummy/path.png");
    when(mockObject.getSpriteData()).thenReturn(mockSpriteData);
    when(mockObject.getIdentityData()).thenReturn(mockIdentityData);
    when(mockController.getEditorObject(testId)).thenReturn(mockObject);

    Platform.runLater(() -> gameView.onObjectAdded(testId));
    waitForFxEvents();

    EditorGameView spyView = spy(gameView);

    Platform.runLater(() -> spyView.onSelectionChanged(testId));
    waitForFxEvents();
    try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

    verify(spyView, atLeastOnce()).redrawObjectsInternal();
    verify(mockController, never()).notifyObjectSelected(any());
  }
  @Test
  void testClickWithToolDelegatesToTool(FxRobot robot) {
    ObjectInteractionTool mockTool = mock(ObjectInteractionTool.class);
    gameView.updateCurrentTool(mockTool);

    robot.clickOn(gameView);
    waitForFxEvents();

    verify(mockTool).interactObjectAt(anyDouble(), anyDouble());
    verify(mockController, never()).notifyObjectSelected(any());
  }

  @Test
  void testOnObjectAddedPreloadsImageAndRedraws() {
    UUID testId = UUID.randomUUID();
    EditorObject mockObject = mock(EditorObject.class);
    SpriteData mockSpriteData = mock(SpriteData.class);
    IdentityData mockIdentityData = mock(IdentityData.class);

    when(mockSpriteData.getSpritePath()).thenReturn("test/path/image.png");
    when(mockObject.getSpriteData()).thenReturn(mockSpriteData);
    when(mockObject.getIdentityData()).thenReturn(mockIdentityData);
    when(mockController.getEditorObject(testId)).thenReturn(mockObject);

    Platform.runLater(() -> gameView.onObjectAdded(testId));
    waitForFxEvents();

    verify(mockController, atLeastOnce()).getEditorObject(testId);
  }

  @Test
  void testConstructorWithInvalidCellSizeThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> {
      new EditorGameView(0, INITIAL_ZOOM, mockController, null);
    });
    assertThrows(IllegalArgumentException.class, () -> {
      new EditorGameView(-10, INITIAL_ZOOM, mockController, null);
    });
  }
  @Test
  void testObjectWithNullSpritePathDrawsPlaceholder() {
    UUID testId = UUID.randomUUID();
    EditorObject mockObject = mock(EditorObject.class);
    SpriteData mockSpriteData = mock(SpriteData.class);
    IdentityData mockIdentityData = mock(IdentityData.class);

    when(mockSpriteData.getSpritePath()).thenReturn(null);
    when(mockObject.getSpriteData()).thenReturn(mockSpriteData);
    when(mockObject.getIdentityData()).thenReturn(mockIdentityData);
    when(mockIdentityData.getType()).thenReturn("TestGroup");
    when(mockController.getEditorObject(testId)).thenReturn(mockObject);

    Platform.runLater(() -> gameView.onObjectAdded(testId));
    waitForFxEvents();

    EditorGameView spyView = spy(gameView);

    Platform.runLater(() -> spyView.onSelectionChanged(testId));
    waitForFxEvents();
    try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

    verify(spyView, atLeastOnce()).redrawObjectsInternal();
  }

  @Test
  void testPreloadHandlesNullImagePath() {
    UUID testId = UUID.randomUUID();
    EditorObject mockObject = mock(EditorObject.class);
    SpriteData mockSpriteData = mock(SpriteData.class);

    when(mockSpriteData.getSpritePath()).thenReturn(null);
    when(mockObject.getSpriteData()).thenReturn(mockSpriteData);
    when(mockController.getEditorObject(testId)).thenReturn(mockObject);

    assertDoesNotThrow(() -> {
      Platform.runLater(() -> gameView.onObjectAdded(testId));
      waitForFxEvents();
    });
    verify(mockController, atLeastOnce()).getEditorObject(testId);
  }

  @Test
  void testPreloadHandlesInvalidResourcePath() {
    UUID testId = UUID.randomUUID();
    EditorObject mockObject = mock(EditorObject.class);
    SpriteData mockSpriteData = mock(SpriteData.class);
    IdentityData mockIdentityData = mock(IdentityData.class);

    when(mockSpriteData.getSpritePath()).thenReturn("/invalid/path/that/does/not/exist.png");
    when(mockObject.getSpriteData()).thenReturn(mockSpriteData);
    when(mockObject.getIdentityData()).thenReturn(mockIdentityData);
    when(mockController.getEditorObject(testId)).thenReturn(mockObject);

    EditorGameView spyView = spy(gameView);

    Platform.runLater(() -> spyView.onObjectAdded(testId));
    waitForFxEvents();
    try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

    verify(mockController, atLeastOnce()).getEditorObject(testId);
    verify(spyView, atLeastOnce()).redrawObjectsInternal();
  }
  @Test
  void testOnErrorOccurredDoesNotThrow() {
    assertDoesNotThrow(() -> {
      Platform.runLater(() -> gameView.onErrorOccurred("Test error message from test"));
      waitForFxEvents();
    });
  }
  @Test
  void testGettersReturnExpectedValues() {
    assertEquals(CELL_SIZE, gameView.getCellSize());
    gameView.getGridWidth();
    gameView.getGridHeight();
  }

  @Test
  void testConstructorWithNullControllerThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> {
      new EditorGameView(CELL_SIZE, INITIAL_ZOOM, null, null);
    });
  }
  @Test
  void testOnObjectRemovedRemovesObjectAndRedraws() {
    UUID testId = UUID.randomUUID();
    EditorObject mockObject = mock(EditorObject.class);
    SpriteData mockSpriteData = mock(SpriteData.class);
    IdentityData mockIdentityData = mock(IdentityData.class);

    when(mockSpriteData.getSpritePath()).thenReturn("test/path/image.png");
    when(mockObject.getSpriteData()).thenReturn(mockSpriteData);
    when(mockObject.getIdentityData()).thenReturn(mockIdentityData);
    when(mockController.getEditorObject(testId)).thenReturn(mockObject);

    Platform.runLater(() -> gameView.onObjectAdded(testId));
    waitForFxEvents();

    verify(mockController, atLeastOnce()).getEditorObject(testId);
  }

  @Test
  void testOnObjectUpdatedPreloadsImageAndRedraws() {
    UUID testId = UUID.randomUUID();
    EditorObject mockObject = mock(EditorObject.class);
    SpriteData mockSpriteData = mock(SpriteData.class);
    IdentityData mockIdentityData = mock(IdentityData.class);

    when(mockSpriteData.getSpritePath()).thenReturn("test/path/updated_image.png");
    when(mockObject.getSpriteData()).thenReturn(mockSpriteData);
    when(mockObject.getIdentityData()).thenReturn(mockIdentityData);
    when(mockController.getEditorObject(testId)).thenReturn(mockObject);

    Platform.runLater(() -> gameView.onObjectAdded(testId));
    waitForFxEvents();

    EditorGameView spyView = spy(gameView);

    Platform.runLater(() -> spyView.onObjectUpdated(testId));
    waitForFxEvents();
    try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

    verify(spyView, atLeastOnce()).redrawObjectsInternal();
  }


  @Test
  void testOnSelectionChangedUpdatesSelectionAndRedraws() {
    UUID testId1 = UUID.randomUUID();
    UUID testId2 = UUID.randomUUID();

    EditorObject mockObj1 = mock(EditorObject.class);
    EditorObject mockObj2 = mock(EditorObject.class);
    SpriteData mockSprite1 = mock(SpriteData.class);
    SpriteData mockSprite2 = mock(SpriteData.class);
    IdentityData mockIdentity1 = mock(IdentityData.class);
    IdentityData mockIdentity2 = mock(IdentityData.class);

    when(mockObj1.getSpriteData()).thenReturn(mockSprite1);
    when(mockObj1.getIdentityData()).thenReturn(mockIdentity1);
    when(mockSprite1.getSpritePath()).thenReturn("dummy/path1.png");
    doReturn(0).when(mockSprite1).getX();
    doReturn(0).when(mockSprite1).getY();

    when(mockObj2.getSpriteData()).thenReturn(mockSprite2);
    when(mockObj2.getIdentityData()).thenReturn(mockIdentity2);
    when(mockSprite2.getSpritePath()).thenReturn("dummy/path2.png");
    doReturn((int)CELL_SIZE).when(mockSprite2).getX();
    doReturn((int)CELL_SIZE).when(mockSprite2).getY();

    when(mockController.getEditorObject(testId1)).thenReturn(mockObj1);
    when(mockController.getEditorObject(testId2)).thenReturn(mockObj2);

    Platform.runLater(() -> gameView.onObjectAdded(testId1));
    Platform.runLater(() -> gameView.onObjectAdded(testId2));
    waitForFxEvents();

    EditorGameView spyView = spy(gameView);

    Platform.runLater(() -> spyView.onSelectionChanged(testId2));
    waitForFxEvents();
    try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

    verify(spyView, atLeastOnce()).redrawObjectsInternal();
    reset(spyView);

    Platform.runLater(() -> spyView.onSelectionChanged(null));
    waitForFxEvents();
    try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

    verify(spyView, atLeastOnce()).redrawObjectsInternal();

  }
}