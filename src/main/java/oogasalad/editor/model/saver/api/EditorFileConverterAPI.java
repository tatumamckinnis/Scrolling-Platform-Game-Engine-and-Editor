package oogasalad.editor.model.saver.api;

import java.io.IOException;
import java.util.zip.DataFormatException;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.filesaver.savestrategy.SaverStrategy;

/**
 * Defines the API for converting Editor data to and from files. Implementations of this interface
 * are responsible for saving the current editor scene to a file, as well as loading a scene from an
 * existing file. The conversion process involves translating editor objects into a format
 * understood by the file parser and vice versa.
 *
 * @author Jacob You
 */
public interface EditorFileConverterAPI {

  /**
   * Saves the current Editor scene to a file by: 1) Gathering Editor objects from the Editor's
   * model 2) Converting them into a format recognized by GameFileParserAPI 3) Sending the write
   * operation to GameFileParserAPI
   *
   * @param editorLevelData The level data to save
   * @param fileName        The file path to save the data to
   * @param saver           The saving strategy to use
   * @throws IOException         if underlying file operations fail
   * @throws DataFormatException if data cannot be translated into the parser's model
   */
  void saveEditorDataToFile(EditorLevelData editorLevelData, String fileName, SaverStrategy saver)
      throws IOException, DataFormatException;

  /**
   * Loads an existing file into the Editor by: 1) Calling GameFileParserAPI to parse the file into
   * a standardized data structure 2) Translating that data structure into Editor-specific objects
   * 3) Populating the Editor model with these objects
   *
   * @throws IOException         if the file cannot be accessed
   * @throws DataFormatException if the file's data cannot be parsed into valid Editor objects
   */
  void loadFileToEditor() throws IOException, DataFormatException;
}
