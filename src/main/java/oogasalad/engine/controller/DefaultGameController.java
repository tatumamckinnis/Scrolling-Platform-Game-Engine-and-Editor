/**
 * Game controller API logic implementation
 */
package oogasalad.engine.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import oogasalad.engine.controller.api.EngineFileConverterAPI;
import oogasalad.engine.controller.api.GameControllerAPI;
import oogasalad.engine.controller.api.GameExecutor;
import oogasalad.engine.controller.api.GameObjectProvider;
import oogasalad.engine.controller.api.InputProvider;
import oogasalad.engine.event.CollisionHandler;
import oogasalad.engine.event.DefaultCollisionHandler;
import oogasalad.engine.event.DefaultEventHandler;
import oogasalad.engine.event.Event;
import oogasalad.engine.event.EventHandler;
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.model.object.ViewObject;
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
public class DefaultGameController implements GameControllerAPI, GameObjectProvider, GameExecutor {
  private static final ResourceBundle CONTROLLER_RESOURCES = ResourceBundle.getBundle(DefaultGameController.class.getPackageName() + "." + "Controller");
  private final EventHandler eventHandler;
  private final CollisionHandler collisionHandler;
  private Map<String, GameObject> myGameObjectMap;
  private List<GameObject> myGameObjects;
  private mapObject myMapObject;

  public DefaultGameController(InputProvider inputProvider) {
    this.collisionHandler = new DefaultCollisionHandler(this);
    this.eventHandler = new DefaultEventHandler(inputProvider, collisionHandler, this);
    this.myGameObjects = new ArrayList<>();

  }

  @Override
  public List<GameObject> getGameObjects() {
    return myGameObjects;
  }


  @Override
  public List<ViewObject> getImmutableObjects() {
    return makeGameObjectsImmutable();
  }



  @Override
  public GameObject getGameObjectByUUID(String id) {

    return myGameObjectMap.getOrDefault(id, null);
  }

  @Override
  public mapObject getMapObject(){
    return myMapObject;
  }


  @Override
  public ViewObject getViewObjectByUUID(String uuid) {
    try {
      return convertToViewObject(myGameObjectMap.get(uuid));
    }
    catch (NullPointerException e) {
      throw new NoSuchElementException(CONTROLLER_RESOURCES.getString("NoObjectWithUUID") + uuid);
    }
  }


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

  @Override
  public void setLevelData(LevelData data) {
    DefaultEngineFileConverter converter = new DefaultEngineFileConverter();
    myGameObjectMap = converter.loadFileToEngine(data);
    myGameObjects = new ArrayList<>(myGameObjectMap.values());
    myMapObject = new mapObject(data.levelWidth(), data.levelHeight(), data.levelWidth(), data.levelHeight());
  }

  @Override
  public void destroyGameObject(GameObject gameObject) {
    myGameObjects.remove(gameObject);
    myGameObjectMap.remove(gameObject.getUUID());
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

}