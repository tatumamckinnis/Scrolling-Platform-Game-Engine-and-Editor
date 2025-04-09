/**
 * Game controller API logic implementation
 */
package oogasalad.engine.controller;

import java.util.*;

import oogasalad.engine.controller.api.*;
import oogasalad.engine.event.*;
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.model.object.ViewObject;
import oogasalad.engine.model.object.mapObject;
import oogasalad.fileparser.records.LevelData;

/**
 * Default implementation of the {@link GameControllerAPI}.
 *
 * <p>This class is responsible for managing game objects, loading level data,
 * and updating the game state based on the loaded data. It delegates file handling to an
 * {@link EngineFileConverterAPI} and stores a local list of {@link GameObject}s that represent the
 * current game state.
 *
 * <p>Implements {@link GameObjectProvider} to expose game object data to external classes, and
 * {@link GameExecutor} to handle game progression logic.
 *
 * @author Alana Zinkin
 */
public class DefaultGameController implements GameControllerAPI, GameObjectProvider, GameExecutor {

  /**
   * Resource bundle for localized controller messages (e.g., error strings)
   */
  private static final ResourceBundle CONTROLLER_RESOURCES = ResourceBundle.getBundle(
      DefaultGameController.class.getPackageName() + "." + "Controller");
  private final EventHandler eventHandler;
  private final CollisionHandler collisionHandler;
  private Map<String, GameObject> myGameObjectMap;
  private List<GameObject> myGameObjects;
  private mapObject myMapObject;

  /**
   * Constructs a new DefaultGameController using the provided input provider.
   *
   * @param inputProvider the source of user input used in event handling
   */
  public DefaultGameController(InputProvider inputProvider) {
    this.collisionHandler = new DefaultCollisionHandler(this);
    this.eventHandler = new DefaultEventHandler(inputProvider, collisionHandler, this);
    this.myGameObjects = new ArrayList<>();
  }

  /**
   * Returns a mutable list of all game objects in the current game state.
   *
   * @return list of {@link GameObject}s
   */
  @Override
  public List<GameObject> getGameObjects() {
    return myGameObjects;
  }

  /**
   * Returns an immutable list of {@link ViewObject}s that can be used by the view. Filters out any
   * objects that do not have a visible frame.
   *
   * @return list of {@link ViewObject}s
   */
  @Override
  public List<ViewObject> getImmutableObjects() {
    return makeGameObjectsImmutable();
  }

  /**
   * Retrieves a {@link GameObject} by its UUID.
   *
   * @param id the UUID of the object
   * @return the corresponding {@link GameObject}, or null if not found
   */
  @Override
  public GameObject getGameObjectByUUID(String id) {
    return myGameObjectMap.getOrDefault(id, null);
  }

  /**
   * Returns the current {@link mapObject} representing the game map.
   *
   * @return the map object
   */
  @Override
  public mapObject getMapObject() {
    return myMapObject;
  }

  /**
   * Retrieves an immutable view of a {@link GameObject} by its UUID.
   *
   * @param uuid the UUID of the object
   * @return the corresponding {@link ViewObject}
   * @throws NoSuchElementException if the UUID is not found
   */
  @Override
  public ViewObject getViewObjectByUUID(String uuid) {
    try {
      return convertToViewObject(myGameObjectMap.get(uuid));
    } catch (NullPointerException e) {
      throw new NoSuchElementException(CONTROLLER_RESOURCES.getString("NoObjectWithUUID") + uuid);
    }
  }

  /**
   * Updates the state of all game objects by:
   * <ul>
   *   <li>Processing collisions</li>
   *   <li>Handling triggered events</li>
   *   <li>Updating object positions based on velocities or physics</li>
   * </ul>
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
      gameObject.updatePosition(); // process y velocity/x velocity from gravity/jump
    }
  }

  /**
   * Loads level data from a {@link LevelData} record and initializes game objects and the map.
   *
   * @param data the level data to be loaded
   */
  @Override
  public void setLevelData(LevelData data) {
    DefaultEngineFileConverter converter = new DefaultEngineFileConverter();
    myGameObjectMap = converter.loadFileToEngine(data);
    myGameObjects = new ArrayList<>(myGameObjectMap.values());
    myMapObject = new mapObject(data.levelWidth(), data.levelHeight());
  }

  /**
   * Removes a {@link GameObject} from the current game state by deleting it from both the list and
   * map.
   *
   * @param gameObject the object to be destroyed
   */
  @Override
  public void destroyGameObject(GameObject gameObject) {
    myGameObjects.remove(gameObject);
    myGameObjectMap.remove(gameObject.getUuid());
  }

  /**
   * Converts all game objects into {@link ViewObject}s, filtering out those without a current
   * frame.
   *
   * @return a list of immutable {@link ViewObject}s
   */
  private List<ViewObject> makeGameObjectsImmutable() {
    List<ViewObject> immutableObjects = new ArrayList<>();
    for (GameObject gameObject : myGameObjects) {
      ViewObject viewObject = convertToViewObject(gameObject);
      // only gives objects to view if there's a real image
      immutableObjects.add(viewObject);
    }
    return immutableObjects;
  }

  /**
   * Converts a {@link GameObject} to a {@link ViewObject}.
   *
   * @param gameObject the object to convert
   * @return the corresponding view object
   */
  private static ViewObject convertToViewObject(GameObject gameObject) {
    return new ViewObject(gameObject);
  }
}
