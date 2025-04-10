package oogasalad.editor.model.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Properties;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the EditorLevelData class.
 * @author Jacob You
 */
class EditorLevelDataTest {

  private EditorLevelData levelData;

  @BeforeEach
  void setUp() {
    levelData = new EditorLevelData();
  }

  /**
   * Tests the EditorLevelData constructor and initial state.
   */
  @Test
  void constructor_whenInitialized_shouldCreateDefaultLayerAndEmptyCollections() {
    assertNotNull(levelData.getGroups());
    assertTrue(levelData.getGroups().isEmpty());
    assertNotNull(levelData.getLayers());
    assertEquals(1, levelData.getLayers().size());
    assertEquals("New Layer", levelData.getLayers().get(0).getName());
    assertEquals(0, levelData.getLayers().get(0).getPriority());
  }

  /**
   * Tests createEditorObject with no parameters.
   */
  @Test
  void createEditorObject_whenCalled_shouldReturnValidUUIDAndRegisterObject() {
    UUID createdId = levelData.createEditorObject();
    assertNotNull(createdId);
    EditorObject retrieved = levelData.getEditorObject(createdId);
    assertNotNull(retrieved);
    assertEquals(createdId, retrieved.getIdentityData().getId());
  }

  /**
   * Tests getGroups and addGroup.
   */
  @Test
  void addGroup_whenCalled_shouldAddGroupToList() {
    levelData.addGroup("GroupA");
    assertTrue(levelData.getGroups().contains("GroupA"));
  }

  /**
   * Tests removeGroup when the group is in use by some EditorObject.
   */
  @Test
  void removeGroup_whenGroupIsInUse_shouldReturnFalseAndNotRemove() {
    UUID id = levelData.createEditorObject();
    EditorObject obj = levelData.getEditorObject(id);
    obj.getIdentityData().setGroup("InUseGroup");
    levelData.addGroup("InUseGroup");
    assertFalse(levelData.removeGroup("InUseGroup"));
    assertTrue(levelData.getGroups().contains("InUseGroup"));
  }

  /**
   * Tests removeGroup when the group is unused.
   */
  @Test
  void removeGroup_whenGroupIsUnused_shouldRemoveGroupAndReturnTrue() {
    levelData.addGroup("UnusedGroup");
    assertTrue(levelData.removeGroup("UnusedGroup"));
    assertFalse(levelData.getGroups().contains("UnusedGroup"));
  }

  /**
   * Tests addLayer to ensure layers are inserted according to descending priority.
   */
  @Test
  void addLayer_whenCalled_shouldInsertLayerByPriority() {

    Layer midLayer = new Layer("midLayer", 5);
    Layer highLayer = new Layer("highLayer", 10);
    Layer lowLayer = new Layer("lowLayer", -1);

    levelData.addLayer(midLayer);
    levelData.addLayer(highLayer);
    levelData.addLayer(lowLayer);

    List<Layer> layers = levelData.getLayers();
    // Current layers should be inserted in the order of descending priority: highest first
    // So "layer0"(0) was there initially, but "highLayer"(10) > midLayer(5) > layer0(0) > lowLayer(-1)
    assertEquals("highLayer", layers.get(0).getName());
    assertEquals("midLayer", layers.get(1).getName());
    assertEquals("New Layer", layers.get(2).getName());
    assertEquals("lowLayer", layers.get(3).getName());
  }

  /**
   * Tests removeLayer when layer is empty and can be removed.
   */
  @Test
  void removeLayer_whenLayerIsEmpty_shouldRemoveAndReturnTrue() {
    Layer removableLayer = new Layer("removableLayer", 1);
    levelData.addLayer(removableLayer);
    assertTrue(levelData.removeLayer("removableLayer"));
  }

  /**
   * Tests removeLayer when layer is not empty and cannot be removed.
   */
  @Test
  void removeLayer_whenLayerHasObjects_shouldReturnFalse() {
    UUID newObjId = levelData.createEditorObject();
    Layer defaultLayer = levelData.getLayers().get(0);
    assertEquals("New Layer", defaultLayer.getName());
    assertFalse(levelData.removeLayer("layer0"));
  }

  /**
   * Tests getEditorObject with an invalid UUID.
   */
  @Test
  void getEditorObject_whenUUIDNotPresent_shouldReturnNull() {
    assertNull(levelData.getEditorObject(UUID.randomUUID()));
  }

  /**
   * Tests getEditorConfig to ensure properties are loaded.
   */
  @Test
  void getEditorConfig_whenCalled_shouldReturnNonNullProperties() {
    Properties props = levelData.getEditorConfig();
    assertNotNull(props);
  }
}
