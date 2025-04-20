package oogasalad.editor.model.saver;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import oogasalad.editor.model.data.EditorObject;
import oogasalad.editor.model.data.Layer;
import oogasalad.editor.model.data.object.HitboxData;
import oogasalad.editor.model.data.object.IdentityData;
import oogasalad.editor.model.data.object.event.EditorEvent;
import oogasalad.editor.model.data.object.event.ExecutorData;
import oogasalad.editor.model.data.object.sprite.AnimationData;
import oogasalad.editor.model.data.object.sprite.FrameData;
import oogasalad.editor.model.data.object.sprite.SpriteData;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.EventData;

/**
 * Unit tests for the BlueprintBuilder converter.
 * @author Jacob You
 */
class BlueprintBuilderTest {

  private EditorObject editorObject;
  private SpriteData spriteMock;
  private HitboxData hitboxMock;
  private IdentityData identityMock;

  /**
   * Sets up a fully populated EditorObject mock before each test.
   */
  @BeforeEach
  void setUp() {
    // Sprite mock
    spriteMock = mock(SpriteData.class);
    when(spriteMock.getName()).thenReturn("PlayerSprite");
    when(spriteMock.getSpritePath()).thenReturn("assets/player.png");
    when(spriteMock.getRotation()).thenReturn(45.0);
    when(spriteMock.getX()).thenReturn(200);
    when(spriteMock.getY()).thenReturn(300);

    Map<String, FrameData> frameMap = new LinkedHashMap<>();
    frameMap.put("idle", new FrameData("idle", 0, 0, 32, 32));
    when(spriteMock.getFrames()).thenReturn(frameMap);

    Map<String, AnimationData> animMap = new LinkedHashMap<>();
    animMap.put("walk", new AnimationData("walk", 0.1, List.of("idle")));
    when(spriteMock.getAnimations()).thenReturn(animMap);

    // Hitbox mock
    hitboxMock = mock(HitboxData.class);
    when(hitboxMock.getShape()).thenReturn("rectangle");
    when(hitboxMock.getWidth()).thenReturn(20);
    when(hitboxMock.getHeight()).thenReturn(30);
    when(hitboxMock.getX()).thenReturn(190);
    when(hitboxMock.getY()).thenReturn(285);

    // Identity mock
    identityMock = mock(IdentityData.class);
    when(identityMock.getName()).thenReturn("Player");
    when(identityMock.getType()).thenReturn("Character");
    when(identityMock.getId()).thenReturn(UUID.randomUUID());
    when(identityMock.getLayer()).thenReturn(new Layer("layer0", 0));

    // Event maps
    Map<String, EditorEvent> emptyMap = new LinkedHashMap<>();
    Map<String, EditorEvent> collisionEvents = new LinkedHashMap<>();
    collisionEvents.put("hitWall", buildEvent("Collision"));
    Map<String, EditorEvent> inputEvents = new LinkedHashMap<>();
    inputEvents.put("jump", buildEvent("Input"));

    // EditorObject mock (deep‑stub)
    editorObject = mock(EditorObject.class, RETURNS_DEEP_STUBS);
    when(editorObject.getIdentityData()).thenReturn(identityMock);
    when(editorObject.getSpriteData()).thenReturn(spriteMock);
    when(editorObject.getHitboxData()).thenReturn(hitboxMock);
    when(editorObject.getCollisionData().getEvents()).thenReturn(collisionEvents);
    when(editorObject.getInputData().getEvents()).thenReturn(inputEvents);
    when(editorObject.getPhysicsData().getEvents()).thenReturn(emptyMap);
    when(editorObject.getCustomEventData().getEvents()).thenReturn(emptyMap);
    when(editorObject.getEventData().getEvents()).thenReturn(List.of("hitWall", "jump"));
  }

  /**
   * Tests fromEditorObject with a fully populated EditorObject.
   */
  @Test
  void fromEditorObject_AllFieldsValid_ShouldMapToBlueprintDataCorrectly() {
    BlueprintData bp = BlueprintBuilder.fromEditorObject(editorObject);

    assertEquals(-1, bp.blueprintId());
    assertEquals("Player", bp.gameName());
    assertEquals("Character", bp.type());
    assertEquals("assets/player.png", bp.spriteData().spriteFile().getPath());
    assertEquals(10, bp.hitBoxData().spriteDx());   // 200‑190
    assertEquals(15, bp.hitBoxData().spriteDy());   // 300‑285
    assertEquals(2, bp.eventDataList().size());
    assertEquals("hitWall", bp.eventDataList().get(0).eventId());
    assertEquals("jump", bp.eventDataList().get(1).eventId());
  }

  /**
   * Tests fromEditorObject when frames and animations are empty.
   */
  @Test
  void fromEditorObject_EmptyFramesAndAnimations_ShouldHandleGracefully() {
    when(spriteMock.getFrames()).thenReturn(new HashMap<>());
    when(spriteMock.getAnimations()).thenReturn(new HashMap<>());
    BlueprintData bp = BlueprintBuilder.fromEditorObject(editorObject);
    assertNull(bp.spriteData().baseImage());
    assertTrue(bp.spriteData().frames().isEmpty());
    assertTrue(bp.spriteData().animations().isEmpty());
  }

  /**
   * Tests fromEditorObject with null argument.
   */
  @Test
  void fromEditorObject_NullEditorObject_ShouldThrowNullPointerException() {
    assertThrows(NullPointerException.class, () -> BlueprintBuilder.fromEditorObject(null));
  }

  /**
   * Tests event ordering respects the order list in EditorObject.
   */
  @Test
  void fromEditorObject_EventOrder_ShouldFollowProvidedOrder() {
    when(editorObject.getEventData().getEvents()).thenReturn(List.of("jump", "hitWall"));
    BlueprintData bp = BlueprintBuilder.fromEditorObject(editorObject);
    assertEquals("jump", bp.eventDataList().get(0).eventId());
    assertEquals("hitWall", bp.eventDataList().get(1).eventId());
  }

  private EditorEvent buildEvent(String typeLabel) {
    EditorEvent ev = mock(EditorEvent.class, RETURNS_DEEP_STUBS);
    ExecutorData ex = mock(ExecutorData.class);
    when(ex.getExecutorName()).thenReturn(typeLabel + "Exec");
    when(ex.getStringParams()).thenReturn(Map.of("k", "v"));
    when(ex.getDoubleParams()).thenReturn(Map.of("d", 1.0));
    when(ev.getConditions()).thenReturn(List.of(List.of(ex)));
    when(ev.getOutcomes()).thenReturn(List.of(ex));
    return ev;
  }
}
