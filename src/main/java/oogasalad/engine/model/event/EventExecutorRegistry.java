package oogasalad.engine.model.event;

import java.util.HashMap;
import java.util.Map;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * EventExecutorRegistry
 *
 * A global registry mapping event IDs (strings) to their corresponding EventExecutor instances.
 * Allows for centralized management of all known event types in the system.
 *
 * Design Notes:
 * - This registry can be initialized from hardcoded data, a config file, or another data source.
 * - The static design allows for easy global access without passing instances.
 * - You can replace this registry entirely with another implementation if needed.
 */
public final class EventExecutorRegistry {

  // Map of event ID → EventExecutor instance
  private static final Map<String, EventExecutor> registry = new HashMap<>();

  // Prevent instantiation
  private EventExecutorRegistry() {}

  /**
   * Registers an EventExecutor for a given event ID.
   *
   * @param eventId unique string ID for the event type
   * @param executor the EventExecutor that handles the event
   */
  public static void registerExecutor(String eventId, EventExecutor executor) {
    registry.put(eventId, executor);
  }

  /**
   * Retrieves the EventExecutor associated with the given event ID.
   *
   * @param eventId the string ID of the event
   * @return the EventExecutor if registered, or null if not found
   */
  public static EventExecutor getExecutor(String eventId) {
    return registry.get(eventId);
  }

  /**
   * Checks if an executor is registered for the given event ID.
   *
   * @param eventId the event ID to check
   * @return true if an executor exists, false otherwise
   */
  public static boolean contains(String eventId) {
    return registry.containsKey(eventId);
  }

  /**
   * Removes an executor from the registry (optional).
   *
   * @param eventId the ID to unregister
   */
  public static void unregister(String eventId) {
    registry.remove(eventId);
  }

  /**
   * Returns a set of all currently registered event IDs.
   *
   * @return set of registered event IDs
   */
  public static Set<String> getAllEventIds() {
    return registry.keySet();
  }

  /**
   * Clears the entire registry (useful for testing or reload scenarios).
   */
  public static void clearRegistry() {
    registry.clear();
  }
}

