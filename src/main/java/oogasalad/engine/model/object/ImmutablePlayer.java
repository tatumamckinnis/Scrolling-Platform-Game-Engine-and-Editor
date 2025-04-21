package oogasalad.engine.model.object;

import java.util.Map;

/**
 * Immutable interface for accessing read-only information about a {@link Player}.
 *
 * <p>This interface is designed for view or logic components that need to access
 * player-visible stats without modifying the underlying {@link Player} object. It promotes
 * encapsulation and separation of concerns between model and view layers.
 */
public interface ImmutablePlayer extends ImmutableGameObject {

  /**
   * Returns a map of stats that are displayed to the player (e.g., health, score, stamina).
   *
   * @return map of stat names to their double values
   */
  Map<String, String> getDisplayedStatsMap();
}

