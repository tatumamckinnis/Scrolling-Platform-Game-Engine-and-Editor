package oogasalad.userData.records;

import java.io.File;
import java.util.List;
import oogasalad.fileparser.records.SpriteData;

/**
 * Top-level record containing all user-specific data,
 * including credentials, preferences, profile images, and
 * per-game progress and statistics.
 *
 * @param username the unique user login identifier
 * @param displayName the user-friendly name shown in the UI
 * @param email the user's email address
 * @param password the user's hashed password or credential token
 * @param language the user's preferred language code (e.g., "en", "es")
 * @param bio a short biography or description provided by the user
 * @param userImage the File path to the user's profile image
 * @param userAvatar the SpriteData object representing the user's avatar
 * @param userGameData a list of UserGameData entries for each game the user has played
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
