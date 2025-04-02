/**
 * API to process events
 * @author Gage Garcia
 */
package oogasalad.engine.event;

public interface EventHandler {
    /**
     * handles event by checking its condition/executing its outcome
     * @param event
     */
    void handleEvent(Event event);
}
