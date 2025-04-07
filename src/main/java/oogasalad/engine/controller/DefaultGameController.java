/**
 * Game controller api logic implementation
 */
package oogasalad.engine.controller;

import java.util.*;

import oogasalad.engine.controller.api.EngineFileConverterAPI;
import oogasalad.engine.controller.api.GameControllerAPI;
import oogasalad.engine.event.*;
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.model.object.mapObject;
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
  private static ResourceBundle CONTROLLER_RESOURCES = ResourceBundle.getBundle(DefaultGameController.class.getPackageName() + "." + "Controller");
  private EventHandler eventHandler;
  private CollisionHandler collisionHandler;

  /**
   * constructor for creating a game controller
   * @param inputProvider used to determine which keys were pressed
   */
  public DefaultGameController(InputProvider inputProvider) {
    this.collisionHandler = new CollisionHandler(this);
    this.eventHandler = new DefaultEventHandler(inputProvider,this);
    this.myGameObjects = new ArrayList<>();

  }
  //Map of UUUID (as Strings) to GameObjects
  private Map<String, GameObject> myGameObjectMap;
  //List of all game objects currently in the game state
  private List<GameObject> myGameObjects;
  private mapObject myMapObject;

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
   * @return a collection of immutable game objects
   */
  @Override
  public List<ViewObject> getImmutableObjects() {
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
  public GameObject getGameObjectByUUID(String id) {

    return myGameObjectMap.getOrDefault(id, null);
  }

  /**
   * @return a mapping of UUID to game object
   */
  public mapObject getMapObject(){
    return myMapObject;
  }

  /**
   * Retrieves a game object given its UUID
   * @param uuid unique id of object to retrieve
   * @return GameObject with corresponding UUID
   */
  @Override
  public ViewObject getViewObjectByUUID(String uuid) {
    try {
      return convertToViewObject(myGameObjectMap.get(uuid));
    }
    catch (NullPointerException e) {
      throw new NoSuchElementException(CONTROLLER_RESOURCES.getString("NoObjectWithUUID") + uuid);
    }
  }

  /**
   * Updates the game state.
   * <p>
   * Currently unimplemented — this method should contain logic for progressing the game, handling
   * interactions, updating variables, etc.
   */
  @Override
  public void updateGameState() {
    collisionHandler.updateCollisions();
    List<GameObject> objectsCopy = new ArrayList<>(myGameObjects);
    for (GameObject gameObject : objectsCopy) {
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
    myMapObject = new mapObject(data.levelWidth(), data.levelHeight());
  }

  private List<ViewObject> makeGameObjectsImmutable() {
    List<ViewObject> immutableObjects = new ArrayList<>();
    for (GameObject gameObject : myGameObjects) {
      ViewObject viewObject = convertToViewObject(gameObject);
      // only gives objects to view if there's a real image
      if (gameObject.getCurrentFrame() != null) {
        immutableObjects.add(viewObject);
      }
    }
    return immutableObjects;
  }

  private static ViewObject convertToViewObject(GameObject gameObject) {
    return new ViewObject(gameObject);
  }


  //should refactor
  public CollisionHandler getCollisionHandler() {
    return collisionHandler;
  }

  /**
   * Removes game object from level
   * @param gameObject to remove
   */
  public void destroyGameObject(GameObject gameObject) {
    myGameObjects.remove(gameObject);
    myGameObjectMap.remove(gameObject.getUuid());
  }
}