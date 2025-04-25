package oogasalad.userData.records;

import java.io.File;
import java.util.List;
import oogasalad.fileparser.records.SpriteData;

/**
 *
 * @param username
 * @param displayName
 * @param email
 * @param password
 * @param language
 * @param bio
 * @param userImage
 * @param userAvatar
 * @param userGameData
 *
 * @author Billy McCune
 */
public record UserData(
    String username,
    String displayName,
    String email,
    String password,
    String language,
    String bio,
    File userImage,
    SpriteData userAvatar,
    List<UserGameData> userGameData
) {

}
