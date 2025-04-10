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

    sprite = new Sprite(frameMap, defaultFrame, animations, 10, -5, spriteFile);
  }

  @Test
  void getSpriteDxReturnsCorrectOffset() {
    assertEquals(10, sprite.getSpriteDx());
  }

  @Test
  void getSpriteDyReturnsCorrectOffset() {
    assertEquals(-5, sprite.getSpriteDy());
  }


  @Test
  void getFrameMapReturnsCorrectSizeAndContents() {
    Map<String, FrameData> returnedMap = sprite.getFrameMap();
    assertEquals(2, returnedMap.size());
    assertTrue(returnedMap.containsKey("idle"));
    assertTrue(returnedMap.containsKey("run"));
  }

  @Test
  void getSpriteThrowsIfFrameIdNotFound() {
    assertThrows(NoSuchElementException.class, () -> {
      sprite.getSprite("jump");
    });

  }

  @Test
  void getSpriteFileReturnsCorrectFile() {
    assertEquals(spriteFile, sprite.getSpriteFile());
  }
}
