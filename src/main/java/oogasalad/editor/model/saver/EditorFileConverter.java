package oogasalad.editor.model.saver;

import java.io.File;
import java.io.IOException;
import java.util.zip.DataFormatException;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.saver.api.EditorFileConverterAPI;
import oogasalad.exceptions.BlueprintParseException;
import oogasalad.exceptions.EditorSaveException;
import oogasalad.exceptions.EventParseException;
import oogasalad.exceptions.GameObjectParseException;
import oogasalad.exceptions.HitBoxParseException;
import oogasalad.exceptions.LayerParseException;
import oogasalad.exceptions.LevelDataParseException;
import oogasalad.exceptions.PropertyParsingException;
import oogasalad.exceptions.SpriteParseException;
import oogasalad.fileparser.DefaultFileParser;
import oogasalad.fileparser.FileParserApi;
import oogasalad.fileparser.records.LevelData;
import oogasalad.filesaver.savestrategy.SaverStrategy;

/**
 * Implements the {@link EditorFileConverterAPI} to handle conversion of editor level data to and
 * from files. This class is responsible for saving the editor's data to a file and loading data
 * from a file into the editor. The methods currently have placeholder implementations and will need
 * to be completed.
 *
 * @author Jacob You, Alana Zinkin
 */
public class EditorFileConverter implements EditorFileConverterAPI {

  @Override
  public void saveEditorDataToFile(EditorLevelData editorLevelData, String fileName,
      SaverStrategy saver)
      throws EditorSaveException {
    saver.save(EditorDataSaver.buildLevelData(editorLevelData), new File(fileName));
  }

  @Override
  public LevelData loadFileToEditor(String fileName)
      throws LayerParseException, LevelDataParseException, PropertyParsingException, SpriteParseException, EventParseException, HitBoxParseException, BlueprintParseException, GameObjectParseException {
    FileParserApi parser = new DefaultFileParser();
    return parser.parseLevelFile(fileName);
  }
}
