
package oogasalad.engine.controller.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.zip.DataFormatException;
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.exceptions.BlueprintParseException;
import oogasalad.exceptions.EventParseException;
import oogasalad.exceptions.GameObjectParseException;
import oogasalad.exceptions.HitBoxParseException;
import oogasalad.exceptions.LayerParseException;
import oogasalad.exceptions.LevelDataParseException;
import oogasalad.exceptions.PropertyParsingException;
import oogasalad.exceptions.RenderingException;
import oogasalad.exceptions.SpriteParseException;
import oogasalad.fileparser.records.GameObjectData;

/**
 * API responsible for managing the game loop, including playing, pausing, and selecting a game
 *
 * @author Alana Zinkin
 */
public interface GameManagerAPI {

  /**
   * Initiates or resumes the game loop, updating the model and rendering the view at regular
   * intervals.
   */
  void playGame();

  /**
   * Suspends the game loop, freezing updates and rendering.
   */
  void pauseGame();

  /**
   * Restarts the current game from the beginning (or last checkpoint), resetting all necessary
   * model data.
   */
  void restartGame()
      throws DataFormatException, IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, LayerParseException, LevelDataParseException, PropertyParsingException, SpriteParseException, EventParseException, HitBoxParseException, BlueprintParseException, GameObjectParseException;

  /**
   * Loads a new level or game scene, possibly by calling into file loaders, parsing game data, and
   * updating the current model.
   */
  void selectGame(String filePath)
      throws DataFormatException, IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, LayerParseException, LevelDataParseException, PropertyParsingException, SpriteParseException, EventParseException, HitBoxParseException, BlueprintParseException, GameObjectParseException;

  /**
   * Pauses the timeline and displays a splashscreen when the user either wins or loses
   */
  void endGame();

  /**
   * Lists all available levels for user to select
   */
  List<String> listLevels();

  /**
   * Displays initial game objects when the level is first loaded.
   *
   * @throws RenderingException    thrown if error rendering objects.
   * @throws FileNotFoundException thrown if invalid level file.
   */
  void displayGameObjects() throws RenderingException, FileNotFoundException;

  /**
   * removes a game object image from the level view scene
   *
   * @param gameObject the game object to remove from the view
   */
  void removeGameObjectImage(ImmutableGameObject gameObject);

  /**
   * adds game object image from the level view scene
   *
   * @param gameObject the game object to add to the view
   */
  void addGameObjectImage(ImmutableGameObject gameObject);

  /**
   * get gameobject using bid from level api
   *
   */
  public GameObject makeObjectFromData(GameObjectData gameObjectData);
  /**
   * @return a String path to the current level file
   */
  String getCurrentLevel() throws NullPointerException;

  /**
   * sets the language of the text for the Resource Manager
   *
   * @param language new language selected
   */
  void setLanguage(String language);

  Object getPlayer();

  String getCurrentGameName();

  String getCurrentLevelName();
}
