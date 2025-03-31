package oogasalad.fileparser.records;

import java.util.List;

/**
 *
 * @author Billy McCune
 */
public record EventChainData(
    String id,
    List<EventData> events
) {}
