package oogasalad.editor.model.saver;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import oogasalad.editor.model.data.Layer;
import oogasalad.editor.model.data.EditorObject;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.object.HitboxData;
import oogasalad.editor.model.data.object.IdentityData;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.LevelData;

/**
 * Unit tests for the EditorDataSaver utility.
 * @author Jacob You
 */
class EditorDataSaverTest {

  private EditorLevelData levelMock;
  private EditorObject objA;
  private EditorObject objB;
  private BlueprintData bpA;
  private BlueprintData bpB;

  /**
   * Sets up a mock EditorLevelData with two objects before each test.
   */
  @BeforeEach
  void setUp() {
    levelMock = mock(EditorLevelData.class);

    Layer layer0 = new Layer("layer0", 0);
    IdentityData idA = new IdentityData(UUID.randomUUID(), "ObjA", "", layer0);
    IdentityData idB = new IdentityData(UUID.randomUUID(), "ObjB", "", layer0);

    HitboxData hb = new HitboxData(10, 20, 30, 40, "rectangle");

    objA = mock(EditorObject.class, RETURNS_DEEP_STUBS);
    objB = mock(EditorObject.class, RETURNS_DEEP_STUBS);

    when(objA.getIdentityData()).thenReturn(idA);
    when(objB.getIdentityData()).thenReturn(idB);
    when(objA.getHitboxData()).thenReturn(hb);
    when(objB.getHitboxData()).thenReturn(hb);

    Map<UUID, EditorObject> map = new LinkedHashMap<>();
    map.put(idA.getId(), objA);
    map.put(idB.getId(), objB);
    when(levelMock.getObjectDataMap()).thenReturn(map);
    when(levelMock.getBounds()).thenReturn(new int[]{0, 0, 640, 480});
    when(levelMock.getLevelName()).thenReturn("TestLevel");

    bpA = new BlueprintData(-1, 0, 0, 0, false, "A", "", "", null, null,
        List.of(), Map.of(), Map.of(), List.of());
    bpB = new BlueprintData(-1, 0, 0, 0, false, "B", "", "", null, null,
        List.of(), Map.of(), Map.of(), List.of());
  }

  /**
   * Tests buildLevelData with two distinct objects.
   */
  @Test
  void buildLevelData_TwoObjectsDistinctBlueprints_ShouldReturnTwoBlueprints() {
    try (MockedStatic<BlueprintBuilder> mockStatic = mockStatic(BlueprintBuilder.class)) {
      mockStatic.when(() -> BlueprintBuilder.fromEditorObject(objA)).thenReturn(bpA);
      mockStatic.when(() -> BlueprintBuilder.fromEditorObject(objB)).thenReturn(bpB);

      LevelData data = EditorDataSaver.buildLevelData(levelMock);

      assertEquals("TestLevel", data.name());
      assertEquals(2, data.gameBluePrintData().size());
      assertEquals(2, data.gameObjects().size());
    }
  }

  /**
   * Tests buildLevelData when BlueprintBuilder returns identical blueprints for both objects.
   */
  @Test
  void buildLevelData_DuplicateBlueprints_ShouldNotAssignUniqueIdsPerObject() {
    try (MockedStatic<BlueprintBuilder> mockStatic = mockStatic(BlueprintBuilder.class)) {
      mockStatic.when(() -> BlueprintBuilder.fromEditorObject(any())).thenReturn(bpA);

      LevelData data = EditorDataSaver.buildLevelData(levelMock);

      assertEquals(1, data.gameBluePrintData().size());
      assertEquals(2, data.gameObjects().size());
    }
  }

  /**
   * Tests buildLevelData with an empty EditorLevelData.
   */
  @Test
  void buildLevelData_EmptyLevel_ShouldReturnEmptyCollections() {
    EditorLevelData emptyLevel = mock(EditorLevelData.class);
    when(emptyLevel.getObjectDataMap()).thenReturn(new HashMap<>());
    when(emptyLevel.getBounds()).thenReturn(new int[]{0, 0, 0, 0});
    when(emptyLevel.getLevelName()).thenReturn("Empty");

    LevelData data = EditorDataSaver.buildLevelData(emptyLevel);

    assertTrue(data.gameBluePrintData().isEmpty());
    assertTrue(data.gameObjects().isEmpty());
  }

  /**
   * Tests flipMapping returns correct reverse mapping.
   */
  @Test
  void flipMapping_ValidInput_ShouldReturnReversedMap()
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
    Map<BlueprintData, Integer> map = new HashMap<>();
    map.put(bpA.withId(1), 1);
    map.put(bpB.withId(2), 2);

    Map<Integer, BlueprintData> flipped =
        invokeFlipMapping(map);

    assertEquals(bpA.withId(1), flipped.get(1));
    assertEquals(bpB.withId(2), flipped.get(2));
  }

  @SuppressWarnings("unchecked")
  private Map<Integer, BlueprintData> invokeFlipMapping(Map<BlueprintData, Integer> in)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    var m = EditorDataSaver.class.getDeclaredMethod("flipMapping", Map.class);
    m.setAccessible(true);
    return (Map<Integer, BlueprintData>) m.invoke(null, in);
  }
}
