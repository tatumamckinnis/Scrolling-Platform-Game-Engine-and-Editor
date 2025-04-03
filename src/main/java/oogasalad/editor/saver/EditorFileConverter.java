package oogasalad.editor.saver;

import java.io.IOException;
import java.util.zip.DataFormatException;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.saver.api.EditorFileConverterAPI;

public class EditorFileConverter implements EditorFileConverterAPI {

  @Override
  public void saveEditorDataToFile(EditorLevelData editorLevelData)
      throws IOException, DataFormatException {

  }

  @Override
  public void loadFileToEditor() throws IOException, DataFormatException {

  }
}
