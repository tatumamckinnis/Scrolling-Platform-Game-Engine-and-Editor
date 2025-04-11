package oogasalad.fileparser.records;

import java.util.Map;

/**
 * Represents the camera configuration data parsed from XML.
 *
 * <p>The {@code CameraData} record
 * encapsulates the type of camera along with its associated properties. It separates properties
 * into two categories: string-based properties and double-based properties. These properties
 * facilitate dynamic camera configuration and setup based on the parsed XML data.</p>
 *
 * @param type             the type identifier of the camera
 * @param stringProperties a map containing string-based properties for the camera
 * @param doubleProperties a map containing double-based properties for the camera
 * @author Billy McCune
 */
public record CameraData(
    String type,
    Map<String, String> stringProperties,
    Map<String, Double> doubleProperties
) {

}
