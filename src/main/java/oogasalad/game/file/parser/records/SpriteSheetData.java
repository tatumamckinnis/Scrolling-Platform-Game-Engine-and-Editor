package oogasalad.game.file.parser.records;

import java.util.List;

public record SpriteSheetData(
    String imagePath,
    int width,
    int height,
    List<SpriteData> sprites
) {}
