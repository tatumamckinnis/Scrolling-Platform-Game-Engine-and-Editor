package oogasalad.fileparser.records;

import java.util.List;

/**
 *
 * @author Billy McCune
 */
public record EventData(
    int order,
    String eventId,
    List<String> parameters
) {}
