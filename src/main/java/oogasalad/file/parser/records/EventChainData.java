package oogasalad.file.parser.records;

import java.util.List;

public record EventChainData(
    String id,
    List<EventData> events
) {}
