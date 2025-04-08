package oogasalad.editor.view.tools;

import oogasalad.editor.controller.EditorController;
import oogasalad.editor.view.EditorGameView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the GameObjectPlacementTool class.
 * Uses Mockito for mocking dependencies.
 */
class GameObjectPlacementToolTest {

  private static final int TEST_CELL_SIZE = 32;
  private static final String TEST_OBJECT_GROUP = "PLAYER";
  private static final String TEST_NAME_PREFIX = "Player_";

  @Mock
  private EditorGameView mockGameView;

  @Mock
  private EditorController mockEditorController;

  /**
   * Argument captor for the object group passed to requestObjectPlacement.
   */
  @Captor private ArgumentCaptor<String> objectGroupCaptor;
  /**
   * Argument captor for the name prefix passed to requestObjectPlacement.
   */
  @Captor private ArgumentCaptor<String> namePrefixCaptor;
  /**
   * Argument captor for the x-coordinate passed to requestObjectPlacement.
   */
  @Captor private ArgumentCaptor<Integer> xCaptor;
  /**
   * Argument captor for the y-coordinate passed to requestObjectPlacement.
   */
  @Captor private ArgumentCaptor<Integer> yCaptor;
  /**
   * Argument captor for the size passed to requestObjectPlacement.
   */
  @Captor private ArgumentCaptor<Integer> sizeCaptor;

  private GameObjectPlacementTool placementTool;

  /**
   * Sets up the test environment before each test method.
   * Initializes Mockito mocks, configures the mock game view, and creates an instance of GameObjectPlacementTool.
   */
  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    when(mockGameView.getCellSize()).thenReturn(TEST_CELL_SIZE);

    placementTool = new GameObjectPlacementTool(
        mockGameView,
        mockEditorController,
        TEST_OBJECT_GROUP,
        TEST_NAME_PREFIX
    );
  }

  /**
   * Tests that the placeObjectAt method calls the editor controller with the correct parameters
   * based on the provided grid coordinates and the cell size from the game view.
   */
  @Test
  void testPlaceObjectAtCallsControllerWithCorrectParameters() {
    int testGridX = 5;
    int testGridY = 10;
    int expectedWorldX = testGridX * TEST_CELL_SIZE;
    int expectedWorldY = testGridY * TEST_CELL_SIZE;
    int expectedCellSize = TEST_CELL_SIZE;

    placementTool.placeObjectAt(testGridX, testGridY);

    verify(mockEditorController, times(1)).requestObjectPlacement(
        objectGroupCaptor.capture(),
        namePrefixCaptor.capture(),
        xCaptor.capture(),
        yCaptor.capture(),
        sizeCaptor.capture()
    );

    assertEquals(TEST_OBJECT_GROUP, objectGroupCaptor.getValue(), "Object group should match");
    assertEquals(TEST_NAME_PREFIX, namePrefixCaptor.getValue(), "Name prefix should match");
    assertEquals(expectedWorldX, xCaptor.getValue(), "Calculated World X coordinate should match");
    assertEquals(expectedWorldY, yCaptor.getValue(), "Calculated World Y coordinate should match");
    assertEquals(expectedCellSize, sizeCaptor.getValue(), "Cell size should match");
  }

  /**
   * Tests the placeObjectAt method with zero grid coordinates to ensure the world coordinates are also zero.
   */
  @Test
  void testPlaceObjectAtZeroCoordinates() {
    int testGridX = 0;
    int testGridY = 0;
    int expectedWorldX = 0;
    int expectedWorldY = 0;

    placementTool.placeObjectAt(testGridX, testGridY);

    verify(mockEditorController).requestObjectPlacement(
        anyString(), anyString(), eq(expectedWorldX), eq(expectedWorldY), eq(TEST_CELL_SIZE)
    );
  }

  /**
   * Tests the placeObjectAt method with negative grid coordinates to ensure the world coordinates are calculated correctly.
   */
  @Test
  void testPlaceObjectAtNegativeCoordinates() {
    int testGridX = -2;
    int testGridY = -3;
    int expectedWorldX = testGridX * TEST_CELL_SIZE;
    int expectedWorldY = testGridY * TEST_CELL_SIZE;

    placementTool.placeObjectAt(testGridX, testGridY);

    verify(mockEditorController).requestObjectPlacement(
        anyString(), anyString(), eq(expectedWorldX), eq(expectedWorldY), eq(TEST_CELL_SIZE)
    );
  }

  /**
   * Tests the behavior of placeObjectAt when the game view returns a zero cell size.
   * In this case, the controller should not be called.
   */
  @Test
  void testPlaceObjectAtWithZeroCellSize() {
    when(mockGameView.getCellSize()).thenReturn(0);
    int testGridX = 5;
    int testGridY = 10;

    placementTool = new GameObjectPlacementTool(
        mockGameView, mockEditorController, TEST_OBJECT_GROUP, TEST_NAME_PREFIX
    );

    placementTool.placeObjectAt(testGridX, testGridY);

    verify(mockEditorController, never()).requestObjectPlacement(any(), any(), anyInt(), anyInt(), anyInt());
  }

  /**
   * Tests the behavior of placeObjectAt when the game view returns a negative cell size.
   * In this case, the controller should not be called.
   */
  @Test
  void testPlaceObjectAtWithNegativeCellSize() {
    when(mockGameView.getCellSize()).thenReturn(-10);
    int testGridX = 5;
    int testGridY = 10;

    placementTool = new GameObjectPlacementTool(
        mockGameView, mockEditorController, TEST_OBJECT_GROUP, TEST_NAME_PREFIX
    );

    placementTool.placeObjectAt(testGridX, testGridY);

    verify(mockEditorController, never()).requestObjectPlacement(any(), any(), anyInt(), anyInt(), anyInt());
  }
}