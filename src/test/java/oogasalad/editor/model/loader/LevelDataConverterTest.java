package oogasalad.editor.model.loader;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import oogasalad.editor.controller.level.EditorDataAPI;
import oogasalad.editor.controller.listeners.EditorListenerNotifier;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.Layer;
import oogasalad.editor.model.loader.LevelDataConverter;
import oogasalad.editor.model.saver.api.EditorFileConverterAPI;
import oogasalad.exceptions.EditorSaveException;
import oogasalad.fileparser.records.CameraData;
import oogasalad.fileparser.records.LevelData;
import oogasalad.filesaver.savestrategy.SaverStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for LevelDataConverter focusing on camera-loading behavior.
 * Author: Jacob
 */
public class LevelDataConverterTest {

  private EditorLevelData editorLevelData;
  private TestDataAPI dataAPI;
  private TestFileConverter fileConverter;
  private LevelDataConverter converter;

  /**
   * Initialize fresh converter, data API stub, and file-converter stub before each test.
   */
  @BeforeEach
  public void setUp() {
    editorLevelData = new EditorLevelData();
    dataAPI = new TestDataAPI(editorLevelData);
    fileConverter = new TestFileConverter();
    converter = new LevelDataConverter();
  }

  /**
   * When LevelData includes camera info, loadLevelData should copy it into EditorLevelData.
   */
  @Test
  public void loadLevelData_WithCameraInLevelData_UpdatesEditorCamera() throws Exception {
    CameraData sourceCamera = new CameraData(
        "AutoScroller",
        Map.of("objectToTrack", "objectID"),
        Map.of("cameraOffsetX", 100.0)
    );
    LevelData levelData = new LevelData(
        "levelName",
        0, 0, 0, 0,
        sourceCamera,
        Map.of(),
        List.of()
    );
    fileConverter.setLevelData(levelData);

    converter.loadLevelData(dataAPI, editorLevelData, fileConverter, "ignoredPath");

    var cam = editorLevelData.getCameraData();
    assertEquals("AutoScroller", cam.getCameraType());
    assertEquals(100.0, cam.getDoubleParams().get("cameraOffsetX"));
    assertEquals("objectID", cam.getStringParam("objectToTrack"));
  }

  /**
   * Simple stub implementing EditorDataAPI by returning a fixed EditorLevelData.
   */
  private static class TestDataAPI extends EditorDataAPI {
    private final EditorLevelData level;
    TestDataAPI(EditorLevelData level) {
      super(new EditorListenerNotifier());
      this.level = level; }
    @Override public EditorLevelData getLevel() { return level; }
  }

  /**
   * Simple stub implementing EditorFileConverterAPI by returning a preset LevelData.
   */
  private static class TestFileConverter implements EditorFileConverterAPI {
    private LevelData toReturn;
    void setLevelData(LevelData ld) { this.toReturn = ld; }

    /**
     * Saves the current Editor scene to a file by: 1) Gathering Editor objects from the Editor's
     * model 2) Converting them into a format recognized by GameFileParserAPI 3) Sending the write
     * operation to GameFileParserAPI
     *
     * @param editorLevelData The level data to save
     * @param fileName        The file path to save the data to
     * @param saver           The saving strategy to use
     */
    @Override
    public void saveEditorDataToFile(EditorLevelData editorLevelData, String fileName,
        SaverStrategy saver) throws EditorSaveException {

    }

    @Override public LevelData loadFileToEditor(String path) { return toReturn; }
  }
}
