package oogasalad.userData.records;
import java.util.Map;

/**
 *
 * @param gameName
 * @param lastPlayed
 * @param playerHighestGameStatMap
 * @param playerLevelStatMap
 *
 * @author Billy McCune
 */
public record UserGameData(
    String gameName,
    String lastPlayed,
    Map<String,String> playerHighestGameStatMap,
    Map<String, UserLevelData> playerLevelStatMap
    ) {

}
