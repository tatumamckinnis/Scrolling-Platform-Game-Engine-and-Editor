package oogasalad.userData.records;

import java.util.Map;

/**
 *
 *
 * @param levelName
 * @param lastPlayed
 * @param levelHighestStatMap
 *
 * @author Billy McCune
 */
public record UserLevelData(
    String levelName,
    String lastPlayed,
    Map<String, String> levelHighestStatMap
) {

}
