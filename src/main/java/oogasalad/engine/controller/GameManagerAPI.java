/**
 * Interface between engine model and view Handles Timeline actions(play/pause), and changing level
 * state
 */
package oogasalad.engine.controller;

import java.io.IOException;
import java.util.zip.DataFormatException;

public interface GameManagerAPI {

  /**
   * Initiates or resumes the game loop, updating the model and rendering the view at regular
   * intervals.
   */
  public void playGame();

  /**
   * Suspends the game loop, freezing updates and rendering.
   */
  public void pauseGame();

  /**
   * Restarts the current game from the beginning (or last checkpoint), resetting all necessary
   * model data.
   */
  public void restartGame();

  /**
   * Loads a new level or game scene, possibly by calling into file loaders, parsing game data, and
   * updating the current model.
   */
  public void selectGame(String game, String category, String level) throws DataFormatException, IOException;

}
