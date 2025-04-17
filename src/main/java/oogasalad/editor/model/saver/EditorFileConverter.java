package oogasalad.editor.model.saver;

import java.io.File;
import java.io.IOException;
import java.util.zip.DataFormatException;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.saver.api.EditorFileConverterAPI;
import oogasalad.filesaver.savestrategy.SaverStrategy;

/**
 * Implements the {@link EditorFileConverterAPI} to handle conversion of editor level data to and
 * from files. This class is responsible for saving the editor's data to a file and loading data
 * from a file into the editor. The methods currently have placeholder implementations and will need
 * to be completed.
 *
 * @author Jacob You
 */
public class EditorFileConverter implements EditorFileConverterAPI {

  @Override
  public void saveEditorDataToFile(EditorLevelData editorLevelData, String fileName,
      SaverStrategy saver)
      throws IOException, DataFormatException {
    saver.save(EditorDataSaver.buildLevelData(editorLevelData), new File(fileName));
  }

  @Override
  public void loadFileToEditor() throws IOException, DataFormatException {
    // TODO: Implement loading
  }
}
