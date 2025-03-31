package oogasalad.engine.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.DataFormatException;
import oogasalad.fileparser.records.LevelData;

public class DefaultEngineFile implements EngineFileAPI {

  @Override
  public void saveLevelStatus() throws IOException, DataFormatException {

  }

  @Override
  public LevelData loadFileToEngine() throws IOException, DataFormatException {
    return new LevelData("Dinosaur Jump", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
  }
}
