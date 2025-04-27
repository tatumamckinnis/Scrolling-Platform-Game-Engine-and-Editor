package oogasalad.userData.records;

import java.util.Map;

/**
 * Represents the data for a single level belonging to a user,
 * including the level name, when it was last played, and
 * the highest recorded stats for that level.
 *
 * @param levelName the unique name or identifier of the level
 * @param lastPlayed the timestamp (e.g., ISO-8601 string) when the level was last played
 * @param levelHighestStatMap a map from stat name to the highest value achieved on this level
 * @author Billy McCune
 */
public record UserLevelData(
    String levelName,
    String lastPlayed,
    Map<String, String> levelHighestStatMap
) {

}
