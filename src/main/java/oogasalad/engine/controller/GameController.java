package oogasalad.engine.controller;
import oogasalad.engine.model.object.GameObject;

import java.util.List;
import oogasalad.file.parser.records.LevelData;

/**
 * Interface for interacting with GameControllers
 * @author Gage Garcia
 */

public interface GameController {
    /**
     * Returns a list of game objects that have been updated or changed
     * since the last call (e.g., positions, states, or visual representation).
     * Often used by the rendering system to determine which objects to draw.
     *
     * @return a List of changed/updated GameObjects
     */
    List<GameObject> getUpdatedObjects();

    /**
     * Advances the game state by one "tick" or step, typically by:
     * 1) Calling each phase controller (input, physics, collision, etc.)
     * 2) Resolving any post-update tasks (e.g. removing destroyed objects)
     * 3) Tracking which objects have changed for rendering
     */
    void updateGameState();

    /**
     * Loads a new level or scene, potentially calling file loaders to
     * retrieve data and re-initializing internal structures (objects,
     * controllers, etc.).
     */
    void setLevelData(LevelData data);
}