/**
 * Game controller api logic implementation
 */
package oogasalad.engine.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import oogasalad.engine.event.DefaultEventHandler;
import oogasalad.engine.event.Event;
import oogasalad.engine.event.EventHandler;
import oogasalad.engine.event.InputHandler;
import oogasalad.engine.model.object.GameObject;
import oogasalad.fileparser.records.LevelData;

/**
 * Default implementation of the {@link GameControllerAPI}.
 *
 * <p>This class is responsible for managing game objects, loading level data,
 * and updating the game state based on the loaded data. It delegates file handling to an
 * {@link EngineFileConverterAPI} and stores a local list of {@link GameObject}s that represent the current
 * game state.
 *
 * @author Alana Zinkin
 */
public class DefaultGameController implements GameControllerAPI {
  private InputProvider inputProvider;

  public DefaultGameController(InputProvider inputProvider) {
    inputProvider = inputProvider;

  }

  /**
   * Map of UUUID (as Strings) to GameObjects
   */
  private Map<String, GameObject> myGameObjectMap;

  /**
   * List of all game objects currently in the game state
   */
  private List<GameObject> myGameObjects;


  /**
   * Returns the list of all {@link GameObject}s currently in the game.
   *
   * @return a list of game objects
   */
  @Override
  public List<GameObject> getObjects() {
    return myGameObjects;
  }

  @Override
  public List<GameObjectRecord> getImmutableObjects() {
    return makeGameObjectsImmutable();
  }


  /**
   * Returns a map of all game objects currently loaded in the engine.
   *
   * <p>The map uses each object's unique UUID as the key and the corresponding
   * {@link GameObject} as the value. This allows for efficient lookup and manipulation of
   * individual game objects by their identifier.
   *
   * @return a map of UUID strings to their associated {@link GameObject} instances
   */
  @Override
  public Map<String, GameObject> getGameObjectMap() {
    return myGameObjectMap;
  }

  /**
   * Updates the game state.
   * <p>
   * Currently unimplemented — this method should contain logic for progressing the game, handling
   * interactions, updating variables, etc.
   */
  @Override
  public void updateGameState() {
    EventHandler eventHandler = new DefaultEventHandler(inputProvider,this);
    InputHandler inputHandler = new InputHandler();
    for (GameObject gameObject : myGameObjects) {
      List<Event> objectEvents = gameObject.getEvents();
      for (Event event : objectEvents) {
        eventHandler.handleEvent(event);
      }
      gameObject.updatePosition(); //process y velocity/xvelocity from gravity/jump
    }

  }

  /**
   * Loads a new level into the game using the provided {@link LevelData}.
   * <p>
   * This method uses the {@link EngineFileConverterAPI} to parse and convert level data into game objects.
   *
   * @param data the level data to load
   */
  @Override
  public void setLevelData(LevelData data) {
    DefaultEngineFileConverter converter = new DefaultEngineFileConverter();
    myGameObjectMap = converter.loadFileToEngine(data);
    myGameObjects = new ArrayList<>(myGameObjectMap.values());
    System.out.println(myGameObjects);
  }

  private List<GameObjectRecord> makeGameObjectsImmutable() {
    List<GameObjectRecord> immutableObjects = new ArrayList<>();
    for (GameObject gameObject : myGameObjects) {
      GameObjectRecord record = new GameObjectRecord(
          gameObject.getSpriteX(),
          gameObject.getSpriteY(),
          gameObject.getCurrentFrame()
      );
      if (gameObject.getCurrentFrame() != null) {
        immutableObjects.add(record);
      }
    }
    return immutableObjects;
  }
}