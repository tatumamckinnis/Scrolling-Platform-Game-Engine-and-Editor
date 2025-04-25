package oogasalad.userData.records;
import java.util.Map;

public record UserGameData(
    String gameName,
    String lastPlayed,
    Map<String,Double> playerHighestGameStatMap,
    Map<String, UserLevelData> playerLevelStatMap
    ) {

}
