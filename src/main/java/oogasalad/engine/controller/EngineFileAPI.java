package oogasalad.engine.controller;

import java.io.IOException;
import java.util.zip.DataFormatException;
import oogasalad.game.file.parser.records.LevelData;

public interface EngineFileAPI {

  /**
   * Saves the current game or level status by: 1) Gathering current state from the Engine (objects,
   * progress, scores) 2) Converting them into a parser-compatible data structure 3) Delegating the
   * final write operation to GameFileParserAPI
   *
   * @throws IOException         if underlying file operations fail
   * @throws DataFormatException if the data cannot be translated into the parser's model
   */
  public void saveLevelStatus() throws IOException, DataFormatException;

  /**
   * Loads a new level or resumes saved progress by: 1) Calling GameFileParserAPI to parse the file
   * into a standardized data structure 2) Translating that structure into the Engine’s runtime
   * objects 3) Updating the current Engine state
   *
   * @throws IOException         if the file cannot be read
   * @throws DataFormatException if the file's data cannot be interpreted into Engine objects
   */
  public LevelData loadFileToEngine() throws IOException, DataFormatException;

}
