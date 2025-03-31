package oogasalad.engine.controller;

import java.io.IOException;
import java.util.zip.DataFormatException;

public interface LevelAPI {

  public void selectGame(String game, String category, String level)
      throws DataFormatException, IOException;

}
