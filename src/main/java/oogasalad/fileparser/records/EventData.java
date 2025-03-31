package oogasalad.fileparser.records;

import java.util.List;

/**
 *
 * @author Billy McCune
 */
public record EventData(
    String name,
    String type,
    String eventId,
    List<List<String>> conditions,
    List<String> outcomes,
    List<String> parameters
) {}
