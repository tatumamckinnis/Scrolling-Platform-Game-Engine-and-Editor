/**
 * Game controller api logic implementation
 */
package oogasalad.engine.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;

import oogasalad.engine.model.object.GameObject;
import oogasalad.fileparser.records.LevelData;

/**
 * Default implementation of the {@link GameControllerAPI}.
 *
 * <p>This class is responsible for managing game objects, loading level data,
 * and updating the game state based on the loaded data. It delegates file handling
 * to an {@link EngineFileAPI} and stores a local list of {@link GameObject}s
 * that represent the current game state.
 *
 * @author Alana Zinkin
 */
public class DefaultGameController implements GameControllerAPI {

  /** List of all game objects currently in the game state */
  private List<GameObject> myGameObjects = new ArrayList<>();

  /** Reference to the engine's file handling API */
  private EngineFileAPI myEngineFile;

  /**
   * Returns the list of all {@link GameObject}s currently in the game.
   *
   * @return a list of game objects
   */
  @Override
  public List<GameObject> getObjects() {
    return myGameObjects;
  }

  /**
   * Updates the game state.
   * <p>
   * Currently unimplemented — this method should contain logic for progressing
   * the game, handling interactions, updating variables, etc.
   */
  @Override
  public void updateGameState() {
    // To be implemented
  }

  /**
   * Loads a new level into the game using the provided {@link LevelData}.
   * <p>
   * This method uses the {@link EngineFileAPI} to parse and convert level data into game objects.
   *
   * @param data the level data to load
   * @throws DataFormatException if the level data format is incorrect
   * @throws IOException if there is an error reading the data
   * @throws ClassNotFoundException if a referenced class is not found
   * @throws InvocationTargetException if an error occurs during object instantiation
   * @throws NoSuchMethodException if a required constructor is missing
   * @throws InstantiationException if an object cannot be instantiated
   * @throws IllegalAccessException if access to a constructor is denied
   */
  @Override
  public void setLevelData(LevelData data)
      throws DataFormatException, IOException, ClassNotFoundException,
      InvocationTargetException, NoSuchMethodException,
      InstantiationException, IllegalAccessException {
    myGameObjects = myEngineFile.loadFileToEngine(data);
  }
}