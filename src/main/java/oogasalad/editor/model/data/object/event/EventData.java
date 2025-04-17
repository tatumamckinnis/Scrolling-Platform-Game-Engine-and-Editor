package oogasalad.editor.model.data.object.event;

import java.util.ArrayList;
import java.util.List;

/**
 * The EventData class holds a list of all events by string ID in a specific order. This order is
 * what will be used as the order of execution.
 *
 * @author Jacob You
 */
public class EventData {

  private List<String> events;

  /**
   * Instantiates events to a new {@link ArrayList}.
   */
  public EventData() {
    events = new ArrayList<>();
  }

  /**
   * Adds an event ID to the event list.
   * @param event the event ID name to add to the list
   */
  public void addEvent(String event) {
    events.add(event);
  }

  /**
   * Gets all event IDs tied to this object.
   * @return the list of event ID names
   */
  public List<String> getEvents() {
    return events;
  }
}
