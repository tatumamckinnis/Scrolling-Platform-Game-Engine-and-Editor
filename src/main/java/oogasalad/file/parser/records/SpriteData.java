package oogasalad.file.parser.records;

import java.util.List;

public record SpriteData(
    String name,
    int x,
    int y,
    int width,
    int height,
    String baseImage, // NEW: path or reference to base sprite image
    List<FrameData> frames,
    List<AnimationData> animations
) {}