package oogasalad.game.file.parser.records;

import java.util.List;

public record EventChainData(
    String id,
    List<EventData> events
) {}
