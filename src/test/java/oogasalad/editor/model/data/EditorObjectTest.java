package oogasalad.editor.model.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import oogasalad.editor.model.data.object.EditorObject;
import oogasalad.editor.model.data.object.event.CollisionData;
import oogasalad.editor.model.data.object.HitboxData;
import oogasalad.editor.model.data.object.IdentityData;
import oogasalad.editor.model.data.object.event.CustomEventData;
import oogasalad.editor.model.data.object.event.InputData;
import oogasalad.editor.model.data.object.event.PhysicsData;
import oogasalad.editor.model.data.object.event.EventData;
import oogasalad.editor.model.data.object.sprite.SpriteData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the EditorObject class.
 * @author Jacob You
 */
class EditorObjectTest {

  private EditorLevelData mockEditorLevelData;
  private IdentityData sampleIdentityData;
  private InputData sampleInputData;
  private PhysicsData samplePhysicsData;
  private CollisionData sampleCollisionData;
  private SpriteData sampleSpriteData;
  private HitboxData sampleHitboxData;
  private CustomEventData sampleCustomData;
  private EventData sampleEventData;

  /**
   * Sets up the test environment.
   */
  @BeforeEach
  void setUp() {
    mockEditorLevelData = new EditorLevelData();
    mockEditorLevelData.getEditorConfig().setProperty("defaultHitboxWidth", "50");
    mockEditorLevelData.getEditorConfig().setProperty("defaultHitboxHeight", "100");
    mockEditorLevelData.getEditorConfig().setProperty("defaultHitboxShape", "rectangle");
   // sampleIdentityData = new IdentityData(UUID.randomUUID(), "TestName", "TestDescription");
    sampleInputData = new InputData();
    samplePhysicsData = new PhysicsData();
    sampleCollisionData = new CollisionData();
    //sampleSpriteData = new SpriteData(10, 20, "path/to/sprite");
    sampleHitboxData = new HitboxData(10, 20, 30, 40, "circle");
    sampleEventData = new EventData();
    sampleCustomData = new CustomEventData();
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
        sampleHitboxData,
        sampleCustomData,
        sampleEventData
    );
    assertEquals(sampleIdentityData, editorObject.getIdentityData());
    assertEquals(sampleInputData, editorObject.getInputData());
    assertEquals(samplePhysicsData, editorObject.getPhysicsData());
    assertEquals(sampleCollisionData, editorObject.getCollisionData());
    assertEquals(sampleSpriteData, editorObject.getSpriteData());
    assertEquals(sampleHitboxData, editorObject.getHitboxData());
    assertEquals(sampleCustomData, editorObject.getCustomEventData());
    assertEquals(sampleEventData, editorObject.getEventData());
  }

  /**
   * Tests the EditorObject constructor with only EditorLevelData.
   */
  @Test
  void constructor_whenUsingSingleParameter_shouldUseDefaultProperties() {
    EditorObject editorObject = new EditorObject(mockEditorLevelData);
    assertNotNull(editorObject.getIdentityData());
    assertEquals("Untitled", editorObject.getIdentityData().getName());
    assertEquals("", editorObject.getIdentityData().getType());
    HitboxData hitboxData = editorObject.getHitboxData();
    assertNotNull(hitboxData);
    assertEquals(0, hitboxData.getX());
    assertEquals(0, hitboxData.getY());
    assertEquals(50, hitboxData.getWidth());
    assertEquals(100, hitboxData.getHeight());
    assertEquals("rectangle", hitboxData.getShape());
    SpriteData spriteData = editorObject.getSpriteData();
    assertNotNull(spriteData);
    assertEquals(0, spriteData.getX());
    assertEquals(0, spriteData.getY());
    assertEquals(new HashMap<>(), spriteData.getFrames());
    assertEquals(new HashMap<>(), spriteData.getAnimations());
    assertEquals("", spriteData.getSpritePath());
    assertEquals(new HashMap<>(), editorObject.getCollisionData().getEvents());
    assertEquals(new HashMap<>(), editorObject.getPhysicsData().getEvents());
    assertEquals(new HashMap<>(), editorObject.getInputData().getEvents());
    assertEquals(new HashMap<>(), editorObject.getCustomEventData().getEvents());
    assertEquals(new ArrayList<>(), editorObject.getEventData().getEvents());
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

  /**
   * Tests setCustomEventData with a valid CollisionData object.
   */
  @Test
  void setCustomEventData_whenValidObject_shouldUpdateCollisionData() {
    EditorObject editorObject = new EditorObject(mockEditorLevelData);
    editorObject.setCustomEventData(sampleCustomData);
    assertEquals(sampleCustomData, editorObject.getCustomEventData());
  }

  /**
   * Tests setCustomEventData with null.
   */
  @Test
  void setCustomEventData_whenNull_shouldAcceptNull() {
    EditorObject editorObject = new EditorObject(mockEditorLevelData);
    editorObject.setCustomEventData(null);
    assertNull(editorObject.getCustomEventData());
  }

  /**
   * Tests setEventData with a valid EventData object.
   */
  @Test
  void setEventData_whenValidObject_shouldUpdateEventData() {
    EditorObject editorObject = new EditorObject(mockEditorLevelData);
    editorObject.setEventData(sampleEventData);
    assertEquals(sampleEventData, editorObject.getEventData());
  }

  /**
   * Tests setEventData with null.
   */
  @Test
  void setEventData_whenNull_shouldAcceptNull() {
    EditorObject editorObject = new EditorObject(mockEditorLevelData);
    editorObject.setEventData(null);
    assertNull(editorObject.getEventData());
  }
}
