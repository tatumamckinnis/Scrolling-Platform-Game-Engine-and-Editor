package oogasalad.userData.records;

import java.io.File;
import java.util.List;
import oogasalad.fileparser.records.SpriteData;

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
