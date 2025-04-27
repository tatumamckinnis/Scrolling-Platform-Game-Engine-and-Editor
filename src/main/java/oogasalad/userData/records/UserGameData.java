package oogasalad.userData.records;
import java.util.Map;

/**
 * Represents the aggregated game-level data for a user,
 * including the game name, when it was last played,
 * overall highest stats for the game, and detailed per-level data.
 *
 * @param gameName the name of the game
 * @param lastPlayed the timestamp when the game was last played by the user
 * @param playerHighestGameStatMap a map from stat name to the highest overall stat value in the game
 * @param playerLevelStatMap a map from level identifier to UserLevelData for each level
 * @author Billy McCune
 */
public record UserGameData(
    String gameName,
    String lastPlayed,
    Map<String,String> playerHighestGameStatMap,
    Map<String, UserLevelData> playerLevelStatMap
    ) {

}
