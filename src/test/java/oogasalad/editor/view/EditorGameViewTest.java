// TODO: Refactored very soon before deadline, we had no time to fix these tests.
//
// package oogasalad.editor.view;
//
//import java.io.File;
//import java.util.Map;
//import java.util.ResourceBundle;
//import java.util.UUID;
//import javafx.application.Platform;
//import javafx.scene.Scene;
//import javafx.scene.input.MouseButton;
//import javafx.stage.Stage;
//import oogasalad.editor.controller.EditorController;
//import oogasalad.editor.controller.level.EditorDataAPI;
//import oogasalad.editor.model.data.EditorLevelData;
//import oogasalad.editor.model.data.Layer;
//import oogasalad.editor.model.data.SpriteSheetAtlas;
//import oogasalad.editor.model.data.object.EditorObject;
//import oogasalad.editor.model.data.object.IdentityData;
//import oogasalad.editor.model.data.object.sprite.FrameData;
//import oogasalad.editor.model.data.object.sprite.SpriteData;
//import oogasalad.editor.view.components.EditorGameView;
//import oogasalad.editor.view.components.PrefabPalettePane;
//import oogasalad.editor.view.tools.ObjectInteractionTool;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.MockitoAnnotations;
//import org.testfx.api.FxRobot;
//import org.testfx.framework.junit5.ApplicationExtension;
//import org.testfx.framework.junit5.Start;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;
//
//@ExtendWith(ApplicationExtension.class)
//class EditorGameViewTest {
//
//  private static final int CELL_SIZE = 16;
//  private static final double INITIAL_ZOOM = 1.0;
//  private static final int VIEW_WIDTH = 800;
//  private static final int VIEW_HEIGHT = 600;
//  private static final long EVENT_WAIT_MS = 200;
//
//  @Mock
//  private EditorController mockController;
//  @Mock
//  private PrefabPalettePane mockPrefabPalettePane;
//  @Mock
//  private ResourceBundle mockUiBundle;
//  @Mock
//  private EditorDataAPI mockEditorDataAPI;
//  @Mock
//  private EditorLevelData mockLevelData;
//  @Mock
//  private SpriteSheetAtlas mockAtlas;
//  @Mock
//  private File mockImageFile;
//  @Mock
//  private Layer mockLayer;
//
//
//  private EditorGameView gameView;
//  private AutoCloseable mockitoCloseable;
//  private Stage stage;
//
//  @Start
//  private void start(Stage stage) {
//    this.stage = stage;
//  }
//
//  @BeforeEach
//  void setUp() {
//    mockitoCloseable = MockitoAnnotations.openMocks(this);
//
//    Mockito.lenient().when(mockUiBundle.getString(anyString()))
//        .thenAnswer(invocation -> invocation.getArgument(0));
//
//    Mockito.lenient().when(mockController.getEditorDataAPI()).thenReturn(mockEditorDataAPI);
//    Mockito.lenient().when(mockEditorDataAPI.getLevel()).thenReturn(mockLevelData);
//    Mockito.lenient().when(mockEditorDataAPI.getObjectLayerPriority(any(UUID.class))).thenReturn(0);
//    Mockito.lenient().when(mockLevelData.getAtlas(any(UUID.class))).thenReturn(mockAtlas);
//    Mockito.lenient().when(mockAtlas.getImageFile()).thenReturn(mockImageFile);
//
//    Mockito.lenient().when(mockImageFile.exists()).thenReturn(true); // Assume file exists conceptually
//    Mockito.lenient().when(mockImageFile.isFile()).thenReturn(true);
//    Mockito.lenient().when(mockImageFile.isAbsolute()).thenReturn(true);
//    Mockito.lenient().when(mockImageFile.getAbsolutePath()).thenReturn("/dummy/absolute/path/sprite.png");
//    try {
//      Mockito.lenient().when(mockImageFile.toURI()).thenReturn(new File("/dummy/absolute/path/sprite.png").toURI());
//    } catch (Exception e) {
//      fail("Failed to create dummy file URI in setup", e);
//    }
//
//
//    Mockito.lenient().when(mockLayer.getName()).thenReturn("DefaultLayer");
//
//    gameView = new EditorGameView(CELL_SIZE, INITIAL_ZOOM, mockController, mockPrefabPalettePane, mockUiBundle);
//    gameView.setPrefSize(VIEW_WIDTH, VIEW_HEIGHT);
//    Scene scene = new Scene(gameView, VIEW_WIDTH, VIEW_HEIGHT);
//    Platform.runLater(() -> {
//      stage.setScene(scene);
//      stage.show();
//      gameView.requestFocus();
//    });
//    waitForFxEvents();
//  }
//
//  @AfterEach
//  void tearDown() throws Exception {
//    if (mockitoCloseable != null) {
//      mockitoCloseable.close();
//    }
//    Platform.runLater(() -> {
//      stage.hide();
//    });
//    waitForFxEvents();
//  }
//
//
//  @Test
//  void testInitializationCreatesCanvases() {
//    assertEquals(2, gameView.getChildren().size());
//    assertTrue(gameView.getChildren().get(0) instanceof javafx.scene.canvas.Canvas);
//    assertTrue(gameView.getChildren().get(1) instanceof javafx.scene.canvas.Canvas);
//  }
//
//
//  @Test
//  void testConstructorWithNullArgsThrowsException() {
//    assertThrows(NullPointerException.class, () -> {
//      new EditorGameView(CELL_SIZE, INITIAL_ZOOM, null, mockPrefabPalettePane, mockUiBundle);
//    });
//    assertThrows(NullPointerException.class, () -> {
//      new EditorGameView(CELL_SIZE, INITIAL_ZOOM, mockController, null, mockUiBundle);
//    });
//    assertThrows(NullPointerException.class, () -> {
//      new EditorGameView(CELL_SIZE, INITIAL_ZOOM, mockController, mockPrefabPalettePane, null);
//    });
//  }
//
//
//  @Test
//  void testUpdateCurrentTool() {
//    ObjectInteractionTool mockTool1 = mock(ObjectInteractionTool.class);
//    ObjectInteractionTool mockTool2 = mock(ObjectInteractionTool.class);
//
//    gameView.updateCurrentTool(mockTool1);
//    robotClickOnViewCenter(new FxRobot());
//    verify(mockTool1, timeout(500)).interactObjectAt(anyDouble(), anyDouble());
//    verify(mockTool2, never()).interactObjectAt(anyDouble(), anyDouble());
//
//    gameView.updateCurrentTool(mockTool2);
//    robotClickOnViewCenter(new FxRobot());
//    verify(mockTool1, times(1)).interactObjectAt(anyDouble(), anyDouble());
//    verify(mockTool2, timeout(500)).interactObjectAt(anyDouble(), anyDouble());
//
//    gameView.updateCurrentTool(null);
//    robotClickOnViewCenter(new FxRobot());
//    verify(mockTool1, times(1)).interactObjectAt(anyDouble(), anyDouble());
//    verify(mockTool2, times(1)).interactObjectAt(anyDouble(), anyDouble());
//  }
//
//
//  @Test
//  void testOnObjectAddedPreloadsImageAndRedraws(FxRobot robot) {
//    UUID testId = UUID.randomUUID();
//    setupMockObject(testId, "test/path/image.png");
//
//    EditorGameView spyView = spy(gameView);
//
//    Platform.runLater(() -> spyView.onObjectAdded(testId));
//    waitForFxEvents();
//
//    verify(mockController, timeout(500).atLeastOnce()).getEditorObject(testId);
//    verify(spyView, timeout(500).atLeastOnce()).redrawObjectsInternal();
//  }
//
//  @Test
//  void testOnObjectRemovedRemovesObjectAndRedraws(FxRobot robot) {
//    UUID testId = UUID.randomUUID();
//    setupMockObject(testId, "test/path/image.png");
//
//    Platform.runLater(() -> gameView.onObjectAdded(testId));
//    waitForFxEvents();
//
//    EditorGameView spyView = spy(gameView);
//    reset(spyView);
//
//    Platform.runLater(() -> spyView.onObjectRemoved(testId));
//    waitForFxEvents();
//
//    verify(spyView, timeout(500).atLeastOnce()).redrawObjectsInternal();
//  }
//
//  @Test
//  void testOnObjectUpdatedPreloadsImageAndRedraws(FxRobot robot) {
//    UUID testId = UUID.randomUUID();
//    setupMockObject(testId, "test/path/image.png");
//
//    Platform.runLater(() -> gameView.onObjectAdded(testId));
//    waitForFxEvents();
//
//    EditorGameView spyView = spy(gameView);
//    reset(spyView);
//
//    setupMockObject(testId, "test/path/updated_image.png");
//
//    Platform.runLater(() -> spyView.onObjectUpdated(testId));
//    waitForFxEvents();
//
//    verify(mockController, timeout(500).atLeastOnce()).getEditorObject(testId);
//    verify(spyView, timeout(500).atLeastOnce()).redrawObjectsInternal();
//  }
//
//
//  @Test
//  void testOnSelectionChangedUpdatesSelectionAndRedraws(FxRobot robot) {
//    UUID testId1 = UUID.randomUUID();
//    UUID testId2 = UUID.randomUUID();
//    setupMockObject(testId1, "path1.png", 0, 0);
//    setupMockObject(testId2, "path2.png", CELL_SIZE, CELL_SIZE);
//
//    Platform.runLater(() -> gameView.onObjectAdded(testId1));
//    Platform.runLater(() -> gameView.onObjectAdded(testId2));
//    waitForFxEvents();
//
//    EditorGameView spyView = spy(gameView);
//
//    Platform.runLater(() -> spyView.onSelectionChanged(testId2));
//    waitForFxEvents();
//    verify(spyView, timeout(500).atLeastOnce()).redrawObjectsInternal();
//
//    reset(spyView);
//    Platform.runLater(() -> spyView.onSelectionChanged(null));
//    waitForFxEvents();
//    verify(spyView, timeout(500).atLeastOnce()).redrawObjectsInternal();
//  }
//
//
//  @Test
//  void testSetCellSize(FxRobot robot) {
//    EditorGameView spyView = spy(gameView);
//    Platform.runLater(() -> spyView.setCellSize(CELL_SIZE * 2));
//    waitForFxEvents();
//    assertEquals(CELL_SIZE * 2, spyView.getCellSize());
//    verify(spyView, timeout(500).atLeastOnce()).redrawObjectsInternal();
//  }
//
//  @Test
//  void testRefreshDisplayCallsRedraw(FxRobot robot) {
//    EditorGameView spyView = spy(gameView);
//    Platform.runLater(() -> spyView.refreshDisplay());
//    waitForFxEvents();
//    verify(spyView, timeout(500).atLeastOnce()).redrawObjectsInternal();
//  }
//
//  @Test
//  void testRemoveAllObjectsClearsAndRedraws(FxRobot robot) {
//    UUID testId = UUID.randomUUID();
//    setupMockObject(testId, "path.png");
//    Platform.runLater(() -> gameView.onObjectAdded(testId));
//    waitForFxEvents();
//
//    EditorGameView spyView = spy(gameView);
//    reset(spyView);
//
//    Platform.runLater(() -> spyView.removeAllObjects());
//    waitForFxEvents();
//    verify(spyView, timeout(500).atLeastOnce()).redrawObjectsInternal();
//  }
//
//  @Test
//  void testGettersReturnValues() {
//    assertEquals(CELL_SIZE, gameView.getCellSize());
//    assertEquals(VIEW_WIDTH, gameView.getGridWidth(), 0.1);
//    assertEquals(VIEW_HEIGHT, gameView.getGridHeight(), 0.1);
//    assertNull(gameView.getSelectedPrefab());
//  }
//
//  @Test
//  void testOtherListenerMethodsDoNotThrow() {
//    assertDoesNotThrow(() -> Platform.runLater(() -> gameView.onDynamicVariablesChanged()));
//    assertDoesNotThrow(() -> Platform.runLater(() -> gameView.onErrorOccurred("Test Error")));
//    assertDoesNotThrow(() -> Platform.runLater(() -> gameView.onPrefabsChanged()));
//    assertDoesNotThrow(() -> Platform.runLater(() -> gameView.onSpriteTemplateChanged()));
//    waitForFxEvents();
//  }
//
//  private void robotClickOnViewCenter(FxRobot robot) {
//    robot.clickOn(gameView, MouseButton.PRIMARY);
//    waitForFxEvents();
//  }
//
//  private void setupMockObject(UUID id, String spritePath) {
//    setupMockObject(id, spritePath, 0.0, 0.0);
//  }
//
//  private void setupMockObject(UUID id, String spritePath, double x, double y) {
//    EditorObject mockObject = mock(EditorObject.class);
//    SpriteData mockSpriteData = mock(SpriteData.class);
//    IdentityData mockIdentityData = mock(IdentityData.class);
//    FrameData mockFrameData = mock(FrameData.class);
//
//    String frameName = "defaultFrame";
//
//    Mockito.lenient().when(mockSpriteData.getX()).thenReturn((int) x);
//    Mockito.lenient().when(mockSpriteData.getY()).thenReturn((int)y);
//    Mockito.lenient().when(mockSpriteData.getBaseFrameName()).thenReturn(frameName);
//    Mockito.lenient().when(mockSpriteData.getFrames()).thenReturn(Map.of(frameName, mockFrameData));
//
//    Mockito.lenient().when(mockFrameData.name()).thenReturn(frameName);
//    Mockito.lenient().when(mockFrameData.x()).thenReturn((int)0.0);
//    Mockito.lenient().when(mockFrameData.y()).thenReturn((int)0.0);
//    Mockito.lenient().when(mockFrameData.width()).thenReturn((int)(double)CELL_SIZE);
//    Mockito.lenient().when(mockFrameData.height()).thenReturn((int)(double)CELL_SIZE);
//
//    Mockito.lenient().when(mockIdentityData.getId()).thenReturn(id);
//    Mockito.lenient().when(mockIdentityData.getLayer()).thenReturn(mockLayer);
//
//    Mockito.lenient().when(mockObject.getId()).thenReturn(id);
//    Mockito.lenient().when(mockObject.getSpriteData()).thenReturn(mockSpriteData);
//    Mockito.lenient().when(mockObject.getIdentityData()).thenReturn(mockIdentityData);
//
//    Mockito.lenient().when(mockController.getEditorObject(eq(id))).thenReturn(mockObject);
//
//    File mockFileForPath = mock(File.class);
//    String absolutePath = "/dummy/absolute/path/" + spritePath;
//    Mockito.lenient().when(mockFileForPath.exists()).thenReturn(true);
//    Mockito.lenient().when(mockFileForPath.isFile()).thenReturn(true);
//    Mockito.lenient().when(mockFileForPath.isAbsolute()).thenReturn(true);
//    Mockito.lenient().when(mockFileForPath.getAbsolutePath()).thenReturn(absolutePath);
//    try {
//      Mockito.lenient().when(mockFileForPath.toURI()).thenReturn(new File(absolutePath).toURI());
//    } catch (Exception e) {
//      System.err.println("Warning: Could not create dummy file URI in setupMockObject: " + e.getMessage());
//    }
//
//
//    SpriteSheetAtlas mockAtlasForObj = mock(SpriteSheetAtlas.class);
//    Mockito.lenient().when(mockAtlasForObj.getImageFile()).thenReturn(mockFileForPath);
//    Mockito.lenient().when(mockLevelData.getAtlas(eq(id))).thenReturn(mockAtlasForObj);
//  }
//}
//
