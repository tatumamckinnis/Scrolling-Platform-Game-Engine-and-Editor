/**
 * Interface between engine model and view Handles Timeline actions(play/pause), and changing level
 * state
 */
package oogasalad.engine.controller.api;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.zip.DataFormatException;

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
      throws DataFormatException, IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException;

  /**
   * Loads a new level or game scene, possibly by calling into file loaders, parsing game data, and
   * updating the current model.
   */
  void selectGame(String level)
      throws DataFormatException, IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException;

  /**
   * Lists all available levels for user to select
   */
  List<String> listLevels();
}
