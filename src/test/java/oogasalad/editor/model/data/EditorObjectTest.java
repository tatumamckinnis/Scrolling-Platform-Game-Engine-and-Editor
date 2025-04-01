package oogasalad.editor.model.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;
import oogasalad.editor.model.data.object.CollisionData;
import oogasalad.editor.model.data.object.EditorObject;
import oogasalad.editor.model.data.object.HitboxData;
import oogasalad.editor.model.data.object.IdentityData;
import oogasalad.editor.model.data.object.InputData;
import oogasalad.editor.model.data.object.PhysicsData;
import oogasalad.editor.model.data.object.SpriteData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the EditorObject class.
 * @author Jacob You
 */
class EditorLevelDataTest {

  private EditorLevelData mockEditorLevelData;
  private IdentityData sampleIdentityData;
  private InputData sampleInputData;
  private PhysicsData samplePhysicsData;
  private CollisionData sampleCollisionData;
  private SpriteData sampleSpriteData;
  private HitboxData sampleHitboxData;

  /**
   * Sets up the test environment.
   */
  @BeforeEach
  void setUp() {
    mockEditorLevelData = new EditorLevelData();
    mockEditorLevelData.getEditorConfig().setProperty("defaultHitboxWidth", "50");
    mockEditorLevelData.getEditorConfig().setProperty("defaultHitboxHeight", "100");
    mockEditorLevelData.getEditorConfig().setProperty("defaultHitboxShape", "rectangle");
    sampleIdentityData = new IdentityData(UUID.randomUUID(), "TestName", "TestDescription");
    sampleInputData = new InputData(new HashMap<>());
    samplePhysicsData = new PhysicsData(new HashMap<>());
    sampleCollisionData = new CollisionData(new HashMap<>());
    sampleSpriteData = new SpriteData(10, 20, "path/to/sprite");
    sampleHitboxData = new HitboxData(10, 20, 30, 40, "circle");
  }

  /**
   * Tests the EditorObject constructor with all valid parameters.
   */
  @Test
  void constructor_whenAllParametersValid_shouldInitializeFieldsProperly() {
    EditorObject editorObject = new EditorObject(
        mockEditorLevelData,
        sampleIdentityData,
        sampleInputData,
        samplePhysicsData,
        sampleCollisionData,
        sampleSpriteData,
        sampleHitboxData
    );
    assertEquals(sampleIdentityData, editorObject.getIdentityData());
    assertEquals(sampleInputData, editorObject.getInputData());
    assertEquals(samplePhysicsData, editorObject.getPhysicsData());
    assertEquals(sampleCollisionData, editorObject.getCollisionData());
    assertEquals(sampleSpriteData, editorObject.getSpriteData());
    assertEquals(sampleHitboxData, editorObject.getHitboxData());
  }

  /**
   * Tests the EditorObject constructor with only EditorLevelData.
   */
  @Test
  void constructor_whenUsingSingleParameter_shouldUseDefaultProperties() {
    EditorObject editorObject = new EditorObject(mockEditorLevelData);
    assertNotNull(editorObject.getIdentityData());
    assertEquals("Untitled", editorObject.getIdentityData().name());
    assertEquals("", editorObject.getIdentityData().group());
    HitboxData hitboxData = editorObject.getHitboxData();
    assertNotNull(hitboxData);
    assertEquals(0, hitboxData.x());
    assertEquals(0, hitboxData.y());
    assertEquals(50, hitboxData.width());
    assertEquals(100, hitboxData.height());
    assertEquals("rectangle", hitboxData.shape());
    SpriteData spriteData = editorObject.getSpriteData();
    assertNotNull(spriteData);
    assertEquals(0, spriteData.x());
    assertEquals(0, spriteData.y());
    assertEquals("", spriteData.spritePath());
    assertNull(editorObject.getCollisionData());
    assertNull(editorObject.getPhysicsData());
    assertNull(editorObject.getInputData());
  }

  /**
   * Tests the EditorObject constructor with invalid default properties.
   */
  @Test
  void constructor_whenDefaultPropertiesInvalid_shouldThrowNumberFormatException() {
    Properties props = mockEditorLevelData.getEditorConfig();
    props.setProperty("defaultHitboxWidth", "notANumber");
    props.setProperty("defaultHitboxHeight", "stillNotANumber");
    assertThrows(NumberFormatException.class, () -> new EditorObject(mockEditorLevelData));
  }

  /**
   * Tests setIdentityData with a valid IdentityData object.
   */
  @Test
  void setIdentityData_whenValidObject_shouldUpdateIdentityData() {
    EditorObject editorObject = new EditorObject(mockEditorLevelData);
    editorObject.setIdentityData(sampleIdentityData);
    assertEquals(sampleIdentityData, editorObject.getIdentityData());
  }

  /**
   * Tests setIdentityData with null.
   */
  @Test
  void setIdentityData_whenNull_shouldAcceptNull() {
    EditorObject editorObject = new EditorObject(mockEditorLevelData);
    editorObject.setIdentityData(null);
    assertNull(editorObject.getIdentityData());
  }

  /**
   * Tests setSpriteData with a valid SpriteData object.
   */
  @Test
  void setSpriteData_whenValidObject_shouldUpdateSpriteData() {
    EditorObject editorObject = new EditorObject(mockEditorLevelData);
    editorObject.setSpriteData(sampleSpriteData);
    assertEquals(sampleSpriteData, editorObject.getSpriteData());
  }

  /**
   * Tests setSpriteData with null.
   */
  @Test
  void setSpriteData_whenNull_shouldAcceptNull() {
    EditorObject editorObject = new EditorObject(mockEditorLevelData);
    editorObject.setSpriteData(null);
    assertNull(editorObject.getSpriteData());
  }

  /**
   * Tests setHitboxData with a valid HitboxData object.
   */
  @Test
  void setHitboxData_whenValidObject_shouldUpdateHitboxData() {
    EditorObject editorObject = new EditorObject(mockEditorLevelData);
    editorObject.setHitboxData(sampleHitboxData);
    assertEquals(sampleHitboxData, editorObject.getHitboxData());
  }

  /**
   * Tests setHitboxData with null.
   */
  @Test
  void setHitboxData_whenNull_shouldAcceptNull() {
    EditorObject editorObject = new EditorObject(mockEditorLevelData);
    editorObject.setHitboxData(null);
    assertNull(editorObject.getHitboxData());
  }

  /**
   * Tests setInputData and setPhysicsData with valid objects.
   */
  @Test
  void setInputDataAndPhysicsData_whenValidObjects_shouldUpdateCorrespondingFields() {
    EditorObject editorObject = new EditorObject(mockEditorLevelData);
    editorObject.setInputData(sampleInputData);
    editorObject.setPhysicsData(samplePhysicsData);
    assertEquals(sampleInputData, editorObject.getInputData());
    assertEquals(samplePhysicsData, editorObject.getPhysicsData());
  }

  /**
   * Tests setInputData and setPhysicsData with null.
   */
  @Test
  void setInputDataAndPhysicsData_whenNull_shouldAcceptNull() {
    EditorObject editorObject = new EditorObject(mockEditorLevelData);
    editorObject.setInputData(null);
    editorObject.setPhysicsData(null);
    assertNull(editorObject.getInputData());
    assertNull(editorObject.getPhysicsData());
  }

  /**
   * Tests setCollisionData with a valid CollisionData object.
   */
  @Test
  void setCollisionData_whenValidObject_shouldUpdateCollisionData() {
    EditorObject editorObject = new EditorObject(mockEditorLevelData);
    editorObject.setCollisionData(sampleCollisionData);
    assertEquals(sampleCollisionData, editorObject.getCollisionData());
  }

  /**
   * Tests setCollisionData with null.
   */
  @Test
  void setCollisionData_whenNull_shouldAcceptNull() {
    EditorObject editorObject = new EditorObject(mockEditorLevelData);
    editorObject.setCollisionData(null);
    assertNull(editorObject.getCollisionData());
  }
}
