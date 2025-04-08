/**
 * Interface that outcomes use to update game state
 * @author Gage Garcia
 */
package oogasalad.engine.controller.api;

import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.model.object.mapObject;

public interface GameExecutor {
    void destroyGameObject(GameObject gameObject);

    mapObject getMapObject();

    GameObject getGameObjectByUUID(String id);
}
