package oogasalad.engine.model.object;

import oogasalad.fileparser.records.AnimationData;
import oogasalad.fileparser.records.FrameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SpriteTest {

  private Sprite sprite;
  private FrameData defaultFrame;
  private Map<String, FrameData> frameMap;
  private File spriteFile;

  @BeforeEach
  void setUp() {
    defaultFrame = new FrameData("idle", 0, 0, 64, 64);
    FrameData runFrame = new FrameData("run", 64, 0, 64, 64);

    frameMap = new HashMap<>();
    frameMap.put("idle", defaultFrame);
    frameMap.put("run", runFrame);

    Map<String, AnimationData> animations = new HashMap<>();
    spriteFile = new File("spritesheet.xml");

    sprite = new Sprite(frameMap, defaultFrame, animations, 10, -5, spriteFile, 90.0, false);
  }

  @Test
  void getSpriteDx_Basic_ReturnsCorrectOffset() {
    assertEquals(10, sprite.getSpriteDx());
  }

  @Test
  void getSpriteDy_Basic_ReturnsCorrectOffset() {
    assertEquals(-5, sprite.getSpriteDy());
  }


  @Test
  void getFrameMap_Basic_ReturnsCorrectSizeAndContents() {
    Map<String, FrameData> returnedMap = sprite.getFrameMap();
    assertEquals(2, returnedMap.size());
    assertTrue(returnedMap.containsKey("idle"));
    assertTrue(returnedMap.containsKey("run"));
  }

  @Test
  void getSprite_Basic_ThrowsIfFrameIdNotFound() {
    assertThrows(NoSuchElementException.class, () -> {
      sprite.getSprite("jump");
    });

  }

  @Test
  void getSprite_Basic_FileReturnsCorrectFile() {
    assertEquals(spriteFile, sprite.getSpriteFile());
  }
}
