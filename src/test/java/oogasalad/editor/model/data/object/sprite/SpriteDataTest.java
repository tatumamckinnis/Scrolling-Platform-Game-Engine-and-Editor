package oogasalad.editor.model.data.object.sprite;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for the SpriteData class.
 * Author: Jacob
 */
class SpriteDataTest {

  SpriteData spriteData;
  Map<String, FrameData> frames;
  Map<String, AnimationData> animations;

  /**
   * Sets up the test environment.
   */
  @BeforeEach
  void setUp() {
    frames = new HashMap<>();
    frames.put("frame1", new FrameData("frame1", 0, 0, 100, 100));
    frames.put("frame2", new FrameData("frame2", 100, 0, 100, 100));

    animations = new HashMap<>();
    animations.put("idle", new AnimationData("idle", 0.5, new ArrayList<>(List.of("frame1"))));
    animations.put("walk", new AnimationData("walk", 0.25, new ArrayList<>(List.of("frame1", "frame2"))));

    spriteData = new SpriteData("", 10, 20, 0, false, frames, animations, "sprite");
  }

  /**
   * Tests constructor initializes fields correctly.
   */
  @Test
  void constructor_withValidData_initializesFieldsProperly() {
    assertEquals(10, spriteData.getX());
    assertEquals(20, spriteData.getY());
    assertEquals("path/to/sprite.png", spriteData.getSpritePath());
    assertEquals(frames, spriteData.getFrames());
    assertEquals(animations, spriteData.getAnimations());
  }

  /**
   * Tests getFrame returns the correct FrameData.
   */
  @Test
  void getFrame_whenFrameExists_returnsFrameData() {
    FrameData result = spriteData.getFrame("frame1");
    assertNotNull(result);
    assertEquals("frame1", result.name());
  }

  /**
   * Tests getFrame returns null when frame does not exist.
   */
  @Test
  void getFrame_whenFrameNotExist_returnsNull() {
    assertNull(spriteData.getFrame("nonexistent"));
  }

  /**
   * Tests getAnimation returns the correct AnimationData.
   */
  @Test
  void getAnimation_whenAnimationExists_returnsAnimationData() {
    AnimationData result = spriteData.getAnimation("walk");
    assertNotNull(result);
    assertEquals(0.25, result.getFrameLength());
    assertEquals(List.of("frame1", "frame2"), result.getFrameNames());
  }

  /**
   * Tests getAnimation returns null when animation does not exist.
   */
  @Test
  void getAnimation_whenAnimationNotExist_returnsNull() {
    assertNull(spriteData.getAnimation("jump"));
  }

  /**
   * Tests getAnimationFrameNames returns frame names.
   */
  @Test
  void getAnimationFrameNames_whenAnimationExists_returnsFrameNames() {
    List<String> frameNames = spriteData.getAnimationFrameNames("walk");
    assertEquals(List.of("frame1", "frame2"), frameNames);
  }

  /**
   * Tests getAnimationFrameLength returns correct length.
   */
  @Test
  void getAnimationFrameLength_whenAnimationExists_returnsFrameLength() {
    assertEquals(0.5, spriteData.getAnimationFrameLength("idle"));
  }

  /**
   * Tests setX and setY update coordinates.
   */
  @Test
  void setXAndSetY_whenCalled_updatesCoordinates() {
    spriteData.setX(40);
    spriteData.setY(50);
    assertEquals(40, spriteData.getX());
    assertEquals(50, spriteData.getY());
  }

  /**
   * Tests setFrames and setAnimations update fields.
   */
  @Test
  void setFramesAndSetAnimations_whenCalled_updatesMaps() {
    Map<String, FrameData> newFrames = new HashMap<>();
    newFrames.put("frameA", new FrameData("frameA", 0, 0, 50, 50));
    Map<String, AnimationData> newAnims = new HashMap<>();
    newAnims.put("run", new AnimationData("run", 0.1, new ArrayList<>(List.of("frameA"))));

    spriteData.setFrames(newFrames);
    spriteData.setAnimations(newAnims);

    assertEquals(newFrames, spriteData.getFrames());
    assertEquals(newAnims, spriteData.getAnimations());
  }

  /**
   * Tests setAnimationFrameLength updates frame length.
   */
  @Test
  void setAnimationFrameLength_whenCalled_updatesFrameLength() {
    spriteData.setAnimationFrameLength("idle", 1.0);
    assertEquals(1.0, spriteData.getAnimation("idle").getFrameLength());
  }

  /**
   * Tests addFrame adds a new FrameData.
   */
  @Test
  void addFrame_whenCalled_shouldAddFrameToMap() {
    FrameData newFrame = new FrameData("frame3", 200, 0, 100, 100);
    spriteData.addFrame("frame3", newFrame);
    assertEquals(newFrame, spriteData.getFrame("frame3"));
  }

  /**
   * Tests removeFrame removes the specified FrameData.
   */
  @Test
  void removeFrame_whenFrameExists_shouldRemoveFrame() {
    spriteData.removeFrame("frame1", frames.get("frame1"));
    assertNull(spriteData.getFrame("frame1"));
  }

  /**
   * Tests removeFrame on a frame that doesn't exist (negative test).
   */
  @Test
  void removeFrame_whenFrameNotExist_shouldNotThrowException() {
    assertDoesNotThrow(() -> spriteData.removeFrame("nonexistent", null));
  }

  /**
   * Tests renameFrame changes the key in the frames map.
   */
  @Test
  void renameFrame_whenOldNameExists_shouldRenameFrame() {
    spriteData.renameFrame("frame1", "frame1_renamed");
    assertNotNull(spriteData.getFrame("frame1_renamed"));
    assertNull(spriteData.getFrame("frame1"));
  }

  /**
   * Tests renameFrame when old frame name does not exist (negative test).
   */
  @Test
  void renameFrame_whenOldNameNotExist_shouldNotThrowException() {
    assertDoesNotThrow(() -> spriteData.renameFrame("nonexistent", "stillNonexistent"));
  }

  /**
   * Tests addAnimationFrame adds frame to animation's list.
   */
  @Test
  void addAnimationFrame_whenCalled_shouldAddFrameNameToAnimation() {
    spriteData.addAnimationFrame("idle", "frame2");
    assertTrue(spriteData.getAnimation("idle").getFrameNames().contains("frame2"));
  }

  /**
   * Tests removeAnimationFrame removes frame from animation's list.
   */
  @Test
  void removeAnimationFrame_whenCalled_shouldRemoveFrameNameFromAnimation() {
    spriteData.removeAnimationFrame("walk", "frame2");
    assertFalse(spriteData.getAnimation("walk").getFrameNames().contains("frame2"));
  }

  /**
   * Tests renameAnimation updates the key in the animations map.
   */
  @Test
  void renameAnimation_whenCalled_shouldRenameAnimation() {
    spriteData.renameAnimation("walk", "run");
    assertNull(spriteData.getAnimations().get("walk"));
    assertNotNull(spriteData.getAnimations().get("run"));
  }

  /**
   * Tests setFrameLength updates frameLength in the animation.
   */
  @Test
  void setFrameLength_whenCalled_updatesAnimationFrameLength() {
    spriteData.setFrameLength("walk", 2.0);
    assertEquals(2.0, spriteData.getAnimation("walk").getFrameLength());
  }

  /**
   * Tests setSpritePath updates the sprite path.
   */
  @Test
  void setSpritePath_whenCalled_updatesPath() {
    spriteData.setSpritePath("new/path.png");
    assertEquals("new/path.png", spriteData.getSpritePath());
  }
}
