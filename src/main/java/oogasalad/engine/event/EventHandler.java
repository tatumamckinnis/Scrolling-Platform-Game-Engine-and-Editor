/**
 * API to process events
 * @author Gage Garcia
 */
package oogasalad.engine.event;

public interface EventHandler {
    /**
     * handles event by checking its condition/executing its outcome
     * condition checking done [[A OR B] AND [C OR D]  AND [E OR F]]
     * @param event event model to handle
     */
    void handleEvent(Event event);
}
