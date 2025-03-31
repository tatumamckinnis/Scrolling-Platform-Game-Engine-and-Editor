package oogasalad.game.file.parser.records;

import java.util.List;

public record EventData(
    int order,
    String eventId,
    List<String> parameters
) {}
