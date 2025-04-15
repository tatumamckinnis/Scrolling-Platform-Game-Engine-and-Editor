package oogasalad.editor.model.data.object.event;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Abstract base class holding common event data, specifically a map of event IDs to EditorEvent
 * objects.
 *
 * @author Jacob You
 */
public abstract class AbstractEventMapData {

  private static final Logger LOG = LogManager.getLogger(AbstractEventMapData.class);

  private final Map<String, EditorEvent> events;

  /**
   * Constructor initializes the events map.
   */
  public AbstractEventMapData() {
    events = new HashMap<>();
  }

  /**
   * Returns an unmodifiable view of the events map. Prevents external modification of the internal
   * map. (DESIGN-10)
   *
   * @return An unmodifiable Map of event IDs to EditorEvent objects.
   */
  public Map<String, EditorEvent> getEvents() {
    return Collections.unmodifiableMap(events);
  }

  /**
   * Adds a new event or replaces an existing event with the same ID.
   *
   * @param eventId The unique identifier for the event (cannot be null or empty).
   * @param event   The EditorEvent object containing conditions and outcomes (cannot be null).
   */
  public void addEvent(String eventId, EditorEvent event) {
    Objects.requireNonNull(eventId, "Event ID cannot be null.");
    Objects.requireNonNull(event, "EditorEvent cannot be null.");
    if (eventId.trim().isEmpty()) {
      LOG.error("Attempted to add event with empty ID.");
      throw new IllegalArgumentException("Event ID cannot be empty.");
    }
    if (events.containsKey(eventId)) {
      LOG.warn("Overwriting existing event with ID: {}", eventId);
    }
    events.put(eventId, event);
    LOG.trace("Added/Updated event with ID: {}", eventId);
  }

  /**
   * Removes an event based on its ID.
   *
   * @param eventId The ID of the event to remove (cannot be null).
   * @return true if an event with the given ID was found and removed, false otherwise.
   */
  public boolean removeEvent(String eventId) {
    Objects.requireNonNull(eventId, "Event ID cannot be null for removal.");
    if (events.containsKey(eventId)) {
      events.remove(eventId);
      LOG.trace("Removed event with ID: {}", eventId);
      return true;
    } else {
      LOG.warn("Attempted to remove non-existent event with ID: {}", eventId);
      return false;
    }
  }

  /**
   * Retrieves a specific event by its ID.
   *
   * @param eventId The ID of the event to retrieve (cannot be null).
   * @return The EditorEvent object, or null if no event with that ID exists.
   */
  public EditorEvent getEvent(String eventId) {
    Objects.requireNonNull(eventId, "Event ID cannot be null for retrieval.");
    EditorEvent event = events.get(eventId);
    return event;
  }
}
