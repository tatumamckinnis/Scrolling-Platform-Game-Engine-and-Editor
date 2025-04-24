/**
 * Game controller API logic implementation
 */
package oogasalad.engine.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.zip.DataFormatException;
import oogasalad.ResourceManager;
import oogasalad.ResourceManagerAPI;
import oogasalad.engine.controller.api.EngineFileConverterAPI;
import oogasalad.engine.controller.api.GameControllerAPI;
import oogasalad.engine.controller.api.GameExecutor;
import oogasalad.engine.controller.api.GameManagerAPI;
import oogasalad.engine.controller.api.GameObjectProvider;
import oogasalad.engine.controller.api.InputProvider;
import oogasalad.engine.model.animation.DefaultAnimationHandler;
import oogasalad.engine.model.event.CollisionHandler;
import oogasalad.engine.model.event.DefaultCollisionHandler;
import oogasalad.engine.model.event.DefaultEventHandler;
import oogasalad.engine.model.event.Event;
import oogasalad.engine.model.event.EventHandler;
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.engine.model.object.mapObject;
import oogasalad.engine.view.camera.Camera;
import oogasalad.exceptions.BlueprintParseException;
import oogasalad.exceptions.EventParseException;
import oogasalad.exceptions.GameObjectParseException;
import oogasalad.exceptions.HitBoxParseException;
import oogasalad.exceptions.LayerParseException;
import oogasalad.exceptions.LevelDataParseException;
import oogasalad.exceptions.PropertyParsingException;
import oogasalad.exceptions.SpriteParseException;
import oogasalad.fileparser.records.GameObjectData;
import oogasalad.fileparser.records.LevelData;

/**
 * Default implementation of the {@link GameControllerAPI}.
 *
 * <p>This class is responsible for managing game objects, loading level data,
 * and updating the game state based on the loaded data. It delegates file handling to an
 * {@link EngineFileConverterAPI} and stores a local list of {@link GameObject}s that represent the
 * current game state.
 *
 * @author Alana Zinkin
 */
public class DefaultGameController implements GameControllerAPI, GameObjectProvider, GameExecutor {

  private static final ResourceManagerAPI resourceManager = ResourceManager.getInstance();

  private final EventHandler eventHandler;
  private final CollisionHandler collisionHandler;
  private Map<String, GameObject> myGameObjectMap;
  private List<GameObject> myGameObjects;
  private mapObject myMapObject;
  private Camera myCamera;
  private final GameManagerAPI myGameManager;
  private final DefaultAnimationHandler myAnimationHandler;

  /**
   * Constructor for the default game controller
   *
   * @param inputProvider object for retrieving any keys or human input
   * @param gameManager   the manager controls the execution of the game loop
   */
  public DefaultGameController(InputProvider inputProvider, GameManagerAPI gameManager) {
    this.collisionHandler = new DefaultCollisionHandler(this);
    this.myAnimationHandler = new DefaultAnimationHandler();
    this.eventHandler = new DefaultEventHandler(inputProvider, collisionHandler, this,
        myAnimationHandler);
    this.myGameObjects = new ArrayList<>();
    this.myGameManager = gameManager;

  }

  @Override
  public List<GameObject> getGameObjects() {
    return myGameObjects;
  }


  @Override
  public List<ImmutableGameObject> getImmutableObjects() {
    return makeGameObjectsImmutable(myGameObjects);
  }

  @Override
  public List<ImmutableGameObject> getImmutablePlayers() {
    List<GameObject> players = new ArrayList<>();
    for (GameObject gameObject : myGameObjects) {
      if (gameObject.getType().equals("player")) {
        players.add(gameObject);
      }
    }
    return makeGameObjectsImmutable(players);
  }

  @Override
  public void endGame() {
    myGameManager.endGame();
  }

  @Override
  public void restartLevel()
      throws LayerParseException, EventParseException, BlueprintParseException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, DataFormatException, LevelDataParseException, PropertyParsingException, SpriteParseException, HitBoxParseException, GameObjectParseException, ClassNotFoundException, InstantiationException {
    myGameManager.restartGame();
  }

  @Override
  public GameObject getGameObjectByUUID(String id) {

    return myGameObjectMap.getOrDefault(id, null);
  }

  @Override
  public mapObject getMapObject() {
    return myMapObject;
  }


  @Override
  public ImmutableGameObject getViewObjectByUUID(String uuid) {
    try {
      return myGameObjectMap.get(uuid);
    } catch (NullPointerException e) {
      throw new NoSuchElementException(resourceManager.getText("exceptions","NoObjectWithUUID") + uuid);
    }
  }


  @Override
  public void updateGameState()
      throws LayerParseException, EventParseException, BlueprintParseException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, DataFormatException, LevelDataParseException, PropertyParsingException, SpriteParseException, HitBoxParseException, GameObjectParseException, ClassNotFoundException, InstantiationException {
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
    myCamera = converter.loadCamera(data);
    myGameObjects = new ArrayList<>(myGameObjectMap.values());
    myMapObject = new mapObject(data.minX(), data.minY(), data.maxX(), data.maxY());
  }

  @Override
  public void destroyGameObject(GameObject gameObject) {
    myGameObjects.remove(gameObject);
    myGameObjectMap.remove(gameObject.getUUID());
    myGameManager.removeGameObjectImage(gameObject);
  }

  @Override
  public void addGameObject(GameObjectData gameObjectData) {
    //get gameObject from level data map
    GameObject gameObject = myGameManager.makeObjectFromData(gameObjectData);
    myGameObjects.add(gameObject);
    myGameObjectMap.put(gameObject.getUUID(), gameObject);
    myGameManager.addGameObjectImage(gameObject);
  }

  @Override
  public Camera getCamera() {
    return myCamera;
  }

  @Override
  public Object getPlayer() {
    return myGameManager.getPlayer();
  }

  @Override
  public String getCurrentGameName() {
    return myGameManager.getCurrentGameName();
  }

  @Override
  public String getCurrentLevelName() {
    return myGameManager.getCurrentLevelName();
  }

  @Override
  public void selectLevel(String filePath)
      throws LayerParseException, EventParseException, BlueprintParseException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, DataFormatException, LevelDataParseException, PropertyParsingException, SpriteParseException, HitBoxParseException, GameObjectParseException, ClassNotFoundException, InstantiationException {
    myGameManager.selectGame(filePath);
  }


  private List<ImmutableGameObject> makeGameObjectsImmutable(
      List<GameObject> gameObjectsToConvert) {
    return new ArrayList<>(gameObjectsToConvert);
  }


}