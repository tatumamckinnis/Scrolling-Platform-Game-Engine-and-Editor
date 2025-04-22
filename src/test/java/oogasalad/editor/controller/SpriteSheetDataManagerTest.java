package oogasalad.editor.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.List;
import javafx.geometry.Rectangle2D;
import oogasalad.editor.model.data.object.sprite.FrameData;
import oogasalad.editor.model.saver.SpriteSheetSaver;
import oogasalad.editor.view.panes.spriteCreation.SpriteRegion;
import oogasalad.exceptions.SpriteSheetSaveException;
import oogasalad.filesaver.savestrategy.SaverStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Unit tests for SpriteSheetDataManager.
 * @author Jacob You
 */
class SpriteSheetDataManagerTest {

  private SpriteSheetSaver saverMock;
  private SaverStrategy strategyMock;
  private SpriteSheetDataManager manager;
  private File tempFile;

  /**
   * Sets up mocks and a temporary file before each test.
   */
  @BeforeEach
  void setUp() throws Exception {
    manager = new SpriteSheetDataManager(null);
    tempFile = File.createTempFile("sheet_test", ".xml");
    tempFile.deleteOnExit();
  }

  /**
   * Tests saveSpriteSheet converts regions to FrameData and delegates to saver.
   */
  @Test
  void saveSpriteSheet_WithOneRegion_DelegatesCorrectly() throws Exception {
    SpriteRegion region = mock(SpriteRegion.class);
    when(region.getName()).thenReturn("heroIdle");
    when(region.getBounds()).thenReturn(new Rectangle2D(10, 20, 30, 40));

    ArgumentCaptor<List<FrameData>> captor = ArgumentCaptor.forClass(List.class);

    manager.saveSpriteSheet("atlas.png", 1024, 768, List.of(region), tempFile);

    verify(saverMock).save(eq("atlas.png"), eq(1024), eq(768),
        captor.capture(), eq(tempFile), eq(strategyMock));

    List<FrameData> frames = captor.getValue();
    assertEquals(1, frames.size());
    FrameData f = frames.get(0);
    assertEquals("heroIdle", f.name());
    assertEquals(10, f.x());
    assertEquals(20, f.y());
    assertEquals(30, f.width());
    assertEquals(40, f.height());
  }

  /**
   * Tests saveSpriteSheet with empty region list passes an empty FrameData list.
   */
  @Test
  void saveSpriteSheet_EmptyRegionList_PassesEmptyFrames() throws Exception {
    ArgumentCaptor<List<FrameData>> captor = ArgumentCaptor.forClass(List.class);

    manager.saveSpriteSheet("atlas.png", 512, 512, List.of(), tempFile);

    verify(saverMock).save(eq("atlas.png"), eq(512), eq(512),
        captor.capture(), eq(tempFile), eq(strategyMock));

    assertTrue(captor.getValue().isEmpty());
  }

  /**
   * Tests saveSpriteSheet propagates SpriteSheetSaveException from saver.
   */
  @Test
  void saveSpriteSheet_SaverThrows_PropagatesException() throws Exception {
    doThrow(new SpriteSheetSaveException("fail"))
        .when(saverMock).save(anyString(), anyInt(), anyInt(),
            anyList(), any(File.class), any(SaverStrategy.class));

    assertThrows(SpriteSheetSaveException.class, () ->
        manager.saveSpriteSheet("bad.png", 256, 256, List.of(), tempFile));
  }
}
