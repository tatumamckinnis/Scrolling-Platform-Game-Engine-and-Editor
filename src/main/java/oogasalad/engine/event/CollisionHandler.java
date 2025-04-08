/**
 * Interface that implements collision updating/getting
 * @author Gage Garcia
 */
package oogasalad.engine.event;

import oogasalad.engine.model.object.GameObject;

import java.util.List;

public interface CollisionHandler {
    void updateCollisions();

    List<GameObject> getCollisions(GameObject gameObject);

}
