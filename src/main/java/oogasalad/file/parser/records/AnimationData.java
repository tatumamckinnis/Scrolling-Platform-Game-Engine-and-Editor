package oogasalad.file.parser.records;

import java.util.List;

public record AnimationData(
    String name,
    double frameLen,
    List<String> frameNames
) {}
