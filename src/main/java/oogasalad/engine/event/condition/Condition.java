/**
 * Interface representing a condition that either returns true or false
 * @author Gage Garcia
 */
package oogasalad.engine.event.condition;

import oogasalad.engine.model.object.GameObject;

public interface Condition {
    /**
     *
     * @param gameObject -> the game object tied to the event
     * @return whether the condition is met
     */
    boolean isMet(GameObject gameObject);
}
