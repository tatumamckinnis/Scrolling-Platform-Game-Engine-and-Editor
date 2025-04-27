package oogasalad.editor.model.loader;

import java.util.List;
import java.util.Map;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.object.EditorObject;
import oogasalad.editor.model.saver.api.EditorFileConverterAPI;
import oogasalad.exceptions.BlueprintParseException;
import oogasalad.exceptions.EditorLoadException;
import oogasalad.exceptions.EventParseException;
import oogasalad.exceptions.GameObjectParseException;
import oogasalad.exceptions.HitBoxParseException;
import oogasalad.exceptions.LayerParseException;
import oogasalad.exceptions.LevelDataParseException;
import oogasalad.exceptions.PropertyParsingException;
import oogasalad.exceptions.SpriteParseException;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.GameObjectData;
import oogasalad.fileparser.records.LevelData;

/**
 * LevelData Converter converts data from level data to editor object Data
 *
 * @author Alana Zinkin
 */
public class LevelDataConverter {

  /**
   * Loads level data from the specified file and populates the editor's internal object map.
   *
   * <p>This method parses the provided level file into {@link LevelData}, extracts all
   * {@link GameObjectData} and their corresponding {@link BlueprintData}, and uses an
   * {@link EditorObjectPopulator} to create {@link EditorObject}s. Each object is then added to the
   * editor's level data map for later use by the editor view or controller.</p>
   *
   * @param fileName the path to the level file to load
   * @throws LayerParseException      if there is an error parsing layer information
   * @throws LevelDataParseException  if there is an error parsing the overall level structure
   * @throws PropertyParsingException if object properties fail to parse
   * @throws SpriteParseException     if sprite information fails to load
   * @throws EventParseException      if event definitions fail to parse
   * @throws HitBoxParseException     if hit box definitions are invalid
   * @throws BlueprintParseException  if blueprint data cannot be interpreted
   * @throws GameObjectParseException if an error occurs while creating game objects
   */
  public void loadLevelData(EditorLevelData editorLevelData,
      EditorFileConverterAPI fileConverterAPI,
      String fileName) throws EditorLoadException {
    try {
      LevelData levelData = fileConverterAPI.loadFileToEditor(fileName);
      Map<Integer, BlueprintData> blueprintMap = levelData.gameBluePrintData();
      List<GameObjectData> gameObjectData = levelData.gameObjects();
      EditorObjectPopulator populator = new EditorObjectPopulator(editorLevelData);
      for (GameObjectData gameObject : gameObjectData) {
        EditorObject object = populator.populateFromGameObjectData(gameObject, blueprintMap);
        editorLevelData.updateObjectInDataMap(object.getId(), object);
      }
    } catch (LayerParseException | LevelDataParseException | PropertyParsingException |
             SpriteParseException | EventParseException | HitBoxParseException |
             BlueprintParseException | GameObjectParseException e) {
      throw new EditorLoadException(e.getMessage(), e);
    }
  }
}
