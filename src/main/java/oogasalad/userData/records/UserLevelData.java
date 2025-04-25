package oogasalad.userData.records;

import java.util.Map;

public record UserLevelData(
    String levelName,
    String lastPlayed,
    Map<String, String> levelHighestStatMap
) {

}
