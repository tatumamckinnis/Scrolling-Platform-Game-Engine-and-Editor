//  oogasalad/editor/model/data/object/event/AbstractEventMapData.java
package oogasalad.editor.model.data.object.event;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for managing a map of events, where each event is associated with a unique
 * identifier (String). Provides basic functionality for adding, removing, and retrieving events.
 *
 * @author Jacob You
 */
public abstract class AbstractEventMapData {

  private final Map<String, EditorEvent> events = new HashMap<>();

  /**
   * Retrieves the event associated with the given identifier.
   *
   * @param eventId the identifier of the event to retrieve
   * @return the {@link EditorEvent} corresponding to the identifier, or null if not found
   */
  public EditorEvent getEvent(String eventId) {
    return events.get(eventId);
  }

  /**
   * Adds a new event with the specified identifier.
   *
   * @param eventId the identifier for the new event
   * @param event   the {@link EditorEvent} to add
   */
  public void addEvent(String eventId, EditorEvent event) {
    events.put(eventId, event);
  }

  /**
   * Removes the event associated with the given identifier.
   *
   * @param eventId the identifier of the event to remove
   * @return true if the event was removed, false otherwise
   */
  public boolean removeEvent(String eventId) {
    return events.remove(eventId) != null;
  }

  /**
   * Retrieves all events.
   *
   * @return A map of all events.
   */
  public Map<String, EditorEvent> getEvents() {
    return events;
  }

  /**
   * Sets the event associated with the given identifier, replacing any existing event with that ID.
   *
   * @param eventId the identifier of the event to set
   * @param event   the {@link EditorEvent} to set
   */
  public void setEvent(String eventId, EditorEvent event) {
    events.put(eventId, event);
  }
}