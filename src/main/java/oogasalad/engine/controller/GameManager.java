/**
 * Interface between engine model and view
 * Handles Timeline actions(play/pause), and changing level state
 */
package oogasalad.engine.controller;

public interface GameManager {

    /**
     * Initiates or resumes the game loop, updating the model
     * and rendering the view at regular intervals.
     */
    void playGame();

    /**
     * Suspends the game loop, freezing updates and rendering.
     */
    void pauseGame();

    /**
     * Restarts the current game from the beginning (or last checkpoint),
     * resetting all necessary model data.
     */
    void restartGame();

    /**
     * Loads a new level or game scene, possibly by calling into
     * file loaders, parsing game data, and updating the current model.
     */
    void loadLevel(String level);

}
